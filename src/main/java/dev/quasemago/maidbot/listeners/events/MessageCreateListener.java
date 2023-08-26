package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.listeners.GenericEventListener;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class MessageCreateListener implements GenericEventListener<MessageCreateEvent> {
    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    public Mono<Void> handle(MessageCreateEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> {
                    final Message message = e.getMessage();
                    message.getAuthor()
                            .ifPresent(member -> Logger.log.debug("MessageCreateEvent received by "+ member.getUsername() +": {}", message.getContent()));
                })
                .then();
    }
}