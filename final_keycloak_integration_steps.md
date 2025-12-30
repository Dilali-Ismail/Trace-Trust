# Checklist Finale & Intégration Java (Phase 4)

Puisque vous avez récupéré le travail de la Personne B, voici les étapes exactes à suivre pour valider l'infrastructure et préparer l'application Spring Boot.

## 1. Validation de l'Infrastructure (Docker)

Même si le code est là, il faut s'assurer que le conteneur a bien chargé la configuration.

**Vos actions :**
1.  Vérifiez que `docker-compose.yml` contient bien la ligne :
    `command: start-dev --import-realm`
    et le volume :
    `- ./keycloak/logistics-realm.json:/opt/keycloak/data/import/realm.json`
2.  Redémarrez proprement pour appliquer l'import :
    ```bash
    docker compose down -v
    docker compose up -d
    ```
    *(Le `-v` est important pour réinitialiser la base de données et forcer l'import du fichier JSON)*.

## 2. Le "Vrai" Test (Postman)

C'est la preuve ultime que Keycloak fonctionne. Vous devez obtenir un token JWT.

**Vos actions :**
1.  Ouvrez Postman.
2.  Créez une requête **POST** vers : `http://localhost:8080/realms/logistics-realm/protocol/openid-connect/token`
3.  Dans l'onglet **Body**, choisissez `x-www-form-urlencoded`.
4.  Ajoutez les clés/valeurs :
    *   `client_id` : `traceandtrust-front` (ou `traceandtrust-api` selon config)
    *   `username` : `admin_user` (ou user créé par Personne B)
    *   `password` : `admin123` (ou mot de passe défini)
    *   `grant_type` : `password`
5.  **Succès** : Si vous recevez `{ "access_token": "eyJh...", ... }`.

---

## 3. Phase 4 : Intégration Spring Boot (À faire ensuite)

C'est ici que le code change. Vous allez transformer l'API en "Resource Server".

**Les étapes techniques à réaliser :**

### A. Nettoyage (Suppression de l'ancien système)
Il faudra supprimer les classes qui géraient l'authentification manuelle :
*   `JwtService.java` (Plus besoin de générer/signer des tokens).
*   `JwtAuthenticationFilter.java` (Spring Security le fera tout seul).
*   `AuthController.java` (Les endpoints `/login` et `/register` ne sont plus gérés par l'API).
*   `RefreshTokenService.java` & `RefreshToken.java`.

### B. Dépendances (`pom.xml`)
Il faudra ajouter le starter officiel :
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### C. Configuration (`application.properties`)
Il faudra indiquer à Spring où vérifier les tokens :
```properties
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/logistics-realm
```

### D. Sécurité (`SecurityConfig.java`)
Il faudra réécrire la configuration pour utiliser le décodeur JWT standard et mapper les rôles Keycloak (qui sont dans `realm_access.roles`) vers les rôles Spring (`ROLE_ADMIN`, etc.).

---

**Dites-moi "Go code" quand vous avez validé l'étape 2 (Postman), et je générerai tout le code pour l'étape 3 !**
