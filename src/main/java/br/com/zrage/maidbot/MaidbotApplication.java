package br.com.zrage.maidbot;

import org.apache.logging.log4j.LogManager;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MaidbotApplication {
	public static org.apache.logging.log4j.Logger log;

	public static void main(String[] args) {
		new SpringApplicationBuilder(MaidbotApplication.class)
				.headless(false)
				.run(args);

		log = LogManager.getLogger(MaidbotApplication.class);
	}
}
