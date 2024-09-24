package com.catolicasc.agrbackend.feature.epic.dto;

import lombok.Data;

@Data
public class EpicDTO {
    private Long id;
    private String name;
    private String summary;
    private String key;
}
