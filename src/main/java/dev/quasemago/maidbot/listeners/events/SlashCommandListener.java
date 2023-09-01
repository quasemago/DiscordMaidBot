package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.commands.SlashCommand;
import dev.quasemago.maidbot.data.BotConfiguration;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import dev.quasemago.maidbot.services.GuildServerService;
import dev.quasemago.maidbot.services.TranslatorService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SlashCommandListener implements GenericEventListener<ChatInputInteractionEvent> {
    @Autowired
    private GuildServerService guildServerService;
    @Autowired
    private TranslatorService translatorService;

    private final List<SlashCommand> commands;

    public SlashCommandListener(List<SlashCommand> slashCommandList) {
        commands = slashCommandList;
    }

    @Override
    public Class<ChatInputInteractionEvent> getEventType() {
        return ChatInputInteractionEvent.class;
    }

    // TODO: This needs to be improved.
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        final var command = commands.stream()
                .filter(c -> c.name().equals(event.getCommandName()))
                .findFirst()
                .orElse(null);

        if (command == null) {
            Logger.log.error("Slash command " + event.getCommandName() + " not found.");
            return event.createFollowup("Slash command "+ event.getCommandName() +" not found.")
                    .withEphemeral(true)
                    .then();
        } else {
            try {
                final Guild guild = event.getInteraction().getGuild().block();
                final GuildServer guildServer = this.guildServerService.getGuildServerByGuild(guild);

                if (Utils.hasPermission(guild, event.getInteraction().getUser(), command.permission())) {
                    return command.handle(event, guildServer);
                } else {
                    return event.createFollowup(translatorService.translate(guildServer, "command_error.restrict.permission"))
                            .withEphemeral(true)
                            .then();
                }
            } catch (Exception e) {
                Logger.log.error("Error while executing slash command " + event.getCommandName() + ": " + e.getMessage());
                return event.createFollowup("Error while executing slash command " + event.getCommandName())
                        .withEphemeral(true)
                        .then();
            }
        }
    }
}
