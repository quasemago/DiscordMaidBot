package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.services.GuildServerService;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Component
public class SetlanguageCommand implements SlashCommand {
    @Autowired
    private GuildServerService guildServerService;

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        final MessageChannel channel = event.getInteraction()
                .getChannel()
                .block();

        if (!(channel instanceof PrivateChannel)) {
            final var option = event.getOption("lang")
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .orElse(null);

            if (option == null) {
                return event.reply("Failed to get language code from args.")
                        .withEphemeral(true);
            }

            final Locale locale = Locale.forLanguageTag(option.asString());

            // Update guild database.
            guildServer.setLocale(locale);
            this.guildServerService.save(guildServer);

            return event.reply("Set language to: " + option.asString() + ".")
                    .withEphemeral(true);
        } else {
            return event.reply("This command can't be done in a PM/DM.")
                    .withEphemeral(true);
        }
    }

    @Override
    public String name() {
        return "setlanguage";
    }

    @Override
    public String description() {
        return "[Admin] Change the bot language!";
    }

    @Override
    // Not really used as these commands are for the owner only,
    // but required by the interface.
    public Permission permission() {
        return Permission.ADMINISTRATOR;
    }
}