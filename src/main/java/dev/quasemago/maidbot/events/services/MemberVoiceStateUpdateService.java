package dev.quasemago.maidbot.events.services;

import dev.quasemago.maidbot.events.GenericEventInterface;
import dev.quasemago.maidbot.events.listeners.MemberVoiceStateUpdateListener;
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
