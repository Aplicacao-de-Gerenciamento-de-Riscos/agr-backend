package com.catolicasc.agrbackend.feature.version.domain;

import com.catolicasc.agrbackend.feature.project.domain.Project;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_version")
@NoArgsConstructor
@AllArgsConstructor
public class Version {
    @Id
    @Column(name = "cod_version")
    private Long id;

    @Column(name = "description")
    private String description;

    @Column(name = "name")
    private String name;

    @Column(name = "archived")
    private Boolean archived;

    @Column(name = "released")
    private Boolean released;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "overdue")
    private Boolean overdue;

    @Column(name = "user_start_date")
    private String userStartDate;

    @Column(name = "user_release_date")
    private String userReleaseDate;

    @JoinColumn(name = "cod_project")
    @ManyToOne
    private Project project;
}
