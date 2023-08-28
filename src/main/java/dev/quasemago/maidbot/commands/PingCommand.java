package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class PingCommand implements SlashCommand {
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Pong!");
        } else {
            return event.reply("This command can't be done in a PM/DM.")
                    .withEphemeral(true);
        }
    }

    @Override
    public String name() {
        return "ping";
    }

    @Override
    public String description() {
        return "Pong!";
    }

    @Override
    public Permission permission() {
        return Permission.SEND_MESSAGES;
    }
}