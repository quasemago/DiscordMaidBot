package dev.quasemago.maidbot;

import discord4j.discordjson.json.ApplicationCommandData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class MaidBotApplication {
	public static final String VERSION = "0.0.2";
	public static final Logger log = LogManager.getLogger(MaidBotApplication.class);
	public static Map<String, ApplicationCommandData> slashCommandList = new HashMap<>();

	public static void main(String[] args) {
		new SpringApplicationBuilder(MaidBotApplication.class)
				.headless(false)
				.run(args);
	}
}
