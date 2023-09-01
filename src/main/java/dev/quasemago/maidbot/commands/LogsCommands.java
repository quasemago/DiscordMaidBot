package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
import dev.quasemago.maidbot.services.TranslatorService;
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
    @Autowired
    private TranslatorService translatorService;
    private GuildServer server;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        this.server = guildServer;

        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            final var options = event.getOptions().get(0);
            final String optionName = options.getName();

            switch (optionName) {
                case "status" -> {
                    return logsStatus(event);
                }
                case "toggle" -> {
                    return logsToggle(event, options);
                }
            }

            Logger.log.error("Failed to get command options: {}", event);
            return event.reply(translatorService.translate(guildServer, "command_error.failedtogetoptions"))
                    .withEphemeral(true);
        } else {
            return event.reply(translatorService.translate(guildServer, "command_error.restrict.dm"))
                    .withEphemeral(true);
        }
    }

    private Mono<Void> logsStatus(ChatInputInteractionEvent event) {
        // Check if the logs are configured.
        if (server.getLogChannelId() == null || server.getLogFlags() == null) {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 Logs Status")
                            .description(translatorService.translate(server, "command.logs.field.notyetconfigured"))
                            .color(Color.RED)
                            .build())
                    .withEphemeral(true);
        } else {
            // Logs is already configured, send a message with the current configuration.
            final StringBuilder logsTypes = new StringBuilder();
            final LogTypesSet logsSet = LogTypesSet.of(server.getLogFlags());
            logsSet.iterator().forEachRemaining(type -> logsTypes.append(type.getName()).append("\n"));

            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 Logs Status")
                            .description(translatorService.translate(server, "command.logs.field.alreadyconfigured"))
                            .addField(translatorService.translate(server, "command.logs.field.title.channel"), "<#"+ server.getLogChannelId() +">", false)
                            .addField(translatorService.translate(server, "command.logs.field.title.types"), logsTypes.toString(), false)
                            .color(Color.GREEN)
                            .build())
                    .withEphemeral(true);
        }
    }

    private Mono<Void> logsToggle(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option) {
        final var statusOption = option.getOption("status")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .orElse(null);

        if (statusOption == null) {
            return event.reply(translatorService.translate(server, "command_error.failedtogetoptionsvalue"))
                    .withEphemeral(true);
        }

        final boolean status = statusOption.asBoolean();
        if (!status) {
            // Check if the logs are already disabled.
            if (server.getLogChannelId() == null || server.getLogFlags() == null) {
                return event.reply(translatorService.translate(server, "command.logs.reply.failed.alreadydisabled"))
                        .withEphemeral(true);
            }

            // Disable the logs.
            server.setLogChannelId(null);
            server.setLogFlags(null);
            this.serversService.saveGuildServer(server);

            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 " + translatorService.translate(server, "command.logs.field.disabled.title"))
                            .description(translatorService.translate(server, "command.logs.field.disabled.text"))
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
                    .withContent(translatorService.translate(server, "command.logs.reply.failed.mustspecifychannel"));
        }

        final long channelId = Objects.requireNonNull(channelOption
                        .asChannel()
                        .block())
                .getId()
                .asLong();

        // Create a temp menu listener to this event.
        // TODO: Create a interaction menu interface.
        final Mono<Void> tempListener = createTempMenuListener(event, server, channelId);

        // Check if the logs are already configured.
        if (server.getLogChannelId() == null || server.getLogFlags() == null) {
            return event.reply()
                    .withEmbeds(EmbedCreateSpec.builder()
                            .title("\uD83D\uDCF0 " + translatorService.translate(server, "command.logs.field.configuration.title"))
                            .description(translatorService.translate(server, "command.logs.field.configuration.notyetconfigured"))
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
                            .title("\uD83D\uDCF0 " + translatorService.translate(server, "command.logs.field.configuration.title"))
                            .description(translatorService.translate(server, "command.logs.field.configuration.alreadyconfigured"))
                            .build())
                    .withComponents(ActionRow.of(createTempMenuOptions(server)))
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
                        this.serversService.saveGuildServer(guildServer);

                        return e.deferEdit()
                                .then(e.editReply(translatorService.translate(guildServer, "command.logs.reply.configuration.updated"))
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
        return translatorService.translate(server, "command.logs.description");
    }
    @Override
    public Permission permission() {
        return Permission.ADMINISTRATOR;
    }
}