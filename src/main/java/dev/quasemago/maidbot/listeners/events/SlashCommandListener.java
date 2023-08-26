package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.commands.SlashCommand;
import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class SlashCommandListener implements GenericEventListener<ChatInputInteractionEvent> {
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
            return event.createFollowup("Slash command " + event.getCommandName() + " not found.")
                    .withEphemeral(true)
                    .then();
        } else {
            try {
                if (Utils.hasPermission(event.getInteraction().getGuild().block(), event.getInteraction().getUser(), command.permission())) {
                    return command.handle(event);
                } else {
                    return event.createFollowup("You don't have permission to use this command.")
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
        /*
        return Flux.fromIterable(commands)
                .filter(command -> command.name().equals(event.getCommandName()))
                .next()
                .filterWhen(command -> Utils.hasPermission(event.getInteraction().getGuild().block(), event.getInteraction().getUser(), command.permission()) ? Mono.just(true) : event.reply()
                        .withEphemeral(true)
                        .withContent("You don't have permission to use this command.")
                        .hasElement())
                .flatMap(command -> command.handle(event))
                .doOnSuccess(ignore -> Logger.log.debug("Slash command " + event.getCommandName() + " executed by " + event.getInteraction().getUser().getUsername()));
         */
    }
}
