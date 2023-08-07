package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.models.SlashCommand;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.*;

@Component
public class SuperadminCommand extends SlashCommand<ChatInputInteractionEvent> {
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final User author = event.getInteraction()
                .getUser();

        // TODO: Create a custom permissions system to only allow commands for the owner,
        //  instead of hardcoded this in the command event.
        if (!Utils.isBotOwner(author)) {
            return event.reply("Only the bot owner can use this command.")
                    .withEphemeral(true);
        }

        final var options = event.getOptions().get(0);
        final String optionName = options.getName();

        switch (optionName) {
            case "stop" -> {
                return event.reply("Bot is stopping...")
                        .withEphemeral(false)
                        .doOnSuccess(ignore -> {
                            Logger.log.info("Bot is stopping...");
                            System.exit(0);
                        });
            }
            case "logs" -> {
                return dumpLogs(event);
            }
        }

        Logger.log.error("Failed to get command options: {}", event);
        return event.reply("Failed get command options.")
                .withEphemeral(true);
    }

    private Mono<Void> dumpLogs(ChatInputInteractionEvent event) {
        // Get logs from local file.
        try (BufferedReader br = new BufferedReader(new FileReader(new File("logs/maidbot.log").getAbsolutePath()))) {
            final StringBuilder sb = new StringBuilder();
            String line = br.readLine();

            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            final String content = sb.toString();
            return event.reply("```" + content + "```")
                    .withEphemeral(true);
        } catch (Exception e) {
            Logger.log.error("Failed to read logs file: {}", e.getMessage());
            return event.reply("Failed to read logs file.")
                    .withEphemeral(true);
        }
    }

    @Override
    public String getName() {
        return "superadmin";
    }

    @Override
    // Not really used as these commands are for the owner only,
    // but required by the interface.
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}