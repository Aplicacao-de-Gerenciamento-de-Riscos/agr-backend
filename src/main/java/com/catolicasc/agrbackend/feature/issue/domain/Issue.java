package com.catolicasc.agrbackend.feature.issue.domain;

import com.catolicasc.agrbackend.feature.component.domain.Component;
import com.catolicasc.agrbackend.feature.epic.domain.Epic;
import com.catolicasc.agrbackend.feature.sprint.domain.Sprint;
import com.catolicasc.agrbackend.feature.versionissue.domain.VersionIssue;
import com.catolicasc.agrbackend.feature.worklog.domain.Worklog;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_issue")
@NoArgsConstructor
@AllArgsConstructor
public class Issue {

    @Id
    @Column(name = "cod_issue")
    private Long id;

    @Column(name = "key")
    private String key;

    @Column(name = "time_original_estimate")
    private Long timeOriginalEstimate;

    @Column(name = "time_estimate")
    private Long timeEstimate;

    @Column(name = "work_ratio")
    private Long workRatio;

    @Column(name = "status")
    private String status;

    @Column(name = "timespent")
    private Long timespent;

    @Column(name = "resolution_date")
    private LocalDateTime resolutionDate;

    @Column(name = "updated")
    private LocalDateTime updated;

    @Column(name = "created")
    private LocalDateTime created;

    @Column(name = "flagged")
    private Boolean flagged;

    @Column(name = "assignee")
    private String assignee;

    @Column(name = "priority")
    private String priority;

    @Column(name = "issuetype")
    private String issueType;

    @Column(name = "summary")
    private String summary;

    @JoinTable(name = "tb_issue_components",
            joinColumns = @JoinColumn(name = "cod_issue"),
            inverseJoinColumns = @JoinColumn(name = "cod_component"))
    @OneToMany
    private List<Component> components;

    @JoinColumn(name = "cod_epic")
    @ManyToOne
    private Epic epic;

    @JoinColumn(name = "cod_sprint")
    @ManyToOne
    private Sprint sprint;

    @JoinColumn(name = "cod_parent")
    @ManyToOne
    private Issue parent;

    @JoinColumn(name = "cod_worklog", referencedColumnName = "cod_worklog")
    @OneToOne(cascade = CascadeType.ALL)
    private Worklog worklog;

    //Anotação orphanRemoval para que VersionIssues antigos sejam removidos quando não estiverem mais associados a uma Issue
    @OneToMany(mappedBy = "issue", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<VersionIssue> versionIssues;

}