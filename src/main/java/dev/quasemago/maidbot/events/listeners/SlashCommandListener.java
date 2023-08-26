package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.commands.SlashCommand;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
public class SlashCommandListener {
    private final Collection<SlashCommand<ChatInputInteractionEvent>> commands;

    public SlashCommandListener(List<SlashCommand<ChatInputInteractionEvent>> slashCommandList, GatewayDiscordClient client) {
        commands = slashCommandList;
        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    // TODO: This needs to be improved.
    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.name().equals(event.getCommandName()))
                .next()
                .filterWhen(command -> Utils.hasPermission(event.getInteraction().getGuild().block(), event.getInteraction().getUser(), command.permission()) ? Mono.just(true) : event.reply()
                        .withEphemeral(true)
                        .withContent("You don't have permission to use this command.")
                        .hasElement())
                .flatMap(command -> command.handle(event))
                .doOnSuccess(ignore -> Logger.log.debug("Slash command " + event.getCommandName() + " executed by " + event.getInteraction().getUser().getUsername()));
    }
}
