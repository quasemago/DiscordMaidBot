package dev.quasemago.maidbot.events.services;

import dev.quasemago.maidbot.events.GenericEventInterface;
import dev.quasemago.maidbot.events.listeners.MemberJoinListener;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberJoinService extends MemberJoinListener implements GenericEventInterface<MemberJoinEvent> {
    @Override
    public Class<MemberJoinEvent> getEventType() {
        return MemberJoinEvent.class;
    }
    @Override
    public Mono<Void> execute(final MemberJoinEvent event) {
        return onMemberJoin(event);
    }
}
