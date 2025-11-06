package com.preet.CloudNest.Dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Profiledto {
    private String id;
    private String clerkId;
    private String email;
    private String firstname;
    private String lastname;
    private Integer credits;
    private String photoUrl;
    private Instant createdAt;
}
