package dev.quasemago.maidbot.data.models;

import dev.quasemago.maidbot.data.dto.GuildServerDTO;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.util.Locale;

@Entity(name = "guild_servers")
@Table(name = "guild_servers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of ="id")
public class GuildServer {
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
    private Locale locale;
    @Column(name = "last_updated")
    private Date lastUpdated;

    public GuildServer(GuildServerDTO data) {
        this.guildId = data.guildId();
        this.logFlags = data.logFlags();
        this.logChannelId = data.logChannelId();
        this.locale = data.locale();
        this.lastUpdated = data.lastUpdated();
    }
}
