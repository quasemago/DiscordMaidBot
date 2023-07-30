package br.com.zrage.maidbot;

import discord4j.discordjson.json.ApplicationCommandData;
import org.apache.logging.log4j.LogManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
@EnableScheduling
public class MaidbotApplication {
	public static org.apache.logging.log4j.Logger log;
	public static Map<String, ApplicationCommandData> slashCommandList = new HashMap<>();

	public static void main(String[] args) {
		new SpringApplicationBuilder(MaidbotApplication.class)
				.headless(false)
				.run(args);

		log = LogManager.getLogger(MaidbotApplication.class);
	}
}
