package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.guild.MemberJoinEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;

public abstract class MemberJoinListener {
    @Autowired
    private GuildServerService serversService;

    public Mono<Void> onMemberJoin(final MemberJoinEvent event) {
        return Mono.just(event)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(e -> {
                    final Member member = e.getMember();
                    if (!member.isBot()) {
                        Logger.log.debug("Joined guild " + member.getUsername());

                        // Check for log system.
                        final GuildServer serverGuild = this.serversService.getGuildServerByGuildId(e.getGuildId().asLong());

                        // Log system is enabled.
                        if (serverGuild != null && serverGuild.getLogChannelId() != null && serverGuild.getLogFlags() != null) {
                            final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
                            if (logTypesSet.contains(LogTypes.MEMBER_JOIN)) {
                                e.getClient()
                                        .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                                        .ofType(MessageChannel.class)
                                        .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                                .title("✅ Member Joined")
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
                    Logger.log.error("Error on MemberJoinEvent: " + er.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}
