package org.usermanagement.traceandtrust.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController // Annonce à Spring que cette classe va gérer des requêtes web REST
public class HealthCheckController {

    // Annonce à Spring que cette méthode doit répondre aux requêtes GET sur le chemin "/"
    @GetMapping("/")
    public String healthCheck() {
        return "Logistics API is running!";
    }
}
