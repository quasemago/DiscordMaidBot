package dev.quasemago.maidbot.data;

import dev.quasemago.maidbot.listeners.GenericEventListener;
import dev.quasemago.maidbot.helpers.Logger;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.ClientActivity;
import discord4j.core.object.presence.ClientPresence;
import discord4j.gateway.GatewayReactorResources;
import discord4j.gateway.intent.IntentSet;
import discord4j.rest.RestClient;
import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Configuration
public class BotConfiguration {
    private static final Dotenv dotenv = Dotenv.load();

    public String getBotToken() {
        return dotenv.get("BOT_TOKEN");
    }
    public  String getBotDefaultPresence() {
        return dotenv.get("BOT_DEFAULT_PRESENCE");
    }
    public static Snowflake getBotOwner() {
        return Snowflake.of(dotenv.get("BOT_OWNER"));
    }
    public static String getBotDatabaseUrl() {
        return dotenv.get("BOT_DATABASE_URL");
    }
    public static Locale getBotDefaultLanguage() {
        final String defaultLanguage = dotenv.get("BOT_DEFAULT_LANGUAGE");
        if (defaultLanguage == null || defaultLanguage.isEmpty()) {
            return Locale.ENGLISH;
        }
        return Locale.forLanguageTag(defaultLanguage);
    }

    @Bean
    public <T extends Event>GatewayDiscordClient gatewayDiscordClient(final List<GenericEventListener<T>> eventListenerList) {
        if (this.getBotToken() == null || this.getBotToken().isEmpty()) {
            Logger.log.fatal("Bot token is empty, please set BOT_TOKEN environment variable!");
            System.exit(0);
        }

        // Login do discord gateway.
        final GatewayDiscordClient gateway = DiscordClient.create(this.getBotToken())
                .gateway()
                .setEnabledIntents(IntentSet.all())
                .setGatewayReactorResources(resources -> GatewayReactorResources.builder(resources)
                        .httpClient(HttpClient.create(ConnectionProvider.newConnection())
                                .compress(true)
                                .followRedirect(true)
                                .secure())
                        .build())
                .login()
                .block();

        if (gateway == null) {
            Logger.log.fatal("Failed to login into discord gateway!");
            System.exit(0);
        }

        // Update bot presence.
        final List<String> presence = Arrays.stream(this.getBotDefaultPresence()
                        .split(";"))
                .toList();

        if (presence.size() < 2) {
            Logger.log.fatal("Invalid default presence format, please set BOT_DEFAULT_PRESENCE environment variable! Correct usage: \"Type;Text;Url (use # if isn't applicable)\"");
            System.exit(0);
        }

        gateway.updatePresence(ClientPresence
                        .online(ClientActivity.of(Activity.Type.of(Integer.parseInt(presence.get(0))),
                                presence.get(1),
                                presence.size() == 2 ? null : presence.get(2)))) // Check if url is present.
                .block();

        // Hook events.
        for (final GenericEventListener<T> listener : eventListenerList) {
            gateway.on(listener.getEventType())
                    .flatMap(listener::handle)
                    .onErrorResume(listener::handleError)
                    .subscribe();
        }

        return gateway;
    }

    @Bean
    public RestClient discordRestClient(GatewayDiscordClient client) {
        return client.getRestClient();
    }
}
