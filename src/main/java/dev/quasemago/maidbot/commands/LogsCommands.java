package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.repository.ServersRepository;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.models.Servers;
import dev.quasemago.maidbot.models.SlashCommand;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.event.domain.interaction.SelectMenuInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.component.ActionRow;
import discord4j.core.object.component.SelectMenu;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeoutException;

@Component
public class LogsCommands extends SlashCommand<ChatInputInteractionEvent> {
    @Autowired
    private ServersRepository serversRepository;

    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            final Snowflake guild = event.getInteraction()
                    .getGuildId()
                    .orElse(null);

            if (guild == null) {
                Logger.log.error("Failed to get guild id from event: {}", event);
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Failed to get guild.");
            }

            final var options = event.getOptions().get(0);
            final String optionName = options.getName();

            switch (optionName) {
                case "status" -> {
                    return logsStatus(event, guild);
                }
                case "toggle" -> {
                    return logsToggle(event, guild, options);
                }
            }

            Logger.log.error("Failed to get command options: {}", event);
            return event.reply("Failed get command options.")
                    .withEphemeral(true);
        } else {
            return event.reply("This command can't be done in a PM/DM.")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> logsStatus(ChatInputInteractionEvent event, Snowflake guildId) {
        final Servers serverGuild = serversRepository.findByGuildId(guildId.asLong());

        // If the guild is not in the database,
        // it means that the initial configuration of the logs has not been done yet.
        if (serverGuild == null || serverGuild.getLogChannelId() == null || serverGuild.getLogFlags() == null) {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("Logs Status")
                            .description("Logs have not yet been configured.\nType ``/logs toggle`` to configure them.")
                            .color(Color.RED)
                            .build())
                    .withEphemeral(true);
        } else {
            // Logs is already configured, send a message with the current configuration.
            final StringBuilder logsTypes = new StringBuilder();
            if (serverGuild.getLogFlags() == null) {
                logsTypes.append("None");
            } else {
                final LogTypesSet logsSet = LogTypesSet.of(serverGuild.getLogFlags());
                logsSet.iterator().forEachRemaining(type -> logsTypes.append(type.getName()).append("\n"));
            }

            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("Logs Status")
                            .description("Logs are Enabled.\nType ``/logs toggle`` to reconfigure them.")
                            .addField("Logs Channel", "<#"+ serverGuild.getLogChannelId() +">", false)
                            .addField("Logs Types", logsTypes.toString(), false)
                            .color(Color.GREEN)
                            .build())
                    .withEphemeral(true);
        }
    }

    private Mono<Void> logsToggle(ChatInputInteractionEvent event, Snowflake guildId, ApplicationCommandInteractionOption option) {
        final Servers serverGuild = serversRepository.findByGuildId(guildId.asLong());
        final boolean statusOption = option.getOption("status")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asBoolean();

        if (!statusOption) {
            // Since the guild isn't in the database,
            // logs are disabled by default.
            if (serverGuild == null) {
                return event.reply("Logs are already disabled.")
                        .withEphemeral(true);
            }

            // Logs are already disabled.
            if (serverGuild.getLogChannelId() == null || serverGuild.getLogFlags() == null) {
                return event.reply("Logs are already disabled.")
                        .withEphemeral(true);
            }

            // Disable the logs.
            serverGuild.setLogChannelId(null);
            serverGuild.setLogFlags(null);
            serversRepository.save(serverGuild);

            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("Logs Disabled")
                            .description("Logs have been disabled.")
                            .build())
                    .withEphemeral(true);
        }

        // User is enabling the logs, then get the channel from command.
        final var channelOption = option.getOption("channel")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .orElse(null);

        // Channel isn't specified.
        if (channelOption == null) {
            Logger.log.error("Failed to get channel from event: {}", event);
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Failed. Since you are enabling logs, you must specify a channel.");
        }

        final long channelId = Objects.requireNonNull(channelOption
                        .asChannel()
                        .block())
                .getId()
                .asLong();

        // Create a temp menu listener to this event.
        final Mono<Void> tempListener = createTempMenuListener(event, channelId);

        // If the guild isn't in the database,
        // it means that the initial configuration of the logs has not been done yet.
        if (serverGuild == null) {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("Logs Toggle")
                            .description("Logs have not yet been configured.\nConfigure:")
                            .build())
                    .withComponents(ActionRow.of(createTempMenuOptions(null)))
                    .withEphemeral(true)
                    .then(tempListener)
                    .onErrorResume(e -> {
                        Logger.log.error("Failed to send logs toggle message: {}", e.getMessage(), e);
                        return Mono.empty();
                    });
        } else {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("Logs Toggle")
                            .description("Logs are already configured.\nConfigure:")
                            .build())
                    .withComponents(ActionRow.of(createTempMenuOptions(serverGuild)))
                    .withEphemeral(true)
                    .then(tempListener)
                    .onErrorResume(e -> {
                        Logger.log.error("Failed to send logs toggle message: {}", e.getMessage(), e);
                        return Mono.empty();
                    });
        }
    }

    private SelectMenu createTempMenuOptions(Servers serverGuild) {
        long logFlags;
        if (serverGuild == null || serverGuild.getLogChannelId() == null || serverGuild.getLogFlags() == null) {
            logFlags = 0L;
        } else {
            logFlags = serverGuild.getLogFlags();
        }

        final LogTypesSet logTypesSet = LogTypesSet.of(logFlags);

        List<SelectMenu.Option> options = new ArrayList<>();
        for (LogTypes logType : LogTypes.values()) {
            if (logTypesSet.contains(logType)) {
                options.add(SelectMenu.Option.of(logType.getName(), String.valueOf(logType.getValue())).withDefault(true));
            } else {
                options.add(SelectMenu.Option.of(logType.getName(), String.valueOf(logType.getValue())));
            }
        }

        return SelectMenu.of("logs-toggle-options", options)
                .withMaxValues(LogTypes.values().length).withMinValues(1);
    }

    private Mono<Void> createTempMenuListener(ChatInputInteractionEvent event, long channelId) {
        return event.getInteraction()
                .getClient()
                .on(SelectMenuInteractionEvent.class, e -> {
                    if (e.getCustomId().equalsIgnoreCase("logs-toggle-options")) {
                        final long rawValue = e.getValues()
                                .stream()
                                .mapToLong(Long::parseLong)
                                .sum();

                        final Snowflake guild = e.getInteraction()
                                .getGuildId()
                                .orElse(null);
                        if (guild == null) {
                            return Mono.empty();
                        }

                        final long guildId = guild.asLong();

                        Servers serverGuild = serversRepository.findByGuildId(guildId);
                        if (serverGuild == null) {
                            serverGuild = new Servers(guildId, rawValue, channelId);
                        } else {
                            serverGuild.setLogChannelId(channelId);
                            serverGuild.setLogFlags(rawValue);
                        }

                        serversRepository.save(serverGuild);
                        return e.reply("Updated! values: {"+ rawValue +"} with channel {<#"+ channelId +">}")
                                .withEphemeral(true);
                    } else {
                        return Mono.empty();
                    }
                })
                .timeout(Duration.ofMinutes(5))
                .onErrorResume(e -> {
                    if (!(e instanceof TimeoutException)) {
                        Logger.log.error("Failed to get logs toggle menu interaction: {}", e.getMessage(), e);
                    }
                    return Mono.empty();
                })
                .then();
    }

    @Override
    public String getName() {
        return "logs";
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}