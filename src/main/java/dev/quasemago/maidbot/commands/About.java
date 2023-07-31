package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.core.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class About extends SlashCommand<ChatInputInteractionEvent> {
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final User bot = event.getInteraction()
                .getClient()
                .getSelf()
                .block();

        final String botName = bot.getUsername();

        final EmbedCreateSpec embed = EmbedCreateSpec.builder()
                .color(Color.of(86, 139, 255))
                .title("🤖 " + botName + " About")
                .url("https://github.com/quasemago/DiscordMaidBot")
                .description("**" + botName + "** is a simple discord bot project made with java and spring boot.")
                .addField("Created by", "mxronning on discord ([GitHub](https://github.com/quasemago))", true)
                .addField("Version", MaidBotApplication.VERSION, true)
                .timestamp(Instant.now())
                .footer("🤖 BOT ID: " + bot.getId().asString(), null)
                .build();

        return event.reply()
                .withEphemeral(false)
                .withEmbeds(embed);
    }

    @Override
    public String getName() {
        return "about";
    }

    @Override
    public Permission getPermission() {
        return Permission.SEND_MESSAGES;
    }
}