package dev.quasemago.maidbot.data.models;

import dev.quasemago.maidbot.data.dto.MemesDTO;
import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;

@Entity(name = "memes")
@Table(name = "memes")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(of ="id")
public class Memes {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, unique = true)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String url;
    @Column(name = "date_created", nullable = false)
    private Date dateCreated;
    @Column(name = "date_updated", nullable = false)
    private Date dateUpdated;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "guild_id", nullable = false)
    private GuildServer guild;

    public Memes(MemesDTO data) {
        this.name = data.name();
        this.url = data.url();
        this.dateCreated = data.dateCreated();
        this.dateUpdated = data.dateCreated();
        this.guild = data.guildServer();
    }
}
