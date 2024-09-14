package com.catolicasc.agrbackend.clients.jira.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class JiraIssueResponseDTO extends JiraResponseDTO{

    private String expand;
    private String id;
    private String self;
    private String key;
    private Fields fields;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Fields {
        private Parent parent;
        private Assignee assignee;
        private List<ClosedSprint> closedSprints;
        private Votes votes;
        private Worklog worklog;
        private Issuetype issuetype;
        private Sprint sprint;
        private Status status;
        private List<Component> components;
        private Creator creator;
        private Reporter reporter;
        private List<FixVersion> fixVersions;
        private Epic epic;
        private Priority priority;
        private List<Version> versions;
        private Progress progress;
        private List<Attachment> attachment;
        private String description;
        private String summary;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Parent {
        private String id;
        private String key;
        private String self;
        private FieldsParent fields;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FieldsParent {
        private String summary;
        private Status status;
        private Priority priority;
        private Issuetype issuetype;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Assignee {
        private String self;
        private String accountId;
        private String emailAddress;
        private AvatarUrls avatarUrls;
        private String displayName;
        private boolean active;
        private String timeZone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClosedSprint {
        private String id;
        private String self;
        private String state;
        private String name;
        private String startDate;
        private String endDate;
        private String completeDate;
        private String createdDate;
        private String originBoardId;
        private String goal;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Votes {
        private String self;
        private int votes;
        private boolean hasVoted;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Worklog {
        private int startAt;
        private int maxResults;
        private int total;
        private List<WorklogEntry> worklogs;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class WorklogEntry {
        private String self;
        private String author;
        private String created;
        private String updated;
        private String timeSpent;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Issuetype {
        private String self;
        private String id;
        private String description;
        private String iconUrl;
        private String name;
        private boolean subtask;
        private int hierarchyLevel;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Sprint {
        private String id;
        private String self;
        private String state;
        private String name;
        private String startDate;
        private String endDate;
        private String createdDate;
        private String originBoardId;
        private String goal;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Status {
        private String self;
        private String description;
        private String iconUrl;
        private String name;
        private String id;
        private StatusCategory statusCategory;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class StatusCategory {
        private String self;
        private int id;
        private String key;
        private String colorName;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Component {
        private String self;
        private String id;
        private String name;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Creator {
        private String self;
        private String accountId;
        private String emailAddress;
        private AvatarUrls avatarUrls;
        private String displayName;
        private boolean active;
        private String timeZone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Reporter {
        private String self;
        private String accountId;
        private String emailAddress;
        private AvatarUrls avatarUrls;
        private String displayName;
        private boolean active;
        private String timeZone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class FixVersion {
        private String self;
        private String id;
        private String description;
        private String name;
        private boolean archived;
        private boolean released;
        private String releaseDate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Epic {
        private String id;
        private String key;
        private String self;
        private String name;
        private String summary;
        private Color color;
        private IssueColor issueColor;
        private boolean done;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Color {
        private String key;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IssueColor {
        private String key;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Priority {
        private String self;
        private String iconUrl;
        private String name;
        private String id;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Version {
        private String self;
        private String id;
        private String description;
        private String name;
        private boolean archived;
        private boolean released;
        private String releaseDate;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Progress {
        private int progress;
        private int total;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Attachment {
        private String self;
        private String id;
        private String filename;
        private Author author;
        private String created;
        private long size;
        private String mimeType;
        private String content;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Author {
        private String self;
        private String accountId;
        private String emailAddress;
        private AvatarUrls avatarUrls;
        private String displayName;
        private boolean active;
        private String timeZone;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class AvatarUrls {
        private String x48x48;
        private String x24x24;
        private String x16x16;
        private String x32x32;
    }
}
