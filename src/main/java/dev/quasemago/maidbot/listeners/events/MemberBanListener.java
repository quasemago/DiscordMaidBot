package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.BanEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Objects;

@Component
public class MemberBanListener implements GenericEventListener<BanEvent> {
    @Autowired
    private GuildServerService serversService;

    @Override
    public Class<BanEvent> getEventType() {
        return BanEvent.class;
    }

    public Mono<Void> handle(BanEvent event) {
        return Mono.just(event)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(e -> {
                    final User member = e.getUser();
                    if (!member.isBot()) {
                        Logger.log.debug("Banned user " + member.getUsername() + " from guild " + Objects.requireNonNull(e.getGuild().block()).getName());

                        // Check for log system.
                        final GuildServer serverGuild = this.serversService.getGuildServerByGuildId(e.getGuildId().asLong());

                        // Log system is enabled.
                        if (serverGuild != null && serverGuild.getLogChannelId() != null && serverGuild.getLogFlags() != null) {
                            final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
                            if (logTypesSet.contains(LogTypes.MEMBER_BAN)) {
                                e.getClient()
                                        .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                                        .ofType(MessageChannel.class)
                                        .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                                .title("\uD83D\uDEAB Member Banned")
                                                .description(member.getUsername())
                                                .color(Color.RED)
                                                .thumbnail(member.getAvatarUrl())
                                                .addField("Id", member.getId().asString(), false)
                                                .timestamp(Instant.now())
                                                .footer(member.getUsername(), null)
                                                .build()))
                                        .subscribe();
                            }
                        }
                    }
                })
                .onErrorResume(er -> {
                    Logger.log.error("Error on BanEvent: " + er.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}