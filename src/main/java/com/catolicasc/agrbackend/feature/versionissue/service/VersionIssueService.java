package com.catolicasc.agrbackend.feature.versionissue.service;

import com.catolicasc.agrbackend.feature.issue.domain.Issue;
import com.catolicasc.agrbackend.feature.versionissue.domain.VersionIssue;
import com.catolicasc.agrbackend.feature.versionissue.repository.VersionIssueRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Transactional
@Service
public class VersionIssueService {
    private final VersionIssueRepository versionIssueRepository;

    public VersionIssueService(VersionIssueRepository versionIssueRepository) {
        this.versionIssueRepository = versionIssueRepository;
    }

    public void deleteAllByIssue(Issue issue) {
        versionIssueRepository.deleteAllByIssue(issue);
    }

    public void saveAll(List<VersionIssue> versionIssues) {
        for (VersionIssue versionIssue : versionIssues) {
            if (!versionIssueRepository.existsByIssueAndVersion(versionIssue.getIssue(), versionIssue.getVersion())) {
                versionIssueRepository.save(versionIssue);
            }
        }
    }

}
