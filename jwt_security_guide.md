# Guide d'Implémentation JWT - TraceAndTrust

Ce document résume l'architecture de sécurité mise en place dans le projet, les concepts clés du JWT et les étapes suivies pour l'implémentation robuste de l'authentification et de l'autorisation.

## 1. Concepts Fondamentaux

### Qu'est-ce que le JWT (JSON Web Token) ?
Un standard (RFC 7519) qui définit une manière compacte et sécurisée de transmettre des informations entre parties sous forme d'objet JSON. 
- **Stateless (Sans état)** : Le serveur n'a pas besoin de stocker la session en mémoire. Toutes les informations nécessaires (ID utilisateur, rôles) sont dans le token lui-même.
- **Signé numériquement** : Le token est signé avec une clé secrète, ce qui empêche sa modification par un tiers.

### Access Token vs Refresh Token
- **Access Token** : Durée de vie courte (ex: 15 min). Utilisé pour chaque requête API. Contient les rôles.
- **Refresh Token** : Durée de vie longue (ex: 7 jours). Stocké en base de données. Utilisé uniquement pour obtenir un nouvel Access Token.
- **Rotation** : À chaque rafraîchissement, l'ancien Refresh Token est invalidé et un nouveau est généré.

---

## 2. Étapes de l'Implémentation

### Étape 1 : Dépendances
Nous avons utilisé la bibliothèque `io.jsonwebtoken` (JJWT) pour la manipulation des tokens.
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
```

### Étape 2 : Le Service JWT (`JwtService.java`)
C'est le cœur technique. Il contient les méthodes pour :
- **Générer** un token à partir d'un `UserDetails`.
- **Extraire** le "subject" (email) et les claims (rôles).
- **Valider** si le token est expiré ou corrompu.

### Étape 3 : Le Filtre de Sécurité (`JwtAuthenticationFilter.java`)
C'est un "intercepteur" qui s'exécute à chaque requête :
1. Récupère le header `Authorization: Bearer <token>`.
2. Valide le token via `JwtService`.
3. Si valide, il injecte l'utilisateur dans le `SecurityContextHolder` de Spring.

### Étape 4 : Configuration de Spring Security (`SecurityConfig.java`)
Nous avons configuré la chaîne de filtres :
- **Désactivation du CSRF** (car nous sommes en stateless).
- **SessionCreationPolicy.STATELESS** : Pas de sessions HTTP.
- **Autorisations** : Définition des routes publiques `/auth/**` et protection du reste.
- **RBAC (Role-Based Access Control)** : Activation de `@EnableMethodSecurity`.

### Étape 5 : Gestion du Login et Refresh (`AuthService.java`)
Logique métier de connexion :
- Vérification du mot de passe avec `PasswordEncoder`.
- Génération du couple Access/Refresh tokens.
- Enregistrement du Refresh Token en base de données pour permettre la révocation.

---

## 3. Autorisation et Isolation

### RBAC avec `@PreAuthorize`
Utilisé sur les contrôleurs pour restreindre l'accès par rôle.
```java
@PreAuthorize("hasRole('ADMIN')")
public void deleteSomething() { ... }
```

### Isolation des Données (Ownership)
Vérification que l'utilisateur possède la ressource qu'il tente de modifier via le `OwnershipService`.
```java
@PreAuthorize("hasRole('CLIENT') and @ownershipService.isOrderOwner(#id, authentication.name)")
```

---

## 4. Glossaire Technique

| Terme | Définition |
| :--- | :--- |
| **Claim** | Une information contenue dans le JWT (ex: "sub", "role"). |
| **Signing Key** | Clé secrète utilisée par le serveur pour signer les tokens. |
| **Bearer** | Type de schéma d'authentification indiquant que le porteur du token est authentifié. |
| **MDC** | Mapped Diagnostic Context, utilisé pour enrichir les logs avec l'email utilisateur (ELK). |

---

## 5. Résumé du Flux
1. Le client envoie ses credentials (`/login`).
2. Le serveur valide et renvoie les tokens.
3. Le client stocke les tokens et les envoie dans le header `Authorization` pour les appels suivants.
4. Le `JwtAuthenticationFilter` intercepte l'appel et identifie l'utilisateur.
5. `@PreAuthorize` vérifie si l'utilisateur a le rôle nécessaire.
6. Si le token expire, le client appelle `/refresh`.
