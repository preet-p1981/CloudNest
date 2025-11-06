package com.preet.CloudNest.Service;

import org.springframework.dao.DuplicateKeyException;
import com.mongodb.MongoWriteException;
import com.preet.CloudNest.Documents.Profiledocument;
import com.preet.CloudNest.Dto.Profiledto;
import com.preet.CloudNest.Repository.Profilerepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;


@Service
@RequiredArgsConstructor
public class Profileservice {
    private final Profilerepository profilerepository;

    // FIXED: createProfile method that handles both create and update
    public Profiledto createProfile(Profiledto profiledto) {
        System.out.println("=== CREATE PROFILE DEBUG ===");
        System.out.println("ClerkId: '" + profiledto.getClerkId() + "'");
        System.out.println("Email: '" + profiledto.getEmail() + "'");
        System.out.println("Firstname: '" + profiledto.getFirstname() + "'");
        System.out.println("Lastname: '" + profiledto.getLastname() + "'");

        try {
            // Check if profile already exists
            if (profilerepository.existsByClerkId(profiledto.getClerkId())) {
                System.out.println("Profile exists - updating with new data");
                return updateprofile(profiledto);
            }

            System.out.println("Creating new profile...");
            Profiledocument profile = Profiledocument.builder()
                    .clerkId(profiledto.getClerkId())
                    .email(profiledto.getEmail())
                    .firstname(profiledto.getFirstname())
                    .lastname(profiledto.getLastname())
                    .photoUrl(profiledto.getPhotoUrl())
                    .Credits(5) // Default credits
                    .createdAt(Instant.now())
                    .build();

            profile = profilerepository.save(profile);
            System.out.println("Profile created successfully with ID: " + profile.getId());

            return Profiledto.builder()
                    .id(profile.getId())
                    .clerkId(profile.getClerkId())
                    .email(profile.getEmail())
                    .firstname(profile.getFirstname())
                    .lastname(profile.getLastname())
                    .photoUrl(profile.getPhotoUrl())
                    .credits(profile.getCredits())
                    .createdAt(profile.getCreatedAt())
                    .build();

        } catch (DuplicateKeyException e) {
            System.out.println("Duplicate key exception - profile exists, updating instead");
            return updateprofile(profiledto);
        } catch (Exception e) {
            System.out.println("ERROR creating profile: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create profile: " + e.getMessage());
        }
    }

    // FIXED: updateprofile method with better validation
    public Profiledto updateprofile(Profiledto profiledto) {
        System.out.println("=== UPDATE PROFILE DEBUG ===");
        System.out.println("Updating ClerkId: '" + profiledto.getClerkId() + "'");

        Profiledocument existingprofile = profilerepository.findByClerkId(profiledto.getClerkId());

        if (existingprofile != null) {
            System.out.println("Existing profile found, updating fields...");

            if (profiledto.getEmail() != null && !profiledto.getEmail().trim().isEmpty()) {
                existingprofile.setEmail(profiledto.getEmail());
                System.out.println("Updated email: " + profiledto.getEmail());
            }
            if (profiledto.getFirstname() != null && !profiledto.getFirstname().trim().isEmpty()) {
                existingprofile.setFirstname(profiledto.getFirstname());
                System.out.println("Updated firstname: " + profiledto.getFirstname());
            }
            if (profiledto.getLastname() != null && !profiledto.getLastname().trim().isEmpty()) {
                existingprofile.setLastname(profiledto.getLastname());
                System.out.println("Updated lastname: " + profiledto.getLastname());
            }
            if (profiledto.getPhotoUrl() != null && !profiledto.getPhotoUrl().trim().isEmpty()) {
                existingprofile.setPhotoUrl(profiledto.getPhotoUrl());
                System.out.println("Updated photoUrl: " + profiledto.getPhotoUrl());
            }

            try {
                existingprofile = profilerepository.save(existingprofile);
                System.out.println("Profile updated successfully");

                return Profiledto.builder()
                        .id(existingprofile.getId())
                        .clerkId(existingprofile.getClerkId())
                        .email(existingprofile.getEmail())
                        .firstname(existingprofile.getFirstname())
                        .lastname(existingprofile.getLastname())
                        .photoUrl(existingprofile.getPhotoUrl())
                        .credits(existingprofile.getCredits())
                        .createdAt(existingprofile.getCreatedAt())
                        .build();

            } catch (Exception e) {
                System.out.println("ERROR updating profile: " + e.getMessage());
                throw new RuntimeException("Failed to update profile: " + e.getMessage());
            }
        }

        System.out.println("No existing profile found for clerkId: " + profiledto.getClerkId());
        return null;
    }

    public boolean existByClerkId(String clerkId) {
        boolean exists = profilerepository.existsByClerkId(clerkId);
        System.out.println("Profile exists for clerkId '" + clerkId + "': " + exists);
        return exists;
    }

    public void deleteProfile(String clerkId) {
        System.out.println("Deleting profile for clerkId: " + clerkId);
        Profiledocument existingprofile = profilerepository.findByClerkId(clerkId);
        if (existingprofile != null) {
            profilerepository.delete(existingprofile);
            System.out.println("Profile deleted successfully");
        } else {
            System.out.println("No profile found to delete");
        }
    }

    // FIXED: getCurrentProfile that handles unique email constraint
    public Profiledocument getCurrentProfile() {
        System.out.println("=== GET CURRENT PROFILE DEBUG ===");

        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            System.out.println("Authentication is NULL");
            throw new UsernameNotFoundException("User not authenticated");
        }

        String clerkId = SecurityContextHolder.getContext().getAuthentication().getName();
        System.out.println("Looking up profile for clerkId: '" + clerkId + "'");

        if (clerkId == null || clerkId.trim().isEmpty() || "anonymousUser".equals(clerkId)) {
            System.out.println("Invalid clerkId: " + clerkId);
            throw new UsernameNotFoundException("Invalid user ID");
        }

        Profiledocument profile = profilerepository.findByClerkId(clerkId);
        System.out.println("Profile found: " + (profile != null ? "YES" : "NO"));

        if (profile == null) {
            System.out.println("No profile found - creating minimal profile");

            // Create minimal profile with unique email to avoid duplicate key error
            String tempEmail = "temp_" + clerkId + "@placeholder.com"; // Unique email

            profile = Profiledocument.builder()
                    .clerkId(clerkId)
                    .email(tempEmail) // Use unique temporary email instead of empty string
                    .firstname("") // Empty string is fine for non-unique fields
                    .lastname("") // Empty string is fine for non-unique fields
                    .photoUrl("") // Empty string is fine for non-unique fields
                    .Credits(5)
                    .createdAt(Instant.now())
                    .build();

            try {
                profile = profilerepository.save(profile);
                System.out.println("Created minimal profile with ID: " + profile.getId());
                System.out.println("Temporary email: " + tempEmail);
            } catch (Exception e) {
                System.out.println("ERROR creating minimal profile: " + e.getMessage());

                // If still fails, try to find existing profile one more time
                profile = profilerepository.findByClerkId(clerkId);
                if (profile != null) {
                    System.out.println("Found profile on retry - using existing profile");
                    return profile;
                }

                throw new RuntimeException("Failed to create profile after retry: " + e.getMessage());
            }
        }

        if (profile != null) {
            System.out.println("Profile ID: " + profile.getId());
            System.out.println("Profile Email: '" + profile.getEmail() + "'");
            System.out.println("Profile Firstname: '" + profile.getFirstname() + "'");
            System.out.println("Profile Lastname: '" + profile.getLastname() + "'");
        }

        return profile;
    }
}