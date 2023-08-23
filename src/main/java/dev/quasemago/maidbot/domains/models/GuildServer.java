package dev.quasemago.maidbot.domains.models;

import dev.quasemago.maidbot.dto.GuildServerDTO;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "guild_servers")
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

    public GuildServer(GuildServerDTO data) {
        this.guildId = data.guildId();
        this.logFlags = data.logFlags();
        this.logChannelId = data.logChannelId();
    }
}
