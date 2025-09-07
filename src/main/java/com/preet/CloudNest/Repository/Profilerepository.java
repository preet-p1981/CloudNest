package com.preet.CloudNest.Repository;

import com.preet.CloudNest.Documents.Profiledocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface Profilerepository extends MongoRepository<Profiledocument,String> {
    Optional<Profiledocument> findByEmail(String email);
    Profiledocument findByClerkId(String clerkId);
    Boolean existsByClerkId(String clerkId);

}
