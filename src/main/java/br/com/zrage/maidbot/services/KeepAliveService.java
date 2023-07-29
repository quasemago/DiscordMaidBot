package br.com.zrage.maidbot.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class KeepAliveService {
    @Scheduled(fixedRate = 60000)
    public void keepAlive() {
        System.out.println("Keeping alive...");
    }
}
