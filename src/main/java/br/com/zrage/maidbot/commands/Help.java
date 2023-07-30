package br.com.zrage.maidbot.commands;

import br.com.zrage.maidbot.MaidbotApplication;
import br.com.zrage.maidbot.core.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;

@Component
public class Help extends SlashCommand<ChatInputInteractionEvent> {
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        final var spec = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title("MaidBot Help")
                .timestamp(Instant.now())
                .footer("MaidBot", null);

        StringBuilder description = new StringBuilder();
        if (MaidbotApplication.slashCommandList.isEmpty()) {
            description.append("No commands registered");
        } else {
            MaidbotApplication.slashCommandList.forEach((name, command) -> description.append("/"+name+" = " + command.description() + "\n "));
        }

        final var embed = spec.description(description.toString()).build();
        return event.reply()
                .withEphemeral(true)
                .withEmbeds(embed);
    }

    public String getName() {
        return "help";
    }

    @Override
    public Permission getPermission() {
        return Permission.SEND_MESSAGES;
    }
}