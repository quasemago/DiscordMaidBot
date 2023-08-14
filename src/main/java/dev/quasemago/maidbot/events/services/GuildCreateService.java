package dev.quasemago.maidbot.events.services;

import dev.quasemago.maidbot.events.GenericEventInterface;
import dev.quasemago.maidbot.events.listeners.GuildCreateListener;
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
