# Résumé de l'Implémentation JWT (Authentification Manuelle)

Ce document explique l'architecture de sécurité "Custom JWT" mise en place avant (ou en alternative à) Keycloak. Il détaille les étapes d'implémentation et le rôle précis de chaque fichier.

## 1. L'Architecture Globale
Contrairement à Keycloak (où un serveur externe gère tout), ici **c'est votre application qui fait tout** :
1.  Elle vérifie le mot de passe.
2.  Elle génère (signe) le Token.
3.  Elle valide le Token à chaque requête.

---

## 2. Le Rôle de Chaque Fichier Créé

Voici l'explication fichier par fichier, pour répondre aux questions type "À quoi ça sert ?".

### A. Le Cœur du Système (Infrastructure)

*   **`JwtService.java`**
    *   **Rôle** : C'est "l'Imprimeur" et le "Vérificateur" de billets.
    *   **Fonction** : Il contient la clé secrète. Il crée les String cryptés (Tokens) et vérifie s'ils sont valides/expirés.

*   **`JwtAuthenticationFilter.java`**
    *   **Rôle** : Le "Douanier" à l'entrée.
    *   **Fonction** : Intercepte **chaque** requête HTTP. Il regarde le header `Authorization`. Si le token est bon (via `JwtService`), il laisse entrer l'utilisateur dans le conteneur Spring Security.

*   **`SecurityConfig.java`**
    *   **Rôle** : Le "Chef de la Sécurité".
    *   **Fonction** : Il désactive les sessions (Stateless), configure les accès publics (`/auth/**`) vs privés, et place le Douanier (`JwtFilter`) au bon endroit.

### B. Le Service d'Authentification (Logique Métier)

*   **`AuthService.java`**
    *   **Rôle** : Le "Guichetier".
    *   **Fonction** : C'est lui qui reçoit le login/password, vérifie avec `AuthenticationManager`, et si c'est bon, demande au `JwtService` de générer les tokens.

*   **`RefreshTokenService.java`** & **`RefreshToken.java`**
    *   **Rôle** : La "Gestion de Session Longue Durée".
    *   **Fonction** : Stocke un token opaque en base de données. Permet à l'utilisateur de rester connecté (obtenir un nouveau JWT) sans ressaisir son mot de passe, et permet de le déconnecter (Logout) en supprimant ce token de la DB.

### C. Les DTOs (Objets de transport)

*   **`LoginRequest.java`** : Contient juste email + password.
*   **`RefreshTokenRequest.java`** : Contient le token de rafraîchissement envoyé par le client.
*   **`AuthResponse.java`** : La réponse JSON envoyée au client (accessToken, refreshToken, type...).

### D. Observabilité & Logs (ELK)

*   **`MdcLogFilter.java`**
    *   **Rôle** : L' "Étiqueteur".
    *   **Fonction** : Ajoute des étiquettes (User, Role, Endpoint) à chaque log pour qu'on puisse filtrer facilement dans Kibana.
    *   *Exemple* : "Qui a supprimé ce produit ?" -> On filtre sur `user:admin@test.com`.

*   **`logback-spring.xml`**
    *   **Rôle** : Le "Tuyau".
    *   **Fonction** : Envoie les logs au format JSON vers Logstash (port 5000) au lieu de juste les écrire dans la console.

---

## 3. Les Étapes d'Implémentation (Résumé)

1.  **Dépendances** : Ajout de `jjwt` (librairie JWT) dans le `pom.xml`.
2.  **Service JWT** : Création de `JwtService` pour gérer la crypto.
3.  **Filtre** : Création de `JwtAuthenticationFilter` pour valider les requêtes entrantes.
4.  **Configuration** : Mise en place de `SecurityConfig` pour activer le mode Stateless.
5.  **Gestion du Refresh** : Implémentation de la rotation de token (Sécurité avancée : un refresh token ne sert qu'une fois).
6.  **Tests** : Écriture de tests d'intégration (`TokenLifecycleIntegrationTest`) pour prouver que le cycle Login -> Refresh -> Logout fonctionne.

---

## 4. Comparaison Rapide pour le Professeur

| Feature | JWT "Maison" (Cette branche) | Keycloak (L'autre branche) |
| :--- | :--- | :--- |
| **Gestion Users** | Table SQL locale | Base Keycloak interne |
| **Mots de passe** | Stockés (hachés) chez nous | Stockés chez Keycloak |
| **Complexité Code** | Élevée (On code tout) | Faible (On configure) |
| **Contrôle** | Total | Délégué |

Ce document résume le travail effectué sur la branche actuelle (`feature-jwt-Kibana`).
