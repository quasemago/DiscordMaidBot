package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.helpers.Logger;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import reactor.core.publisher.Mono;

public abstract class MemberJoinListener {
    public Mono<Void> onMemberJoin(final MemberJoinEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> Logger.log.debug("Joined guild " + e.getMember().getUsername()))
                .then();
    }
}
