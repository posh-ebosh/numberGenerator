package org.example.controllers;

import lombok.RequiredArgsConstructor;
import org.example.services.NumberService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/number")
public class NumberController {

    private final NumberService numberService;

    @GetMapping("/random")
    public String getRandomNumber(){
        return numberService.getRandomNumber();
    }

    @GetMapping("/next")
    public String getNextNumber(){
        return numberService.getNextNumber();
    }

}
