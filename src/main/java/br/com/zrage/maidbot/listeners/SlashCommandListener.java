package br.com.zrage.maidbot.listeners;

import br.com.zrage.maidbot.Utils;
import br.com.zrage.maidbot.core.SlashCommand;
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

    public Mono<Void> handle(ChatInputInteractionEvent event) {
        // TODO: Improve this.
        return Flux.fromIterable(commands)
                .filter(command -> command.getName().equals(event.getCommandName()))
                .next()
                .filterWhen(command -> Utils.hasPermission(event.getInteraction().getGuild().block(), event.getInteraction().getUser(), command.getPermission()) ? Mono.just(true) : event.reply()
                        .withEphemeral(true)
                        .withContent("You don't have permission to use this command.")
                        .hasElement())
                .flatMap(command -> command.exe(event));
    }
}
