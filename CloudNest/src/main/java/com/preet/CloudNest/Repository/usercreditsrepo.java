package com.preet.CloudNest.Repository;

import com.preet.CloudNest.Documents.usercredits;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface usercreditsrepo extends MongoRepository<usercredits, String> {
    Optional<usercredits> findByClerkId(String clerkId);

}
