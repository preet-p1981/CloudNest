package com.preet.CloudNest.Documents;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
@Document(collection="profiles")
public class Profiledocument {

    @Id
    private String Id;
    private String clerkId;
    @Indexed(unique = true)
    private String email;
    private String firstname;
    private String lastname;
    private Integer Credits;
    private String photoUrl;
    @CreatedDate
    private Instant createdAt;
}
