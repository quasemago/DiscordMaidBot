package dev.quasemago.maidbot.models;

import jakarta.persistence.*;

import java.io.Serial;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "memes")
public class Memes implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

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
    @Column(name = "guild_id", nullable = false)
    private Long guildId;

    public Memes() {}

    public Memes(Date date_created, Date date_updated, Long guild_id, String name, String url) {
        this.dateCreated = date_created;
        this.dateUpdated = date_updated;
        this.guildId = guild_id;
        this.name = name;
        this.url = url;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Date date_created) {
        this.dateCreated = date_created;
    }

    public Date getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(Date date_updated) {
        this.dateUpdated = date_updated;
    }

    public Long getGuildId() {
        return guildId;
    }

    public void setGuildId(Long guild_id) {
        this.guildId = guild_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
