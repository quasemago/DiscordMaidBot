package dev.quasemago.maidbot.domains.events.services;

import dev.quasemago.maidbot.domains.events.GenericEventInterface;
import dev.quasemago.maidbot.domains.events.listeners.MessageCreateListener;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MessageCreateService extends MessageCreateListener implements GenericEventInterface<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }
    @Override
    public Mono<Void> execute(final MessageCreateEvent event) {
        return onMessageCreate(event.getMessage());
    }
}