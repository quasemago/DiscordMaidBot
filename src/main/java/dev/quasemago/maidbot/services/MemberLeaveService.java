package dev.quasemago.maidbot.services;

import dev.quasemago.maidbot.listeners.EventListener;
import dev.quasemago.maidbot.listeners.MemberLeaveListener;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberLeaveService extends MemberLeaveListener implements EventListener<MemberLeaveEvent> {
    @Override
    public Class<MemberLeaveEvent> getEventType() {
        return MemberLeaveEvent.class;
    }
    @Override
    public Mono<Void> execute(final MemberLeaveEvent event) {
        return onMemberLeave(event);
    }
}
