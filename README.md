# TP 2 — Sécurisation d'une API REST avec OAuth2 et JWT

Projet réalisé dans le cadre du Master DevOps & Cloud Computing.

Ce dépôt contient deux applications Spring Boot qui mettent en œuvre une architecture de sécurisation d'API REST avec des tokens JWT signés en RSA.

## 1. Architecture

| Projet | Port | Rôle |
|---|---:|---|
| `security-service` | `8088` | Authentification des utilisateurs, génération et renouvellement des tokens JWT avec la clé privée RSA. |
| `compte-service` | `8082` | Microservice de gestion des comptes et validation des Access Tokens avec la clé publique RSA. |

### Technologies

- Java 17+
- Spring Boot 3.x
- Spring Security
- OAuth2 Resource Server
- JWT / RSA 2048 bits
- Maven
- Spring Data JPA
- H2 Database
- Postman

## 2. Structure du dépôt

```text
TP2-OAuth2-JWT/
├── README.md
├── .gitignore
├── security-service/
│   ├── pom.xml
│   └── src/
└── compte-service/
    ├── pom.xml
    └── src/
```

## 3. Prérequis

Avant de lancer les applications, installer :

- JDK 17 ou une version compatible avec les projets ;
- Maven (ou utiliser le Maven Wrapper si présent) ;
- Git ;
- Postman (pour tester les endpoints).

Vérifier les installations :

```bash
java -version
mvn -version
git --version
```

## 4. Configuration des clés RSA

Le service de sécurité signe les tokens avec la clé privée. Le microservice des comptes utilise uniquement la clé publique pour vérifier leur signature.

Les fichiers attendus sont :

```text
security-service/src/main/resources/certs/
├── privateKey.pem
└── publicKey.pem

compte-service/src/main/resources/certs/
└── publicKey.pem
```

**Important :**
- Ne jamais publier `privateKey.pem` sur GitHub.
- Le fichier `publicKey.pem` peut être partagé pour permettre la vérification des tokens.
- Les deux applications doivent utiliser la même paire de clés.
- Si une nouvelle paire est générée, remplacer la clé publique dans les deux projets et conserver la clé privée uniquement dans `security-service`.

Si les fichiers de clés ne sont pas présents après le clonage, générer une paire RSA avec la classe `RSAKeyPairToFile` du projet, puis placer les fichiers dans les emplacements indiqués ci-dessus.

## 5. Configuration et démarrage

Vérifier les paramètres dans les fichiers `application.properties` des deux projets, notamment les ports et les chemins des clés.

### Terminal 1 — Service de sécurité

```bash
cd security-service
mvn spring-boot:run
```

Service disponible sur :

```text
http://localhost:8088
```

### Terminal 2 — Microservice des comptes

```bash
cd compte-service
mvn spring-boot:run
```

Service disponible sur :

```text
http://localhost:8082
```

Les deux applications doivent être démarrées pour tester le parcours complet.

## 6. Utilisateurs de test

Les utilisateurs suivants sont configurés en mémoire dans `security-service` pour les tests du TP :

| Identifiant | Mot de passe | Rôles |
|---|---|---|
| `admin` | `1234` | `ADMIN`, `USER` |
| `user1` | `1234` | `USER` |

Ces identifiants sont des identifiants de démonstration pour l'environnement pédagogique. Ne pas les réutiliser dans un environnement réel.

## 7. Tests avec Postman

### 7.1 Authentification

Envoyer une requête :

```http
POST http://localhost:8088/Login
Content-Type: application/x-www-form-urlencoded
```

Paramètres du formulaire :

```text
username=admin
password=1234
```

La réponse contient :

```json
{
  "access_token": "<ACCESS_TOKEN>",
  "refresh_token": "<REFRESH_TOKEN>"
}
```

### 7.2 Renouvellement du token

Envoyer :

```http
POST http://localhost:8088/RefreToken
Content-Type: application/x-www-form-urlencoded
```

Paramètre :

```text
refreshToken=<REFRESH_TOKEN>
```

Le service retourne un nouvel Access Token si le Refresh Token est valide.

### 7.3 Appels au microservice des comptes

Pour les endpoints protégés, ajouter l'en-tête :

```http
Authorization: Bearer <ACCESS_TOKEN>
```

Exemple de création d'un compte :

```http
POST http://localhost:8082/comptes
Content-Type: application/json
Authorization: Bearer <ACCESS_TOKEN>
```

Corps JSON :

```json
{
  "nom": "Ali",
  "tel": "0600000000",
  "solde": 1000
}
```

### 7.4 Endpoints et autorisations

| Opération | Méthode et endpoint | ADMIN | USER |
|---|---|---:|---:|
| Ajouter un compte | `POST /comptes` | Oui | Non |
| Modifier un compte | `PUT /comptes/{id}` | Oui | Non |
| Supprimer un compte | `DELETE /comptes/{id}` | Oui | Non |
| Lister les comptes | `GET /comptes` | Oui | Non |
| Consulter un compte | `GET /comptes/{id}` | Oui | Oui |
| Consulter le solde | `GET /comptes/{id}/solde` | Oui | Oui |
| Créditer un compte | `POST /comptes/{id}/credit?montant=100` | Oui | Oui |
| Débiter un compte | `POST /comptes/{id}/debit?montant=100` | Oui | Oui |

Les requêtes protégées sans token valide doivent être refusées. Un utilisateur authentifié sans le rôle requis reçoit un refus d'autorisation.

## 8. Durée de validité des tokens

Les durées configurées pour ce TP sont :

- Access Token : 5 minutes ;
- Refresh Token : 30 minutes.

Le microservice des comptes accepte uniquement les Access Tokens. Le Refresh Token est réservé à l'endpoint de renouvellement du service de sécurité.

## 9. Base de données

Le projet `compte-service` utilise H2 en mémoire pour stocker les comptes pendant l'exécution de l'application. Les données peuvent être réinitialisées lorsque l'application redémarre, selon la configuration JPA utilisée.

## 10. Auteur

**Azzeddine Salmoun**  
Master DevOps & Cloud Computing  
Faculté Polydisciplinaire à Larache  
Université Abdelmalek Essaâdi

---

*Projet académique — TP 2 : Sécurisation d'une API REST avec OAuth2 et JWT.*
