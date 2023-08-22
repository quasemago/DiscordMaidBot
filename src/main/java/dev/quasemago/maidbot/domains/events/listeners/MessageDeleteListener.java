package dev.quasemago.maidbot.domains.events.listeners;

import dev.quasemago.maidbot.domains.models.LogTypes;
import dev.quasemago.maidbot.domains.models.LogTypesSet;
import dev.quasemago.maidbot.domains.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;

import java.time.Instant;

public abstract class MessageDeleteListener {
    @Autowired
    private GuildServerService serversService;

    public Mono<Void> onMessageDelete(final Message event) {
        return Mono.just(event)
                .doOnSuccess(e -> e.getAuthor().ifPresent(author -> {
                    if (!author.isBot()) {
                        Logger.log.debug("MessageDeleteEvent received by "+ author.getUsername() +": {}", e.getContent());

                        // Check for log system.
                        final Snowflake guild = e.getGuildId().orElse(null);
                        if (guild != null) {
                            final GuildServer serverGuild = this.serversService.getGuildServerByGuildId(guild.asLong());
                            // Log system is enabled.
                            if (serverGuild != null && serverGuild.getLogChannelId() != null && serverGuild.getLogFlags() != null) {
                                final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
                                if (logTypesSet.contains(LogTypes.DELETE_MESSAGE)) {
                                    e.getClient()
                                            .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                                            .ofType(MessageChannel.class)
                                            .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                                    .title("\uD83D\uDDD1 Message deleted in <#"+ e.getChannelId().asString() +">")
                                                    .description(author.getUsername())
                                                    .color(Color.LIGHT_SEA_GREEN)
                                                    .addField("Content", e.getContent(), false)
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
                    Logger.log.error("Error on MessageDeleteEvent: " + er.getMessage());
                    return Mono.empty();
                })
                .then();
    }
}