package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;
import reactor.core.publisher.Mono;

public interface SlashCommand {
    Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer);
    String name();
    String description();
    Permission permission();
}