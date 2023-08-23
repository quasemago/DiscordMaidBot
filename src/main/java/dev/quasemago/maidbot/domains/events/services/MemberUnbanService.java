package dev.quasemago.maidbot.domains.events.services;

import dev.quasemago.maidbot.domains.events.GenericEventInterface;
import dev.quasemago.maidbot.domains.events.listeners.MemberUnbanListener;
import discord4j.core.event.domain.guild.UnbanEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberUnbanService extends MemberUnbanListener implements GenericEventInterface<UnbanEvent> {
    @Override
    public Class<UnbanEvent> getEventType() {
        return UnbanEvent.class;
    }
    @Override
    public Mono<Void> execute(final UnbanEvent event) {
        return onMemberUnban(event);
    }
}