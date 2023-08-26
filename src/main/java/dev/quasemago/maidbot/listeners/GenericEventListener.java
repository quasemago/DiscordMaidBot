package dev.quasemago.maidbot.listeners;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface GenericEventListener<T extends Event> {
    Class<T> getEventType();
    Mono<Void> handle(T event);
    default Mono<Void> handleError(Throwable error) {
        error.printStackTrace();
        return Mono.empty();
    }
}
