package dev.quasemago.maidbot.events;

import discord4j.core.event.domain.Event;
import reactor.core.publisher.Mono;

public interface GenericEventInterface<T extends Event> {
    Class<T> getEventType();
    Mono<Void> execute(T event);

    default Mono<Void> handleError(Throwable error) {
        error.printStackTrace();
        return Mono.empty();
    }
}
