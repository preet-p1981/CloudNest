package com.preet.CloudNest.Controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.preet.CloudNest.Dto.Profiledto;
import com.preet.CloudNest.Service.Profileservice;
import com.preet.CloudNest.Service.usercreditsservice;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/webhooks")

public class clerkwebhookcontroller {

    @Value("${clerk.webhook-secret}")
    private String webhooksecret;

    private final Profileservice profileservice;
    private final usercreditsservice usercreditsservice;

    @PostMapping("/clerk")
    public ResponseEntity<?> handleclerlwebhook(@RequestHeader("svix-id") String svixid,
                                                @RequestHeader("svix-timestamp") String svixtimestamp,
                                                @RequestHeader("svix-signature") String svixsignature,
                                                @RequestBody String payload) {
        try {
            Boolean isvalid = verifyWebhookSignature(svixid, svixtimestamp, svixsignature, payload);
            if (!isvalid) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid signature");
            }

            ObjectMapper mapper = new ObjectMapper();
            JsonNode rootNode = mapper.readTree(payload);
            String eventType = rootNode.path("type").asText();

            switch (eventType) {
                case "user.created":
                    handleUserCreated(rootNode.path("data"));
                    break;
                case "user.updated":
                    handleUserUpdated(rootNode.path("data"));
                    break;
                case "user.deleted":
                    handleUserDeleted(rootNode.path("data"));
                    break;
            }
            return ResponseEntity.ok().build();

        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }

    private void handleUserDeleted(JsonNode data) {
        String clerkid = data.path("id").asText();
        profileservice.deleteProfile(clerkid);

    }

    private void handleUserUpdated(JsonNode data) {
        String clerkid = data.path("id").asText();
        String email = "";
        JsonNode emailaddressess = data.path("email_addresses");
        if (emailaddressess.isArray() && emailaddressess.size() > 0) {
            email = emailaddressess.get(0).path("email_address").asText();
        }
        String firstname = data.path("first_name").asText("");
        String lastname = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        Profiledto updatedprofile = Profiledto.builder()
                .clerkId(clerkid)
                .email(email)
                .firstname(firstname)
                .lastname(lastname)
                .photoUrl(photoUrl)
                .build();

        updatedprofile = profileservice.updateprofile(updatedprofile);
        if (updatedprofile == null) {
            handleUserCreated(data);
        }
    }

    private void handleUserCreated(JsonNode data) {
        String clerkid = data.path("id").asText();

        String email = "";
        JsonNode emailaddressess = data.path("email_addresses");
        if (emailaddressess.isArray() && emailaddressess.size() > 0) {
            email = emailaddressess.get(0).path("email_address").asText();
        }

        String firstname = data.path("first_name").asText("");
        String lastname = data.path("last_name").asText("");
        String photoUrl = data.path("image_url").asText("");

        Profiledto newprofile = Profiledto.builder()
                .clerkId(clerkid)
                .email(email)
                .firstname(firstname)
                .lastname(lastname)
                .photoUrl(photoUrl)
                .build();
        profileservice.createProfile(newprofile);
        usercreditsservice.createIntialCredits(clerkid);
        System.out.println("user craetes" + clerkid);
        System.out.println("Profile created in DB for clerkId: " + clerkid);



    }


    private boolean verifyWebhookSignature(String svixid, String svixtimestamp, String svixsignature, String payload) {
        return true;
    }
}
