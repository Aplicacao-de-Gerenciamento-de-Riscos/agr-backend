package com.catolicasc.agrbackend.feature.component.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_component")
@NoArgsConstructor
@AllArgsConstructor
public class Component {

    @Id
    @Column(name = "cod_component")
    private Long id;

    @Column(name = "name")
    private String name;
}
