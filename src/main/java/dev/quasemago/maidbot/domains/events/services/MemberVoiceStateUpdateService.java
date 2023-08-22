package dev.quasemago.maidbot.domains.events.services;

import dev.quasemago.maidbot.domains.events.GenericEventInterface;
import dev.quasemago.maidbot.domains.events.listeners.MemberVoiceStateUpdateListener;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberVoiceStateUpdateService extends MemberVoiceStateUpdateListener implements GenericEventInterface<VoiceStateUpdateEvent> {
    @Override
    public Class<VoiceStateUpdateEvent> getEventType() {
        return VoiceStateUpdateEvent.class;
    }
    @Override
    public Mono<Void> execute(final VoiceStateUpdateEvent event) {
        return onMemberVoiceStateUpdate(event);
    }
}
