package dev.quasemago.maidbot.listeners;

import dev.quasemago.maidbot.helpers.Logger;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import reactor.core.publisher.Mono;

public abstract class MemberLeaveListener {
    public Mono<Void> onMemberLeave(final MemberLeaveEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> e.getMember()
                        .ifPresent(member -> Logger.log.debug("Leave guild " + member.getUsername())))
                .then();
    }
}
