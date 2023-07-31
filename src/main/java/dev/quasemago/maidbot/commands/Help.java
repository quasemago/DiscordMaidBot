package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.core.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
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
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            final User bot = event.getInteraction()
                    .getClient()
                    .getSelf()
                    .block();

            final String botName = bot.getUsername();

            // TODO: Improve this.
            final var spec = EmbedCreateSpec.builder()
                    .color(Color.GREEN)
                    .title("🤖 " + botName + " Help")
                    .url(MaidBotApplication.GITHUB_URL)
                    .timestamp(Instant.now())
                    .footer(botName, null);

            StringBuilder description = new StringBuilder();
            if (MaidBotApplication.slashCommandList.isEmpty()) {
                description.append("No commands registered");
            } else {
                MaidBotApplication.slashCommandList.forEach((name, command) -> description.append("**/"+name+"** - " + command.description() + "\n "));
            }

            final var embed = spec.description(description.toString()).build();
            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(embed);
        } else {
            return event.reply("This command can't be done in a PM/DM.")
                    .withEphemeral(true);
        }
    }

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public Permission getPermission() {
        return Permission.SEND_MESSAGES;
    }
}