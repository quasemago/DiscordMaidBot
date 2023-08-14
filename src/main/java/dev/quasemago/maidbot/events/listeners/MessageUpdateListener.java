package dev.quasemago.maidbot.events.listeners;

import dev.quasemago.maidbot.data.repository.ServersRepository;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.models.Servers;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.Instant;

public abstract class MessageUpdateListener {
    @Autowired
    private ServersRepository serversRepository;

    public Mono<Void> onMessageUpdate(final Message event, final Message oldEvent) {
        return Mono.just(event)
                .doOnSuccess(e -> e.getAuthor().ifPresent(author -> {
                    if (!author.isBot()) {
                        Logger.log.debug("MessageUpdateEvent received by " + author.getUsername() + ": {} [old: {}]", e.getContent(), oldEvent != null ? oldEvent.getContent() : "None");

                        // Check for log system.
                        final Snowflake guild = e.getGuildId().orElse(null);
                        if (guild != null) {
                            final Servers serverGuild = serversRepository.findByGuildId(guild.asLong());
                            // Log system is enabled.
                            if (serverGuild != null && serverGuild.getLogChannelId() != null && serverGuild.getLogFlags() != null) {
                                final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
                                if (logTypesSet.contains(LogTypes.EDIT_MESSAGE)) {
                                    e.getClient()
                                            .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                                            .ofType(MessageChannel.class)
                                            .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                                            .title("\uD83D\uDCDD Edited message in <#"+ e.getChannelId().asString() +">")
                                                            .description(author.getUsername())
                                                            .color(Color.LIGHT_SEA_GREEN)
                                                            .addField("Old Message", oldEvent != null ? oldEvent.getContent() : "None", false)
                                                            .addField("New Message", e.getContent(), false)
                                                            .addField("Id", e.getId().asString(), false)
                                                            .timestamp(Instant.now())
                                                            .footer(author.getUsername(), null)
                                                    .build()))
                                            .subscribe();
                                }
                            }
                        }
                    }
                }))
                .onErrorResume(er -> {
                    Logger.log.error("Error on MessageUpdateEvent: " + er.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}