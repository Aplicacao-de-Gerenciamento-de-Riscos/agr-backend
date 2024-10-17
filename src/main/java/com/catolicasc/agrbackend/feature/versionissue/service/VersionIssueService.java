package com.catolicasc.agrbackend.feature.versionissue.service;

import com.catolicasc.agrbackend.feature.versionissue.domain.VersionIssue;
import com.catolicasc.agrbackend.feature.versionissue.repository.VersionIssueRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VersionIssueService {
    private final VersionIssueRepository versionIssueRepository;

    public VersionIssueService(VersionIssueRepository versionIssueRepository) {
        this.versionIssueRepository = versionIssueRepository;
    }

    public void saveAll(List<VersionIssue> versionIssues) {
        versionIssueRepository.saveAll(versionIssues);
    }
}
