package dev.quasemago.maidbot.domains.events.services;

import dev.quasemago.maidbot.domains.events.GenericEventInterface;
import dev.quasemago.maidbot.domains.events.listeners.MessageDeleteListener;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageDeleteService extends MessageDeleteListener implements GenericEventInterface<MessageDeleteEvent> {
    @Override
    public Class<MessageDeleteEvent> getEventType() {
        return MessageDeleteEvent.class;
    }
    @Override
    public Mono<Void> execute(final MessageDeleteEvent event) {
        final var message = event.getMessage().orElse(null);
        if (message != null) {
            return onMessageDelete(message);
        } else {
            return Mono.empty();
        }
    }
}
