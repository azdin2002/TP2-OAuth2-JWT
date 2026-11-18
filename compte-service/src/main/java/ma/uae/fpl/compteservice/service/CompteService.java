package ma.uae.fpl.compteservice.service;

import ma.uae.fpl.compteservice.entities.Compte;
import ma.uae.fpl.compteservice.repository.CompteRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;

@Service
@Transactional
public class CompteService {

    private final CompteRepository compteRepository;

    public CompteService(CompteRepository compteRepository) {
        this.compteRepository = compteRepository;
    }

    public Compte ajouter(Compte compte) {
        compte.setId(null);
        if (compte.getSolde() == null) {
            compte.setSolde(BigDecimal.ZERO);
        }
        if (compte.getSolde().signum() < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le solde initial ne peut pas etre negatif");
        }
        return compteRepository.save(compte);
    }

    public Compte modifier(Long id, Compte donnees) {
        Compte compte = getById(id);
        compte.setNom(donnees.getNom());
        compte.setTel(donnees.getTel());
        if (donnees.getSolde() != null) {
            if (donnees.getSolde().signum() < 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le solde ne peut pas etre negatif");
            }
            compte.setSolde(donnees.getSolde());
        }
        return compteRepository.save(compte);
    }

    public void supprimer(Long id) {
        compteRepository.delete(getById(id));
    }

    @Transactional(readOnly = true)
    public Compte getById(Long id) {
        return compteRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Compte introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public List<Compte> getAll() {
        return compteRepository.findAll();
    }

    @Transactional(readOnly = true)
    public BigDecimal consulterSolde(Long id) {
        return getById(id).getSolde();
    }

    public Compte crediter(Long id, BigDecimal montant) {
        verifierMontant(montant);
        Compte compte = getById(id);
        compte.setSolde(compte.getSolde().add(montant));
        return compteRepository.save(compte);
    }

    public Compte debiter(Long id, BigDecimal montant) {
        verifierMontant(montant);
        Compte compte = getById(id);
        if (compte.getSolde().compareTo(montant) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Solde insuffisant");
        }
        compte.setSolde(compte.getSolde().subtract(montant));
        return compteRepository.save(compte);
    }

    private void verifierMontant(BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Le montant doit etre strictement positif");
        }
    }
}
