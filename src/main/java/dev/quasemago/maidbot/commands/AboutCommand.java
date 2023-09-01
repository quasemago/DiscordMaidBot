package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.services.TranslatorService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component

public class AboutCommand implements SlashCommand {
    @Autowired
    private TranslatorService translatorService;
    private GuildServer server;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        this.server = guildServer;

        final User bot = event.getInteraction()
                .getClient()
                .getSelf()
                .block();

        final String botName = bot.getUsername();

        // TODO: Add more information (e.g. uptime, server count, etc).
        final EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(86, 139, 255))
                .title("🤖 " + botName + " About")
                .url(MaidBotApplication.GITHUB_URL)
                .description(translatorService.translate(guildServer, "command.about.field.description", botName))
                .addField(translatorService.translate(guildServer, "command.about.field.createdby"), MaidBotApplication.GITHUB_AUTHOR +" ([GitHub]("+ MaidBotApplication.GITHUB_AUTHOR_URL +"))", true)
                .addField(translatorService.translate(guildServer, "command.about.field.version"), MaidBotApplication.VERSION, true)
                .timestamp(Instant.now())
                .footer("🤖 BOT ID: " + bot.getId().asString(), null)
                .build();

        return event.reply()
                .withEphemeral(false)
                .withEmbeds(embed);
    }

    @Override
    public String name() {
        return "about";
    }

    @Override
    public String description() {
        return translatorService.translate(server, "command.about.description");
    }

    @Override
    public Permission permission() {
        return Permission.SEND_MESSAGES;
    }
}