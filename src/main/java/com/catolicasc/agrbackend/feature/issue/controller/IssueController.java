package com.catolicasc.agrbackend.feature.issue.controller;

import com.catolicasc.agrbackend.feature.issue.service.IssueService;
import com.catolicasc.agrbackend.feature.jobs.service.JobsService;
import feign.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("v1/issue")
public class IssueController {

    @Autowired
    private IssueService issueService;

    @Autowired
    private JobsService jobsService;

    @GetMapping("/sync")
    public ResponseEntity<Response> syncIssues() {
        jobsService.syncIssuesOnStart();
        return ResponseEntity.ok().build();
    }

}
