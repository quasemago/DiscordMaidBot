package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.helpers.Utils;
import dev.quasemago.maidbot.services.GuildServerService;
import dev.quasemago.maidbot.services.TranslatorService;
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
public class SuperadminCommand implements SlashCommand {
    @Autowired
    private GuildServerService guildServerService;
    @Autowired
    private TranslatorService translatorService;
    private GuildServer server;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        this.server = guildServer;

        final User author = event.getInteraction()
                .getUser();

        // TODO: Create a custom permissions system to only allow commands for the owner,
        //  instead of hardcoded this in the command event.
        if (!Utils.isBotOwner(author)) {
            return event.reply(translatorService.translate(guildServer, "command_error.restrict.owner"))
                    .withEphemeral(true);
        }

        final var options = event.getOptions().get(0);
        final String optionName = options.getName();

        switch (optionName) {
            case "stop" -> {
                return event.reply(translatorService.translate(guildServer, "command.superadmin.stoppingbot"))
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
        return event.reply(translatorService.translate(guildServer, "command_error.failedtogetoptions"))
                .withEphemeral(true);
    }

    private Mono<Void> dumpLogs(ChatInputInteractionEvent event) {
        // Get last 150 lines of local logs.
        final List<String> logsLines = Utils.readLastLine(new File("logs/maidbot.log"), 150);
        if (logsLines.size() == 0) {
            return event.reply(translatorService.translate(server, "command.superadmin.reply.failed.readlogs"))
                    .withEphemeral(true);
        }

        final StringBuilder sb = new StringBuilder();
        logsLines.forEach(line -> sb.append(line).append(System.lineSeparator()));

        final String logs = sb.toString();

        // Check if the logs are longer than 1750 characters.
        if (logs.length() > 1750) {
            final MessageChannel privateChannel = event.getInteraction()
                    .getUser()
                    .getPrivateChannel()
                    .block();

            if (privateChannel == null) {
                return event.reply(translatorService.translate(server, "command.superadmin.reply.failed.getdmchannel"))
                        .withEphemeral(true);
            }

            return event.reply(translatorService.translate(server, "command.superadmin.reply.senttodm"))
                    .withEphemeral(true)
                    .then(privateChannel.createMessage(msg ->
                            msg.addFile("maidbot.log", new ByteArrayInputStream(logs.getBytes()))))
                    .then();
        } else {
            // Since the logs are less than 1750 characters, we can just send it in one message.
            return event.reply(translatorService.translate(server, "command.superadmin.reply.logs") + " ```" + logs + "```")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> leaveGuild(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option) {
        final var guildOption = option.getOption("guildid")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .orElse(null);

        if (guildOption == null) {
            return event.reply(translatorService.translate(server, "command_error.failedtogetoptionsvalue"))
                    .withEphemeral(true);
        }

        final Snowflake guildId = Snowflake.of(guildOption.asString());
        final Guild guild = event.getClient()
                .getGuildById(guildId)
                .block();

        if (guild == null) {
            return event.reply(translatorService.translate(server, "command.superadmin.reply.failed.getguild", guildId.asLong()))
                    .withEphemeral(true);
        }

        // Delete guild server data from database.
        final GuildServer targetGuildServer = this.guildServerService.getGuildServerByGuildId(guildId.asLong());
        if (targetGuildServer != null) {
            this.guildServerService.deleteGuildServer(targetGuildServer);
        }

        return event.reply(translatorService.translate(server, "command.superadmin.reply.leaving", guild.getName(), guildId.asLong()))
                .withEphemeral(true)
                .then(guild.leave())
                .then();
    }

    @Override
    public String name() {
        return "superadmin";
    }

    @Override
    public String description() {
        return translatorService.translate(server, "command.superadmin.description");
    }

    @Override
    // Not really used as these commands are for the owner only,
    // but required by the interface.
    public Permission permission() {
        return Permission.ADMINISTRATOR;
    }
}