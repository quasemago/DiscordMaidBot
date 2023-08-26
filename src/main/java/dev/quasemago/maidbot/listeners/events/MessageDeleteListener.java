package dev.quasemago.maidbot.listeners.events;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.LogTypes;
import dev.quasemago.maidbot.helpers.LogTypesSet;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class MessageDeleteListener implements GenericEventListener<MessageDeleteEvent> {
    @Autowired
    private GuildServerService serversService;

    @Override
    public Class<MessageDeleteEvent> getEventType() {
        return MessageDeleteEvent.class;
    }

    public Mono<Void> handle(MessageDeleteEvent event) {
        final var message = event.getMessage().orElse(null);
        if (message != null) {
            return Mono.just(event)
                    // Since we already get message from event, we can use it directly.
                    .doOnSuccess(ignore -> message.getAuthor()
                            .ifPresent(author -> {
                                if (!author.isBot()) {
                                    Logger.log.debug("MessageDeleteEvent received by "+ author.getUsername() +": {}", message.getContent());

                                    // Check for log system.
                                    final Snowflake guild = message.getGuildId().orElse(null);
                                    if (guild != null) {
                                        final GuildServer serverGuild = this.serversService.getGuildServerByGuildId(guild.asLong());
                                        // Log system is enabled.
                                        if (serverGuild != null && serverGuild.getLogChannelId() != null && serverGuild.getLogFlags() != null) {
                                            final LogTypesSet logTypesSet = LogTypesSet.of(serverGuild.getLogFlags());
                                            if (logTypesSet.contains(LogTypes.DELETE_MESSAGE)) {
                                                message.getClient()
                                                        .getChannelById(Snowflake.of(serverGuild.getLogChannelId()))
                                                        .ofType(MessageChannel.class)
                                                        .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                                                .title("\uD83D\uDDD1 Message deleted in <#"+ message.getChannelId().asString() +">")
                                                                .description(author.getUsername())
                                                                .color(Color.LIGHT_SEA_GREEN)
                                                                .addField("Content", message.getContent(), false)
                                                                .addField("Id", message.getId().asString(), false)
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
        } else {
            return Mono.empty();
        }
    }
}