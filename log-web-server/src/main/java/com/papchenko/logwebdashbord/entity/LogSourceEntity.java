package com.papchenko.logwebdashbord.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.util.List;
import java.util.Objects;

@Entity
@Getter
@Setter
@Table(name = "LOG_SOURCE_ENTITIES")
public class LogSourceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "URL", nullable = false)
    private String url;

    @Column(name = "STATUS")
    private boolean status;

    @OneToOne(mappedBy = "logSourceEntity", cascade = CascadeType.ALL)
    private WatchFileEntity watchFileEntity;

    @OneToMany(mappedBy = "logSourceEntity", cascade = CascadeType.ALL)
    private List<TextAlertEntity> textAlertEntities;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LogSourceEntity logSourceEntity = (LogSourceEntity) o;
        return Objects.equals(url, logSourceEntity.url);
    }

    @Override
    public int hashCode() {

        return Objects.hash(url);
    }
}
