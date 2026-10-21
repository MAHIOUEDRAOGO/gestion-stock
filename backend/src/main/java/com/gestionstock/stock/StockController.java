package com.gestionstock.stock;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.gestionstock.produit.ProduitDtos.ProduitDto;
import com.gestionstock.produit.ProduitRepository;
import com.gestionstock.stock.StockDtos.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockService stockService;
    private final ProduitRepository produitRepository;

    /** Saisie d'une entrée ou d'une sortie (avec motif obligatoire). */
    @PostMapping("/mouvements")
    @PreAuthorize("hasAnyRole('MAGASINIER','ADMIN')")
    public MouvementDto enregistrer(@Valid @RequestBody MouvementRequest req) {
        return stockService.enregistrerManuel(req);
    }

    @GetMapping("/mouvements")
    @PreAuthorize("hasAnyRole('MAGASINIER','ACHATS','DIRECTION','ADMIN')")
    public Page<MouvementDto> historique(
            @RequestParam(required = false) Long produitId,
            @RequestParam(required = false) TypeMouvement type,
            @RequestParam(required = false) MotifMouvement motif,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate du,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate au,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int taille) {
        return stockService.rechercher(produitId, type, motif, du, au, page, taille);
    }

    /** Produits dont le stock est inférieur ou égal au seuil d'alerte. */
    @GetMapping("/alertes")
    public List<ProduitDto> alertes() {
        return produitRepository.findEnAlerte().stream().map(ProduitDto::from).toList();
    }

    @GetMapping("/motifs")
    public List<MotifDto> motifs() {
        return Arrays.stream(MotifMouvement.values())
                .filter(m -> !m.isAutomatique())
                .map(m -> new MotifDto(m, m.getType()))
                .toList();
    }
}
