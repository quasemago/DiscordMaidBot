package dev.quasemago.maidbot.commands;

import dev.quasemago.maidbot.data.dto.MemesDTO;
import dev.quasemago.maidbot.data.models.GuildServer;
import dev.quasemago.maidbot.data.models.Memes;
import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.services.GuildServerService;
import dev.quasemago.maidbot.services.MemesService;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.ChatInputInteractionEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.PrivateChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import discord4j.rest.util.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

@Component
public class MemesCommand implements SlashCommand {
    @Autowired
    private MemesService memesService;
    @Autowired
    private GuildServerService serversService;
    private final Pattern numericPattern = Pattern.compile("-?\\d+(\\.\\d+)?");

    @Override
    public Mono<Void> handle(ChatInputInteractionEvent event, GuildServer guildServer) {
        final MessageChannel channel = event.getInteraction().
                getChannel().
                block();

        if (!(channel instanceof PrivateChannel)) {
            // Get guild id from event.
            final Snowflake guildId = event.getInteraction()
                    .getGuildId()
                    .orElse(null);

            if (guildId == null) {
                Logger.log.error("Failed to get guild id from event: {}", event);
                return event.reply()
                        .withEphemeral(true)
                        .withContent("Failed to get memes list.");
            }

            // TODO: This needs to be improved.
            final var options = event.getOptions().get(0);
            final String optionName = options.getName();
            switch (optionName) {
                case "add" -> {
                    return addMeme(event, options, guildServer);
                }
                case "list" -> {
                    return listMemes(event, guildServer);
                }
                case "get" -> {
                    return getMeme(event, options, guildServer);
                }
                case "random" -> {
                    return randomMeme(event, guildServer);
                }
            }

            Logger.log.error("Failed to get command options: {}", event);
            return event.reply("Failed get command options.")
                    .withEphemeral(true);
        } else {
            return event.reply("This command can only be used in a server channel.")
                    .withEphemeral(true);
        }
    }

    private Mono<Void> addMeme(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option, GuildServer guildServer) {
        final String url = option.getOption("url")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asString();

        final String name = option.getOption("name")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asString();

        // Create new meme and save database.
        final Date date = new Date(System.currentTimeMillis());
        final Memes meme = this.memesService.createMeme(new MemesDTO(name, url, date, date, guildServer));

        // Send embed to channel.
        event.getInteraction()
                .getChannel()
                .flatMap(channel -> channel.createMessage(EmbedCreateSpec.builder()
                                .title("["+ meme.getId() +"] " + meme.getName())
                                .image(meme.getUrl())
                        .build()))
                .block();

        // Reply to user.
        return event.reply()
                .withEphemeral(true)
                .withContent("Meme added!");
    }

    private Mono<Void> listMemes(ChatInputInteractionEvent event, GuildServer guildServer) {
        // Try get bot user.
        final User bot = event.getInteraction()
                .getClient()
                .getSelf()
                .block();

        if (bot == null) {
            Logger.log.error("Failed to get bot user from event: {}", event);
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Failed to get memes list.");
        }

        // Get memes list from database.
        final Iterable<Memes> memesList = this.memesService.getAllMemesByGuild(guildServer);

        // Create embed to reply.
        final var spec = EmbedCreateSpec.builder()
                .color(Color.GREEN)
                .title(bot.getUsername() + " Memes")
                .timestamp(Instant.now())
                .footer(bot.getUsername(), null);

        final StringBuilder description = new StringBuilder();
        memesList.forEach(meme -> description.append("**["+meme.getId()+"]** " + meme.getName() + "\n"));

        final var embed = spec.description(description.toString()).build();
        return event.reply()
                .withEphemeral(true)
                .withEmbeds(embed);
    }

    private Mono<Void> getMeme(ChatInputInteractionEvent event, ApplicationCommandInteractionOption option, GuildServer guildServer) {
        final String search = option.getOption("id")
                .flatMap(ApplicationCommandInteractionOption::getValue)
                .get()
                .asString();

        // Get meme from database.
        final Memes meme;
        if (!numericPattern.matcher(search).matches()) {
            meme = this.memesService.getMemeByNameAndGuild(search, guildServer);
        } else {
            meme = this.memesService.getMemeByIdAndGuild(Long.parseLong(search), guildServer);
        }

        if (meme == null) {
            return event.reply()
                    .withEphemeral(true)
                    .withContent("Meme not found!");
        }

        // Send embed to user.
        return event.reply()
                .withEphemeral(false)
                .withEmbeds(EmbedCreateSpec.builder()
                        .title("["+ meme.getId() +"] " + meme.getName())
                        .image(meme.getUrl())
                        .build());
    }

    private Mono<Void> randomMeme(ChatInputInteractionEvent event, GuildServer guildServer) {
        // Get memes list from database.
        final Iterable<Memes> memesList = this.memesService.getAllMemesByGuild(guildServer);

        // Convert iterable to list and pick a random meme.
        final List<Memes> memes = StreamSupport.stream(memesList.spliterator(), false).toList();
        final Memes meme = memes.get(new Random().nextInt(memes.size()));

        // Send embed to user.
        return event.reply()
                .withEphemeral(false)
                .withEmbeds(EmbedCreateSpec.builder()
                        .title("["+ meme.getId() +"] " + meme.getName())
                        .image(meme.getUrl())
                        .build());
    }

    @Override
    public String name() {
        return "memes";
    }

    @Override
    public String description() {
        return "System to save those sent memes on the server";
    }

    @Override
    public Permission permission() {
        return Permission.SEND_MESSAGES;
    }
}