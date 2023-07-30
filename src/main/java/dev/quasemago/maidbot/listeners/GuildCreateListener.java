package dev.quasemago.maidbot.listeners;

import dev.quasemago.maidbot.MaidBotApplication;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import reactor.core.publisher.Mono;

public abstract class GuildCreateListener {
    public Mono<Void> onGuildCreate(final GuildCreateEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> MaidBotApplication.log.info("Joined guild " + e.getGuild().getName() + " (" + e.getGuild().getId().asString() + ")"))
                .then();
    }
}
