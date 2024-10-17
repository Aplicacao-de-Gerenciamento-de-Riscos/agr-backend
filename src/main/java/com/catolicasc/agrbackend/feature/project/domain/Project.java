package com.catolicasc.agrbackend.feature.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_project")
@NoArgsConstructor
@AllArgsConstructor
public class Project {

    @Id
    @Column(name = "cod_project")
    private Long id;

    @Column(name = "key")
    private String key;

    @Column(name = "board_id")
    private Long boardId;
}
