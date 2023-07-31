package dev.quasemago.maidbot.data;

import dev.quasemago.maidbot.helpers.Logger;
import dev.quasemago.maidbot.events.GenericEventInterface;
import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
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

import java.util.List;

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

    @Bean
    public <T extends Event>GatewayDiscordClient gatewayDiscordClient(final List<GenericEventInterface<T>> eventListenerList) {
        if (this.getBotToken().isEmpty()) {
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
                .setInitialPresence(ignore -> ClientPresence.online(ClientActivity.playing(this.getBotDefaultPresence())))
                .login()
                .block();

        if (gateway == null) {
            Logger.log.fatal("Failed to login into discord gateway!");
            System.exit(0);
        }

        // Hook events.
        for (final GenericEventInterface<T> listener : eventListenerList) {
            gateway.on(listener.getEventType())
                    .flatMap(listener::execute)
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
