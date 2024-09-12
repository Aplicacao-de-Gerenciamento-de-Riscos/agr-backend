package com.catolicasc.agrbackend.feature.epic.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_epic")
@NoArgsConstructor
@AllArgsConstructor
public class Epic {

    @Id
    @Column(name = "cod_epic")
    private Long id;
}
