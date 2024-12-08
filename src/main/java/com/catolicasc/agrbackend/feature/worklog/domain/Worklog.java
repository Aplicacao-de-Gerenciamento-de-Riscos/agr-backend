package com.catolicasc.agrbackend.feature.worklog.domain;

import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.worklogentry.domain.WorklogEntry;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_worklog")
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "seq_worklog", sequenceName = "seq_worklog", allocationSize = 1)
public class Worklog {
    @Id
    @Column(name = "cod_worklog")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_worklog")
    private Long id;

    @Column(name = "start_at")
    private Integer startAt;

    @Column(name = "max_results")
    private Integer maxResults;

    @Column(name = "total")
    private Integer total;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "worklog")
    private List<WorklogEntry> worklogEntries;

}
