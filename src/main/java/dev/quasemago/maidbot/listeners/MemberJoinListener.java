package dev.quasemago.maidbot.listeners;

import dev.quasemago.maidbot.MaidBotApplication;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import reactor.core.publisher.Mono;

public abstract class MemberJoinListener {
    public Mono<Void> onMemberJoin(final MemberJoinEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> MaidBotApplication.log.info("Joined guild " + e.getMember().getUsername()))
                .then();
    }
}
