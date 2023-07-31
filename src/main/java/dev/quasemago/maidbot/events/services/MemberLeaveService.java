package dev.quasemago.maidbot.events.services;

import dev.quasemago.maidbot.events.GenericEventInterface;
import dev.quasemago.maidbot.events.listeners.MemberLeaveListener;
import discord4j.core.event.domain.guild.MemberLeaveEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberLeaveService extends MemberLeaveListener implements GenericEventInterface<MemberLeaveEvent> {
    @Override
    public Class<MemberLeaveEvent> getEventType() {
        return MemberLeaveEvent.class;
    }
    @Override
    public Mono<Void> execute(final MemberLeaveEvent event) {
        return onMemberLeave(event);
    }
}