package com.catolicasc.agrbackend.feature.worklogentry.domain;

import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_worklog_entry")
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "seq_worklog_entry", sequenceName = "seq_worklog_entry", allocationSize = 1)
public class WorklogEntry {

    @Id
    @Column(name = "cod_worklog_entry")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_worklog_entry")
    private Long id;

    @Column(name = "self")
    private String self;

    @Column(name = "author")
    private String author;

    @Column(name = "created")
    private String created;

    @Column(name = "updated")
    private String updated;

    @Column(name = "time_spent")
    private String timeSpent;

    @ManyToOne
    @JoinColumn(name = "cod_worklog")
    private Worklog worklog;
}
