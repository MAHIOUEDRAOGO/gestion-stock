package com.gestionstock.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * En production, Angular est servi par Spring Boot (dossier resources/static).
 * Les routes Angular (/produits, /caisse...) sont redirigées vers index.html.
 */
@Controller
public class SpaController {

    @GetMapping({"/", "/{chemin:^(?!api|swagger-ui|v3)[^\\.]*}", "/{chemin:^(?!api|swagger-ui|v3)[^\\.]*}/**"})
    public String index() {
        return "forward:/index.html";
    }
}
