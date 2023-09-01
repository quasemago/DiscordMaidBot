package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.data.dto.GuildServerDTO;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.core.event.domain.guild.GuildCreateEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Date;

@Component
public class GuildCreateListener implements GenericEventListener<GuildCreateEvent> {
    @Autowired
    private GuildServerService guildServerService;

    @Override
    public Class<GuildCreateEvent> getEventType() {
        return GuildCreateEvent.class;
    }

    public Mono<Void> handle(GuildCreateEvent event) {
        return Mono.just(event)
                .doOnSuccess(e -> {
                    Logger.log.info("Joined guild " + e.getGuild().getName());

                    // Check if guild exists in database
                    //  if not, create it.
                    final Long guildId = e.getGuild().getId().asLong();
                    final Date now = new Date(System.currentTimeMillis());

                    final GuildServer guildServer = this.guildServerService.getGuildServerByGuildId(guildId);
                    if (guildServer == null) {
                        this.guildServerService.createGuildServer(new GuildServerDTO(guildId, null, null, null, now));
                    } else {
                        guildServer.setLastUpdated(now);
                    }
                })
                .then();
    }
}