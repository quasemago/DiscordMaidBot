package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.helpers.Utils;
import dev.quasemago.maidbot.services.TranslatorService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.List;

@Component
public class HelpCommand implements SlashCommand {
    @Autowired
    private List<SlashCommand> slashCommandList;
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
            final Guild guild = event.getInteraction()
                    .getGuild()
                    .block();

            final User author = event.getInteraction()
                    .getUser();

            // TODO: This really needs to be improved,
            //  but for now, it's fine.
            final StringBuilder commands = new StringBuilder();
            slashCommandList.stream()
                    .filter(c -> Utils.hasPermission(guild, author, c.permission()))
                    .forEach(c -> commands.append("> ``/" + c.name() + "`` - " + c.description() + "\n"));

            final User bot = event.getInteraction()
                    .getClient()
                    .getSelf()
                    .block();

            final String botName = bot.getUsername();

            final var spec = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("🤖 " + botName + " Help")
                    .url(MaidBotApplication.GITHUB_URL)
                    .timestamp(Instant.now())
                    .footer(botName, null);

            spec.addField(translatorService.translate(guildServer, "command.help.field.title"), commands.toString(), false);
            final var embed = spec.build();

            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(embed);
        } else {
            return event.reply(translatorService.translate(guildServer, "command_error.restrict.dm"))
                    .withEphemeral(true);
        }
    }

    @Override
    public String name() {
        return "help";
    }

    @Override
    public String description() {
        return translatorService.translate(server, "command.help.description");
    }

    @Override
    public Permission permission() {
        return Permission.SEND_MESSAGES;
    }
}