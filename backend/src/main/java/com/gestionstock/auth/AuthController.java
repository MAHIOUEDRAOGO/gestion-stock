package com.gestionstock.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.audit.AuditService;
import com.gestionstock.auth.AuthDtos.*;
import com.gestionstock.common.BusinessException;
import com.gestionstock.config.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UtilisateurRepository utilisateurRepository;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final CurrentUser currentUser;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(req.login(), req.motDePasse()));
        } catch (RuntimeException e) {
            auditService.log(req.login(), "CONNEXION_ECHOUEE", null, null, null);
            throw new BadCredentialsException("Identifiants invalides");
        }
        Utilisateur u = utilisateurRepository.findByLogin(req.login()).orElseThrow();
        auditService.log(u.getLogin(), "CONNEXION", "Utilisateur", u.getId(), null);
        return new LoginResponse(jwtService.generer(u), UtilisateurDto.from(u));
    }

    @GetMapping("/me")
    public UtilisateurDto me() {
        return UtilisateurDto.from(currentUser.get());
    }

    @PostMapping("/mot-de-passe")
    @Transactional
    public void changerMotDePasse(@Valid @RequestBody ChangerMotDePasseRequest req) {
        Utilisateur u = currentUser.get();
        if (!passwordEncoder.matches(req.ancien(), u.getMotDePasse())) {
            throw new BusinessException("Ancien mot de passe incorrect");
        }
        u.setMotDePasse(passwordEncoder.encode(req.nouveau()));
        auditService.log("CHANGEMENT_MOT_DE_PASSE", "Utilisateur", u.getId(), null);
    }
}
