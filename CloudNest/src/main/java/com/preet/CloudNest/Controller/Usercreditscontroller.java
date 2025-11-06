package com.preet.CloudNest.Controller;

import com.preet.CloudNest.Documents.usercredits;
import com.preet.CloudNest.Dto.usercreditsDto;
import com.preet.CloudNest.Service.usercreditsservice;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class Usercreditscontroller {

    private final usercreditsservice usercreditsservice;

    @GetMapping("/credits")
    public ResponseEntity<?> getusercredits(){
       usercredits userCredits =  usercreditsservice.getusercredits();
        usercreditsDto response = usercreditsDto.builder()
                .credits(userCredits.getCredits())
                .plan(userCredits.getPlan())
                .build();

        return ResponseEntity.ok(response);


    }

}
