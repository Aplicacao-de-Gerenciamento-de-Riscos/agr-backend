package com.catolicasc.agrbackend.feature.sprint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_sprint")
@NoArgsConstructor
@AllArgsConstructor
public class Sprint {

    @Id
    @Column(name = "cod_sprint")
    private Long id;

}
