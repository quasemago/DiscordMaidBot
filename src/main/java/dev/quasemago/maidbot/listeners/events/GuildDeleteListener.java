package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.core.event.domain.guild.GuildDeleteEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class GuildDeleteListener implements GenericEventListener<GuildDeleteEvent> {
    @Autowired
    private GuildServerService guildServerService;

    @Override
    public Class<GuildDeleteEvent> getEventType() {
        return GuildDeleteEvent.class;
    }

    public Mono<Void> handle(GuildDeleteEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> {
                    e.getGuild()
                            .ifPresent(guild -> {
                                Logger.log.info("Left guild " + guild.getName());

                                // Delete guild from database, if exists.
                                this.guildServerService.deleteGuildServerById(guild.getId().asLong());
                            });
                })
                .then();
    }
}