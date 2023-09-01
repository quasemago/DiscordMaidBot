package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.services.GuildServerService;
import dev.quasemago.maidbot.services.TranslatorService;
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
            final var option = event.getOption("lang")
                    .flatMap(ApplicationCommandInteractionOption::getValue)
                    .orElse(null);

            if (option == null) {
                return event.reply(translatorService.translate(guildServer, "command_error.failedtogetoptionsvalue"))
                        .withEphemeral(true);
            }

            final Locale locale = Locale.forLanguageTag(option.asString());

            // Update guild database.
            guildServer.setLocale(locale);
            this.guildServerService.saveGuildServer(guildServer);

            return event.reply(translatorService.translate(guildServer, "command.setlanguage.languageupdated", option.asString()))
                    .withEphemeral(true);
        } else {
            return event.reply(translatorService.translate(guildServer, "command_error.restrict.dm"))
                    .withEphemeral(true);
        }
    }

    @Override
    public String name() {
        return "setlanguage";
    }

    @Override
    public String description() {
        return translatorService.translate(server, "command.setlanguage.description");
    }

    @Override
    public Permission permission() {
        return Permission.ADMINISTRATOR;
    }
}