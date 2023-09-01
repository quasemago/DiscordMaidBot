package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.data.models.GuildServer;
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
                .doOnSuccess(e -> e.getGuild()
                        .ifPresent(guild -> {
                            Logger.log.debug("Left guild " + guild.getName() + "[is unavailable: " + e.isUnavailable() + "]");

                            // Don't delete guild from database if unavailable, as this is likely due to a Discord outage.
                            if (!e.isUnavailable()) {
                                // Delete guild from database, if exists.
                                final GuildServer guildServer = this.guildServerService.getGuildServerByGuild(guild);
                                if (guildServer != null) {
                                    this.guildServerService.deleteGuildServer(guildServer);
                                }
                            }
                        }))
                .then();
    }
}