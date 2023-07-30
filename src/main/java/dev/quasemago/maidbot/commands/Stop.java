package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.core.SlashCommand;
import dev.quasemago.maidbot.helpers.Utils;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Stop extends SlashCommand<ChatInputInteractionEvent> {
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            final User author = event.getInteraction()
                    .getUser();

            // TODO: Create a custom permissions system to only allow commands for the owner,
            //  instead of hardcoded this in the command event.
            if (!Utils.isBotOwner(author)) {
                return event.reply("Only the bot owner can use this command.")
                        .withEphemeral(true);
            }

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