package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.domains.events.GenericEventInterface;
import dev.quasemago.maidbot.domains.events.listeners.GuildCreateListener;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class GuildCreateService extends GuildCreateListener implements GenericEventInterface<GuildCreateEvent> {
    @Override
    public Class<GuildCreateEvent> getEventType() {
        return GuildCreateEvent.class;
    }
    @Override
    public Mono<Void> execute(final GuildCreateEvent event) {
        return onGuildCreate(event);
    }
}
