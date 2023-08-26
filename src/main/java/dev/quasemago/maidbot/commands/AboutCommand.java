package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class AboutCommand implements SlashCommand {
    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event) {
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
                .description("**" + botName + "** is a simple discord bot project made with java and spring boot.")
                .addField("Created by", MaidBotApplication.GITHUB_AUTHOR +" ([GitHub]("+ MaidBotApplication.GITHUB_AUTHOR_URL +"))", true)
                .addField("Version", MaidBotApplication.VERSION, true)
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
    public Permission permission() {
        return Permission.SEND_MESSAGES;
    }
}