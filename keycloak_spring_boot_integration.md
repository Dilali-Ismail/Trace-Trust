# Guide d'Intégration Spring Boot & Keycloak (Resource Server)

Ce guide détaille comment transformer votre application TraceAndTrust pour qu'elle délègue toute l'authentification à Keycloak.

## 1. Concepts Clés

### Resource Server (Serveur de Ressources)
Votre API Spring Boot devient un **Resource Server**.
*   **Avant** : Elle gérait tout (Login, validation JWT, stockage users).
*   **Maintenant** : Elle ne fait que **vérifier** les badges (Tokens). Elle fait confiance à Keycloak (l'Authorization Server) pour émettre ces badges.

### JWT Decoder
Spring Security intègre nativement un décodeur. Il va :
1.  Télécharger la clé publique de Keycloak (automatiquement).
2.  Vérifier la signature du Token reçu.
3.  Vérifier la date d'expiration.

### Role Converter
Keycloak met les rôles dans un champ spécifique du JSON (`realm_access.roles`). Spring Security s'attend à les trouver ailleurs ou sous un autre format. Nous devrons écrire un petit "traducteur" (Converter) pour que `@PreAuthorize("hasRole('ADMIN')")` continue de fonctionner.

---

## 2. Étape par Étape

### Étape 1 : Vérification Préalable (Postman)
Avant de toucher au code, assurez-vous que Keycloak donne bien des tokens.
*   **URL** : `http://localhost:8080/realms/logistics-realm/protocol/openid-connect/token`
*   **Method** : POST
*   **Body (x-www-form-urlencoded)** :
    *   `client_id`: traceandtrust-front
    *   `username`: client_user (ou admin_user)
    *   `password`: (le mot de passe défini)
    *   `grant_type`: password
*   **Résultat attendu** : Un JSON avec `access_token`. Copiez ce token pour plus tard.

### Étape 2 : Nettoyage (Cleanup)
Nous n'avons plus besoin de gérer manuellement le JWT.
*   **À supprimer (ou commenter)** :
    *   `JwtService.java`
    *   `JwtAuthenticationFilter.java`
    *   Les entités `RefreshToken` (si vous voulez nettoyer la BDD).
    *   Les endpoints `/auth/login` et `/auth/register` dans `AuthController` (c'est Keycloak qui gère ça maintenant, ou le Front).

### Étape 3 : Dépendances (`pom.xml`)
Ajoutez le starter officiel pour OAuth2 Resource Server.

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

### Étape 4 : Configuration (`application.properties`)
Dites à Spring où se trouve Keycloak.

```properties
# URL de l'émetteur (Issuer) - Spring ira chercher /.well-known/openid-configuration ici
spring.security.oauth2.resourceserver.jwt.issuer-uri=http://localhost:8080/realms/logistics-realm
# (Optionnel) JWK Set URI si l'issuer n'est pas accessible directement
# spring.security.oauth2.resourceserver.jwt.jwk-set-uri=http://localhost:8080/realms/logistics-realm/protocol/openid-connect/certs
```

### Étape 5 : La Classe `SecurityConfig`
C'est le gros morceau. On remplace notre filtre maison par la config standard.

**Points clés à modifier :**
1.  Supprimer `.addFilterBefore(jwtAuthFilter...)`.
2.  Activer `.oauth2ResourceServer(...)`.
3.  Configurer le **Converter** pour les rôles.

#### Exemple de Converter (Code à ajouter)
```java
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    // Par défaut Spring cherche "SCOPE_...", on veut garder nos rôles tels quels ou préfixés ROLE_
    grantedAuthoritiesConverter.setAuthoritiesClaimName("realm_access"); 
    // Attention : realm_access est un objet complexe, il faudra peut-être une extraction manuelle via une classe dédiée si la structure est imbriquée.
    
    // Approche recommandée pour Keycloak :
    JwtAuthenticationConverter jwtConverter = new JwtAuthenticationConverter();
    jwtConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());
    return jwtConverter;
}
```

(Je vous fournirai le code exact de `KeycloakRoleConverter` dans la prochaine étape d'implémentation).

### Étape 6 : Test de l'API
1.  Lancez l'appli (`mvn spring-boot:run`).
2.  Prenez le token récupéré à l'Étape 1.
3.  Faites un appel API (ex: `GET http://localhost:8081/api/sales-orders`) avec Header `Authorization: Bearer <votre_token>`.
4.  Si vous avez 200 OK -> **Succès !**

---

## Prêt à coder ?
Si ce plan vous convient, dites "**Go**" et je générerai le code pour :
1.  Le `pom.xml`.
2.  Le `KeycloakRoleConverter.java`.
3.  La nouvelle `SecurityConfig.java`.
