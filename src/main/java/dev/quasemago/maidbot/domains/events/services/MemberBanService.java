package dev.quasemago.maidbot.domains.events.services;

import dev.quasemago.maidbot.domains.events.GenericEventInterface;
import dev.quasemago.maidbot.domains.events.listeners.MemberBanListener;
import discord4j.core.event.domain.guild.BanEvent;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class MemberBanService extends MemberBanListener implements GenericEventInterface<BanEvent> {
    @Override
    public Class<BanEvent> getEventType() {
        return BanEvent.class;
    }
    @Override
    public Mono<Void> execute(final BanEvent event) {
        return onMemberBan(event);
    }
}