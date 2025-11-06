package com.preet.CloudNest.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilemetadataDto {

    private String id;
    private String name;
    private Long size;
    private String clerkId;
    private boolean isPublic;
    private String type; // must exist
    private String fileLocation;
    private LocalDateTime uploadedAt;

}
