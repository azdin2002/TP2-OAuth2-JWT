package ma.uae.fpl.compteservice.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

import java.math.BigDecimal;

@Entity
public class Compte {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String nom;
    private String tel;
    private BigDecimal solde = BigDecimal.ZERO;

    public Compte() {
    }

    public Compte(Long id, String nom, String tel, BigDecimal solde) {
        this.id = id;
        this.nom = nom;
        this.tel = tel;
        this.solde = solde;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }

    public String getTel() { return tel; }
    public void setTel(String tel) { this.tel = tel; }

    public BigDecimal getSolde() { return solde; }
    public void setSolde(BigDecimal solde) { this.solde = solde; }
}
