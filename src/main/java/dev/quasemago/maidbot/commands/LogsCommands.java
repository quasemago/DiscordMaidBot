package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
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
public class LogsCommands implements SlashCommand {
    @Autowired
    private GuildServerService serversService;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            final var options = event.getOptions().get(0);
            final String optionName = options.getName();

            switch (optionName) {
                case "status" -> {
                    return logsStatus(event, guildServer);
                }
                case "toggle" -> {
                    return logsToggle(event, guildServer, options);
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

    private Mono<Void> logsStatus(ChatInputInteractionEvent event, GuildServer guildServer) {
        // Check if the logs are configured.
        if (guildServer.getLogChannelId() == null || guildServer.getLogFlags() == null) {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 Logs Status")
                            .description("Logs have not yet been configured.\nType ``/logs toggle`` to configure them.")
                            .color(Color.RED)
                            .build())
                    .withEphemeral(true);
        } else {
            // Logs is already configured, send a message with the current configuration.
            final StringBuilder logsTypes = new StringBuilder();
            final LogTypesSet logsSet = LogTypesSet.of(guildServer.getLogFlags());
            logsSet.iterator().forEachRemaining(type -> logsTypes.append(type.getName()).append("\n"));

            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 Logs Status")
                            .description("Logs are Enabled.\nType ``/logs toggle`` to configure them.")
                            .addField("Logs Channel", "<#"+ guildServer.getLogChannelId() +">", false)
                            .addField("Logs Types", logsTypes.toString(), false)
                            .color(Color.GREEN)
                            .build())
                    .withEphemeral(true);
        }
    }

    private Mono<Void> logsToggle(ChatInputInteractionEvent event, GuildServer guildServer, ApplicationCommandInteractionOption option) {
        final var statusOption = option.getOption("status")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .orElse(null);

        final boolean status = statusOption != null && statusOption.asBoolean();
        if (!status) {
            // Check if the logs are already disabled.
            if (guildServer.getLogChannelId() == null || guildServer.getLogFlags() == null) {
                return event.reply("Logs are already disabled.")
                        .withEphemeral(true);
            }

            // Disable the logs.
            guildServer.setLogChannelId(null);
            guildServer.setLogFlags(null);
            this.serversService.save(guildServer);

            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 Logs Disabled")
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
        final Mono<Void> tempListener = createTempMenuListener(event, guildServer, channelId);

        // Check if the logs are already configured.
        if (guildServer.getLogChannelId() == null || guildServer.getLogFlags() == null) {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 Logs Configuration")
                            .description("Logs have not yet been configured.\nConfigure log options:")
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
                            .title("\uD83D\uDCF0 Logs Configuration")
                            .description("Logs are already configured.\nConfigure log options:")
                            .build())
                    .withComponents(ActionRow.of(createTempMenuOptions(guildServer)))
                    .withEphemeral(true)
                    .then(tempListener)
                    .onErrorResume(e -> {
                        Logger.log.error("Failed to send logs toggle message: {}", e.getMessage(), e);
                        return Mono.empty();
                    });
        }
    }

    private Mono<Void> createTempMenuListener(ChatInputInteractionEvent event, GuildServer guildServer, long channelId) {
        return event.getInteraction()
                .getClient()
                .on(SelectMenuInteractionEvent.class, e -> {
                    if (e.getCustomId().equalsIgnoreCase("logs-toggle-options")) {
                        final long rawValue = e.getValues()
                                .stream()
                                .mapToLong(Long::parseLong)
                                .sum();

                        guildServer.setLogChannelId(channelId);
                        guildServer.setLogFlags(rawValue);
                        this.serversService.save(guildServer);

                        return e.deferEdit()
                                .then(e.editReply("**Updated!**")
                                        .withComponents(ActionRow.of(createTempMenuOptions(guildServer))))
                                .then();
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

    private SelectMenu createTempMenuOptions(GuildServer serverGuild) {
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

    @Override
    public String name() {
        return "logs";
    }

    @Override
    public String description() {
        return "[Admin] Event log system (join, ban, etc)";
    }
    @Override
    public Permission permission() {
        return Permission.ADMINISTRATOR;
    }
}