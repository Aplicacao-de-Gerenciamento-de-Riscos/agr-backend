package com.catolicasc.agrbackend.feature.versionissue.domain;

import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.version.domain.Version;
import jakarta.persistence.*;
import lombok.*;
@Getter
@Setter
@Entity
@Builder
@Table(name = "tb_version_issue")
@NoArgsConstructor
@AllArgsConstructor
@SequenceGenerator(name = "seq_version_issue", sequenceName = "seq_version_issue", allocationSize = 1)
public class VersionIssue {

        @Id
        @Column(name = "cod_version_issue")
        @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_version_issue")
        private Long id;

        @JoinColumn(name = "cod_version")
        @ManyToOne
        private Version version;

        @JoinColumn(name = "cod_issue")
        @ManyToOne
        private Issue issue;

}
