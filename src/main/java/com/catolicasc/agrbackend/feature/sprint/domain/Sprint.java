package com.catolicasc.agrbackend.feature.sprint.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

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

    @Column(name = "name")
    private String name;

    @Column(name = "state")
    private String state;

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @Column(name = "end_date")
    private LocalDateTime endDate;

    @Column(name = "complete_date")
    private LocalDateTime completeDate;

    @Column(name = "goal")
    private String goal;

}
