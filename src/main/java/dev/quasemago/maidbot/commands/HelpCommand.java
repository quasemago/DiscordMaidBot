package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.MaidBotApplication;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.models.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.rest.RestClient;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Component
public class HelpCommand extends SlashCommand<ChatInputInteractionEvent> {
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

            final RestClient restClient = event.getInteraction()
                    .getClient()
                    .getRestClient();
            final long applicationId = restClient.getApplicationId().block();

            Map<String, String> globalCommandList = restClient
                    .getApplicationService()
                    .getGlobalApplicationCommands(applicationId)
                    .collectMap(ApplicationCommandData::name, ApplicationCommandData::description)
                    .block();

            if (globalCommandList == null) {
                Logger.log.error("Error getting commands list at event. {}", event);
                return event.reply("Error getting commands list.")
                        .withEphemeral(true);
            }

            StringBuilder descriptionBuilder = new StringBuilder();
            if (globalCommandList.isEmpty()) {
                descriptionBuilder.append("No commands registered");
            } else {
                globalCommandList.forEach((name, description) -> {
                    descriptionBuilder.append("**/"+name+"** - " + description + "\n ");
                });
            }

            return event.reply()
                    .withEphemeral(true)
                    .withEmbeds(spec.
                            description(descriptionBuilder.toString())
                            .build());
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