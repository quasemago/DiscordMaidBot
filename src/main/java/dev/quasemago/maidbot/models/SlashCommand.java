package dev.quasemago.maidbot.models;

import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public abstract class SlashCommand<M extends ChatInputInteractionEvent> {
    public abstract Mono<Void> exe(M event);
    public abstract String getName();
    public String getDescription() {
        return null;
    }
    public abstract Permission getPermission();
}
