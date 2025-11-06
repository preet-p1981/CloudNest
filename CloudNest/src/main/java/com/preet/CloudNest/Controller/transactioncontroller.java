package com.preet.CloudNest.Controller;

import com.preet.CloudNest.Documents.Profiledocument;
import com.preet.CloudNest.Documents.paymenttansaction;
import com.preet.CloudNest.Repository.paymenttransactionrepository;
import com.preet.CloudNest.Service.Profileservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class transactioncontroller {
    private final paymenttransactionrepository paymenttransactionrepository;
    private final Profileservice profileservice;

    @GetMapping
    public ResponseEntity<?> getusertransactions(){
        Profiledocument currentprofile = profileservice.getCurrentProfile();
        String clerkId = currentprofile.getClerkId();

        List<paymenttansaction> transactions  = paymenttransactionrepository.findByClerkIdAndStatusOrderByTransactionDateDesc(clerkId,"success");
        return ResponseEntity.ok(transactions);
    }
}
