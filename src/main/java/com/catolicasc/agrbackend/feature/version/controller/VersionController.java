package com.catolicasc.agrbackend.feature.version.controller;

import com.catolicasc.agrbackend.feature.version.dto.VersionDTO;
import com.catolicasc.agrbackend.feature.version.service.VersionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("v1/version")
public class VersionController {

    @Autowired
    private VersionService versionService;

    @GetMapping("/all")
    public ResponseEntity<List<VersionDTO>> getAllVersions(@RequestParam(name = "projectId") List<Long> projectId) {
        return ResponseEntity.ok(versionService.findAllByProjectId(projectId));
    }
}
