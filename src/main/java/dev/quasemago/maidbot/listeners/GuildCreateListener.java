package dev.quasemago.maidbot.listeners;

import dev.quasemago.maidbot.helpers.Logger;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import reactor.core.publisher.Mono;

public abstract class GuildCreateListener {
    public Mono<Void> onGuildCreate(final GuildCreateEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> Logger.log.debug("Joined guild " + e.getGuild().getName() + " (" + e.getGuild().getId().asString() + ")"))
                .then();
    }
}
