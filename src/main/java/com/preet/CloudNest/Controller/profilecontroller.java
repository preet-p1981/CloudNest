package com.preet.CloudNest.Controller;

import com.preet.CloudNest.Documents.Profiledocument;
import com.preet.CloudNest.Dto.Profiledto;
import com.preet.CloudNest.Repository.Profilerepository;
import com.preet.CloudNest.Service.Profileservice;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class profilecontroller {

    private final Profileservice profileservice;
    private final Profilerepository profilerepository;

    @PostMapping("/register")
    public ResponseEntity<Profiledto> registerProfile(@RequestBody Profiledto profileDto) {
        boolean exists = profileservice.existByClerkId(profileDto.getClerkId());
        Profiledto savedProfile =profileservice.createProfile(profileDto);
        HttpStatus status = exists ? HttpStatus.OK : HttpStatus.CREATED;
        return new ResponseEntity<>(savedProfile, status);
    }
    @GetMapping("/test-db-connection")
    public ResponseEntity<?> testDatabaseConnection() {
        try {
            System.out.println("Testing database connection...");

            // Test basic connection
            long profileCount = profilerepository.count();
            System.out.println("Current profile count in DB: " + profileCount);

            // List all existing profiles
            List<Profiledocument> allProfiles = profilerepository.findAll();
            System.out.println("All profiles in database:");
            for (Profiledocument p : allProfiles) {
                System.out.println("- ID: " + p.getId() + ", ClerkId: " + p.getClerkId());
            }

            // Test save operation with a dummy profile
            Profiledocument testProfile = new Profiledocument();
            testProfile.setClerkId("test_user_" + System.currentTimeMillis());

            Profiledocument savedTestProfile = profilerepository.save(testProfile);
            System.out.println("Test profile saved with ID: " + savedTestProfile.getId());

            // Clean up test profile
            profilerepository.delete(savedTestProfile);
            System.out.println("Test profile cleaned up");

            return ResponseEntity.ok("Database connection test passed. Profile count: " + profileCount);

        } catch (Exception e) {
            System.err.println("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("Database connection failed: " + e.getMessage());
        }
    }

}
