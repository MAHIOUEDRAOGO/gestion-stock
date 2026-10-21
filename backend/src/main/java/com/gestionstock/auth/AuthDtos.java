package com.gestionstock.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class AuthDtos {
    private AuthDtos() {}

    public record LoginRequest(@NotBlank String login, @NotBlank String motDePasse) {}

    public record LoginResponse(String token, UtilisateurDto utilisateur) {}

    public record UtilisateurDto(Long id, String nomComplet, String login, Role role, boolean actif) {
        public static UtilisateurDto from(Utilisateur u) {
            return new UtilisateurDto(u.getId(), u.getNomComplet(), u.getLogin(), u.getRole(), u.isActif());
        }
    }

    public record CreerUtilisateurRequest(
            @NotBlank String nomComplet,
            @NotBlank @Size(min = 3, max = 60) String login,
            @NotBlank @Size(min = 6) String motDePasse,
            @NotNull Role role) {}

    public record ModifierUtilisateurRequest(@NotBlank String nomComplet, @NotNull Role role, boolean actif) {}

    public record ChangerMotDePasseRequest(@NotBlank String ancien, @NotBlank @Size(min = 6) String nouveau) {}
}
