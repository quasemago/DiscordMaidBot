package br.com.zrage.maidbot.commands;

import br.com.zrage.maidbot.MaidbotApplication;
import br.com.zrage.maidbot.core.SlashCommand;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.rest.util.Permission;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public class Stop extends SlashCommand<ChatInputInteractionEvent> {
    @Override
    public Mono<Void> exe(ChatInputInteractionEvent event) {
        return event.reply()
                .withEphemeral(false)
                .withContent("Bot is stopping...")
                .doOnSuccess(ignore -> {
                    MaidbotApplication.log.info("Bot is stopping...");
                    System.exit(0);
                });
    }

    @Override
    public String getName() {
        return "stop";
    }

    @Override
    public Permission getPermission() {
        return Permission.ADMINISTRATOR;
    }
}