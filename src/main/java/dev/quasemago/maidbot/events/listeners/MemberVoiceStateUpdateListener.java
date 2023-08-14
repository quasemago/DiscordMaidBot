package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.data.repository.ServersRepository;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.models.Servers;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.VoiceStateUpdateEvent;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Objects;

public abstract class MemberVoiceStateUpdateListener {
    @Autowired
    private ServersRepository serversRepository;

    public Mono<Void> onMemberVoiceStateUpdate(final VoiceStateUpdateEvent event) {
        return Mono.just(event)
                .publishOn(Schedulers.boundedElastic())
                .doOnSuccess(e -> {
                    final Member member = e.getCurrent().getMember().block();
                    if (Objects.isNull(member) || member.isBot()) {
                        return;
                    }

                    Logger.log.debug("Member " + member.getUsername() + " updated voice channel state");

                    // Check for log system.
                    final Servers serverGuild = serversRepository.findByGuildId(e.getCurrent().getGuildId().asLong());
                    if (serverGuild == null || serverGuild.getLogChannelId() == null || serverGuild.getLogFlags() == null) {
                        return;
                    }

                    // Log system is enabled.
                    if (e.isJoinEvent()) {
                        onMemberJoinVoiceChannel(e, serverGuild, member);
                    }
                    if (e.isLeaveEvent()) {
                        onMemberLeaveVoiceChannel(e, serverGuild, member);
                    }
                    if (e.isMoveEvent()) {
                        onMemberMoveVoiceChannel(e, serverGuild, member);
                    }
                })
                .onErrorResume(er -> {
                    Logger.log.error("Error on MemberVoiceStateUpdateEvent: " + er.getMessage());
                    return Mono.empty();
                })
                .then();
    }

    private void onMemberJoinVoiceChannel(final VoiceStateUpdateEvent event, final Servers serverGuild, final Member member) {
        final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
        if (logTypesSet.contains(LogTypes.MEMBER_JOIN_VOICE)) {
            final Channel newChannel = event.getCurrent()
                    .getChannel()
                    .block();

            if (newChannel == null) {
                return;
            }

            event.getClient()
                    .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                            .title("\uD83C\uDFA4 Member Joined Voice Channel")
                            .description(member.getUsername())
                            .color(Color.SEA_GREEN)
                            .thumbnail(member.getAvatarUrl())
                            .addField("Channel", newChannel.getMention(), false)
                            .addField("Id", member.getId().asString(), false)
                            .timestamp(Instant.now())
                            .footer(member.getUsername(), null)
                            .build()))
                    .subscribe();
        }
    }

    private void onMemberLeaveVoiceChannel(final VoiceStateUpdateEvent event, final Servers serverGuild, final Member member) {
        final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
        if (logTypesSet.contains(LogTypes.MEMBER_LEAVE_VOICE)) {
            final Channel oldChannel = event.getOld()
                    .map(old -> old.getChannel().block())
                    .orElse(null);

            event.getClient()
                    .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                            .title("\uD83D\uDEAB Member Leave Voice Channel")
                            .description(member.getUsername())
                            .color(Color.RED)
                            .thumbnail(member.getAvatarUrl())
                            .addField("Channel",oldChannel != null ? oldChannel.getMention() : "None", false)
                            .addField("Id", member.getId().asString(), false)
                            .timestamp(Instant.now())
                            .footer(member.getUsername(), null)
                            .build()))
                    .subscribe();
        }
    }

    private void onMemberMoveVoiceChannel(final VoiceStateUpdateEvent event, final Servers serverGuild, final Member member) {
        final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
        if (logTypesSet.contains(LogTypes.MEMBER_JOIN_VOICE)) {
            final Channel newChannel = event.getCurrent()
                    .getChannel()
                    .block();

            if (newChannel == null) {
                return;
            }

            final Channel oldChannel = event.getOld()
                    .map(old -> old.getChannel().block())
                    .orElse(null);

            event.getClient()
                    .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                    .ofType(MessageChannel.class)
                    .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                            .title("\uD83C\uDFA4 Member Move Voice Channel")
                            .description(member.getUsername())
                            .color(Color.YELLOW)
                            .thumbnail(member.getAvatarUrl())
                            .addField("Old Channel", oldChannel != null ? oldChannel.getMention() : "None", true)
                            .addField("New Channel", newChannel.getMention(), true)
                            .addField("Id", member.getId().asString(), false)
                            .timestamp(Instant.now())
                            .footer(member.getUsername(), null)
                            .build()))
                    .subscribe();
        }
    }
}
