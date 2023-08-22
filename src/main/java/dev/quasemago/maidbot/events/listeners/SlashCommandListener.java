package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import dev.quasemago.maidbot.models.SlashCommand;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

@Component
public class SlashCommandListener {
    // TODO: Improve this.
    private final Collection<SlashCommand<ChatInputInteractionEvent>> commands;

    public SlashCommandListener(List<SlashCommand<ChatInputInteractionEvent>> slashCommandList, GatewayDiscordClient client) {
        commands = slashCommandList;
        client.on(ChatInputInteractionEvent.class, this::handle).subscribe();
    }

    public Mono<Void> handle(ChatInputInteractionEvent event) {
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .filterWhen(command -> Utils.hasPermission(event.getInteraction().getGuild().block(), event.getInteraction().getUser(), command.getPermission()) ? Mono.just(true) : event.reply()
                        .withEphemeral(true)
                        .withContent("You don't have permission to use this command.")
                        .hasElement())
                .flatMap(command -> command.exe(event))
                .doOnSuccess(ignore -> Logger.log.debug("Slash command " + event.getCommandName() + " executed by " + event.getInteraction().getUser().getUsername()));
    }
}
