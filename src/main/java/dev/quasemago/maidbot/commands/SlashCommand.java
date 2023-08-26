package dev.quasemago.maidbot.commands;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class SlashCommand<M extends ChatInputInteractionEvent> {
    public abstract Mono<Void> handle(M event);
    public abstract String name();
    public String description() {
        return null;
    }
    public abstract Permission permission();
}