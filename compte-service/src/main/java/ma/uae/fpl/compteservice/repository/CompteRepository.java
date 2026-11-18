package ma.uae.fpl.compteservice.repository;

import ma.uae.fpl.compteservice.entities.Compte;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompteRepository extends JpaRepository<Compte, Long> {
}
