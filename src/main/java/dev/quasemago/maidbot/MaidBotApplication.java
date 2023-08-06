package dev.quasemago.maidbot;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaidBotApplication {
	public static final String VERSION = "0.0.4";
	public static final String GITHUB_URL = "https://github.com/quasemago/DiscordMaidBot";
	public static final String GITHUB_AUTHOR = "quasemago";
	public static final String GITHUB_AUTHOR_URL = "https://github.com/quasemago";
	public static final String DISCORD_AUTHOR = "mxronning";

	public static void main(String[] args) {
		new SpringApplicationBuilder(MaidBotApplication.class)
				.headless(false)
				.run(args);
	}
}
