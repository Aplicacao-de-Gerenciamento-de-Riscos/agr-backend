package com.catolicasc.agrbackend.feature.issue.controller;

import com.catolicasc.agrbackend.feature.issue.service.IssueService;
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

    @GetMapping("/sync")
    public ResponseEntity<Response> syncIssues() {
        issueService.syncIssuesBySprints();
        return ResponseEntity.ok().build();
    }

}
