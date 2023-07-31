package dev.quasemago.maidbot;

import discord4j.discordjson.json.ApplicationCommandData;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class MaidBotApplication {
	public static final String VERSION = "0.0.4";
	public static final String GITHUB_URL = "https://github.com/quasemago/DiscordMaidBot";
	public static final String GITHUB_AUTHOR = "quasemago";
	public static final String GITHUB_AUTHOR_URL = "https://github.com/quasemago";
	public static final String DISCORD_AUTHOR = "mxronning";

	// TODO: Improve this.
	public static Map<String, ApplicationCommandData> aaslashCommandList = new HashMap<>();

	public static void main(String[] args) {
		new SpringApplicationBuilder(MaidBotApplication.class)
				.headless(false)
				.run(args);
	}
}
