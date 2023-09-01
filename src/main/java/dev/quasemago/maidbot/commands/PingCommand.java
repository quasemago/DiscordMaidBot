package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.services.TranslatorService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PingCommand implements SlashCommand {
    @Autowired
    private TranslatorService translatorService;
    private GuildServer server;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        this.server = guildServer;

        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Pong!");
        } else {
            return event.reply(translatorService.translate(guildServer, "command_error.restrict.dm"))
                    .withEphemeral(true);
        }
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description() {
        return translatorService.translate(server, "command.ping.description");
    }

    @Override
    public Permission permission() {
        return Permission.SEND_MESSAGES;
    }
}