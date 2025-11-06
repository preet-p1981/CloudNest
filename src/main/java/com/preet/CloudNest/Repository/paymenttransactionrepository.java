package com.preet.CloudNest.Repository;

import com.preet.CloudNest.Documents.paymenttansaction;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface paymenttransactionrepository extends MongoRepository<paymenttansaction,String> {

    List<paymenttansaction> findByClerkId(String clerkId);
    List<paymenttansaction> findByClerkIdOrderByTransactionDateDesc(String clerkId);
    List<paymenttansaction> findByClerkIdAndStatusOrderByTransactionDateDesc(String clerkId, String status);


}
