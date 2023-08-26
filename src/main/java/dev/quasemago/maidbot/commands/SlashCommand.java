package dev.quasemago.maidbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public interface SlashCommand {
    Mono<Void> handle(ChatInputInteractionEvent event);
    String name();
    default String description() {
        return null;
    }
    Permission permission();
}