package com.preet.CloudNest.Service;

import com.preet.CloudNest.Documents.Profiledocument;
import com.preet.CloudNest.Documents.usercredits;
import com.preet.CloudNest.Repository.usercreditsrepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class usercreditsservice {

    private final usercreditsrepo usercreditsrepo;
    private final Profileservice profileservice; // lowercase variable

    public usercredits createIntialCredits(String clerkId) {
        usercredits credits = usercredits.builder()
                .clerkId(clerkId)
                .credits(5)
                .plan("BASIC")
                .build();

        return usercreditsrepo.save(credits);
    }

    public usercredits getusercredits(String clerkId) {
        return usercreditsrepo.findByClerkId(clerkId)
                .orElseGet(() -> createIntialCredits(clerkId));
    }

    public usercredits getusercredits() {
        String clerkId = profileservice.getCurrentProfile().getClerkId(); // use instance variable
        return getusercredits(clerkId);
    }

    public boolean hasEnoughCredits(int requiredCredits) {
        try {
            usercredits credits = getusercredits();
            if (credits == null) {
                return false;
            }
            return credits.getCredits() >= requiredCredits;
        } catch (Exception e) {
            System.out.println("Error checking credits: " + e.getMessage());
            return false; // Return false instead of throwing
        }
    }

    public usercredits consumecredits() {
        usercredits usercredits = getusercredits();

        if (usercredits.getCredits() <= 0) {
            return null;
        }
        usercredits.setCredits(usercredits.getCredits() - 1);
        return usercreditsrepo.save(usercredits);
    }

    public usercredits addcredits(String clerkId, int creditsToAdd, String plan) {
        usercredits usercredits = usercreditsrepo.findByClerkId(clerkId)
        .orElseGet(()-> createIntialCredits(clerkId));
        usercredits.setCredits(usercredits.getCredits() + creditsToAdd);
        usercredits.setPlan(plan);
        return usercreditsrepo.save(usercredits);
    }
}
