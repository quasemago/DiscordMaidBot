package dev.quasemago.maidbot.models;

import jakarta.persistence.*;

@Entity
@Table(name = "servers")
public class Servers {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(name = "guild_id", nullable = false, unique = true)
    private Long guildId;
    @Column(name = "log_flags")
    private Long logFlags;
    @Column(name = "log_channel_id")
    private Long logChannelId;

    public Servers () {}

    public Servers(Long guildId, Long logFlags, Long logChannelId) {
        this.guildId = guildId;
        this.logFlags = logFlags;
        this.logChannelId = logChannelId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guildId) {
        this.guildId = guildId;
    }

    public Long getLogFlags() {
        return logFlags;
    }

    public void setLogFlags(Long logFlags) {
        this.logFlags = logFlags;
    }

    public Long getLogChannelId() {
        return logChannelId;
    }

    public void setLogChannelId(Long logChannelId) {
        this.logChannelId = logChannelId;
    }
}
