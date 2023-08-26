package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.UnbanEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

public abstract class MemberUnbanListener {
    @Autowired
    private GuildServerService serversService;

    public Mono<Void> onMemberUnban(final UnbanEvent event) {
        return Mono.just(event)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(e -> {
                    final User member = e.getUser();
                    if (!member.isBot()) {
                        Logger.log.debug("User " + member.getUsername() + " unbanned from guild " + e.getGuildId().asLong());

                        // Check for log system.
                        final GuildServer serverGuild = this.serversService.getGuildServerByGuildId(e.getGuildId().asLong());

                        // Log system is enabled.
                        if (serverGuild != null && serverGuild.getLogChannelId() != null && serverGuild.getLogFlags() != null) {
                            final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
                            if (logTypesSet.contains(LogTypes.MEMBER_UNBAN)) {
                                e.getClient()
                                        .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                                        .ofType(MessageChannel.class)
                                        .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                                .title("♻️ Member Unbanned")
                                                .description(member.getUsername())
                                                .color(Color.SEA_GREEN)
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
                    Logger.log.error("Error on UnbanEvent: " + er.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}