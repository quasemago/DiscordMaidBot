package dev.quasemago.maidbot.domains.commands;

import dev.quasemago.maidbot.domains.models.GuildServer;
import dev.quasemago.maidbot.domains.models.SlashCommand;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.List;

@Component
public class SuperadminCommand extends SlashCommand<ChatInputInteractionEvent> {
    @Autowired
    private GuildServerService guildServerService;

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
            case "leaveguild" -> {
                return leaveGuild(event, options);
            }
        }

        Logger.log.error("Failed to get command options: {}", event);
        return event.reply("Failed get command options.")
                .withEphemeral(true);
    }

    private Mono<Void> dumpLogs(ChatInputInteractionEvent event) {
        // Get last 100 lines of local logs.
        final List<String> logsLines = Utils.readLastLine(new File("logs/maidbot.log"), 150);
        if (logsLines.size() == 0) {
            return event.reply("Failed to read logs file.")
                    .withEphemeral(true);
        }

        final StringBuilder sb = new StringBuilder();
        logsLines.forEach(line -> sb.append(line).append(System.lineSeparator()));

        final String logs = sb.toString();

        if (logs.length() > 1750) {
            // Send the first message with the logs.
            final MessageChannel channel = event.getInteraction()
                    .getUser()
                    .getPrivateChannel()
                    .block();

            if (channel == null) {
                return event.reply("Failed to get your private channel interaction. Try again!")
                        .withEphemeral(true);
            }

            return event.reply("The last 150 lines of the logs have been sent to your DM!")
                    .withEphemeral(true)
                    .then(channel.createMessage(msg ->
                            msg.addFile("maidbot.log", new ByteArrayInputStream(logs.getBytes()))))
                    .then();
        } else {
            // Since the logs are less than 2000 characters, we can just send it in one message.
            return event.reply("The last 150 lines of the logs: ```" + logs + "```")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> leaveGuild(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option) {
        final var guildOption = option.getOption("guildid")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .orElse(null);

        if (guildOption == null) {
            return event.reply("Failed to get guild ID from subcommand args.")
                    .withEphemeral(true);
        }

        final Snowflake guildId = Snowflake.of(guildOption.asString());

        final Guild guild = event.getClient()
                .getGuildById(guildId)
                .block();

        if (guild == null) {
            return event.reply("Failed to get guild with ID: " + guildId.asLong())
                    .withEphemeral(true);
        }

        // Delete guild server data from database.
        final GuildServer guildServer = this.guildServerService.getGuildServerByGuildId(guildId.asLong());
        if (guildServer != null) {
            this.guildServerService.deleteGuildServer(guildServer);
        }

        return event.reply("Leaving guild: " + guild.getName() + " [ID: " + guild.getId().asLong() + "]")
                .withEphemeral(true)
                .then(guild.leave())
                .then();
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