package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.core.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Stop extends SlashCommand<ChatInputInteractionEvent> {
    // TODO: Create custom permission system to allow only owner.
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            return event.reply()
                    .withEphemeral(false)
                    .withContent("Bot is stopping...")
                    .doOnSuccess(ignore -> {
                        MaidBotApplication.log.info("Bot is stopping...");
                        System.exit(0);
                    });
        } else {
            return event.reply("This command can't be done in a PM/DM.")
                    .withEphemeral(true);
        }
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}