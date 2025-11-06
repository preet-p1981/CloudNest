package com.preet.CloudNest.Repository;

import com.preet.CloudNest.Documents.Filemetadatadocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

public interface Filemetadatarepository extends MongoRepository<Filemetadatadocument,String> {
    List<Filemetadatadocument> findByClerkId(String clerkId);
    Long countByClerkId(String clerkId);
 //   Optional<Filemetadatadocument> findById(String fileId);

}
