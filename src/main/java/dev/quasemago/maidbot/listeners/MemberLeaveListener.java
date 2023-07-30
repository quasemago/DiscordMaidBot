package dev.quasemago.maidbot.listeners;

import dev.quasemago.maidbot.MaidBotApplication;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import reactor.core.publisher.Mono;

public abstract class MemberLeaveListener {
    public Mono<Void> onMemberLeave(final MemberLeaveEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> e.getMember()
                        .ifPresent(member -> MaidBotApplication.log.info("Leave guild " + member.getUsername())))
                .then();
    }
}
