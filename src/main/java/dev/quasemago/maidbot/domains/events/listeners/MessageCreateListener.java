package dev.quasemago.maidbot.domains.events.listeners;

import dev.quasemago.maidbot.helpers.Logger;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

public abstract class MessageCreateListener {
    public Mono<Void> onMessageCreate(final Message event) {
        return Mono.just(event)
                .doOnSuccess(e -> e.getAuthor()
                        .ifPresent(member -> Logger.log.debug("MessageCreateEvent received by "+ member.getUsername() +": {}", e.getContent())))
                .then();
    }
}