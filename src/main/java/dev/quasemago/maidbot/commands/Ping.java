package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.core.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Ping extends SlashCommand<ChatInputInteractionEvent> {
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        return event.reply()
                .withEphemeral(true)
                .withContent("Pong!");
    }

    public String getName() {
        return "ping";
    }

    @Override
    public Permission getPermission() {
        return Permission.SEND_MESSAGES;
    }
}