package ma.uae.fpl.compteservice.controller;

import ma.uae.fpl.compteservice.entities.Compte;
import ma.uae.fpl.compteservice.service.CompteService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comptes")
public class CompteController {

    private final CompteService compteService;

    public CompteController(CompteService compteService) {
        this.compteService = compteService;
    }

    // --- ADMIN uniquement -------------------------------------------------

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Compte ajouter(@RequestBody Compte compte) {
        return compteService.ajouter(compte);
    }

    @PutMapping("/{id}")
    public Compte modifier(@PathVariable Long id, @RequestBody Compte compte) {
        return compteService.modifier(id, compte);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void supprimer(@PathVariable Long id) {
        compteService.supprimer(id);
    }

    @GetMapping
    public List<Compte> getAll() {
        return compteService.getAll();
    }

    // --- Tout utilisateur authentifie ------------------------------------

    @GetMapping("/{id}")
    public Compte getById(@PathVariable Long id) {
        return compteService.getById(id);
    }

    @GetMapping("/{id}/solde")
    public Map<String, Object> solde(@PathVariable Long id) {
        return Map.of("id", id, "solde", compteService.consulterSolde(id));
    }

    @PostMapping("/{id}/credit")
    public Compte crediter(@PathVariable Long id, @RequestParam BigDecimal montant) {
        return compteService.crediter(id, montant);
    }

    @PostMapping("/{id}/debit")
    public Compte debiter(@PathVariable Long id, @RequestParam BigDecimal montant) {
        return compteService.debiter(id, montant);
    }
}
