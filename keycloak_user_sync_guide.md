# Développeur B : Guide de Nettoyage & Synchronisation Utilisateur

Ce document détaille les étapes cruciales pour aligner votre base de données locale avec Keycloak (la source de vérité pour l'authentification) tout en conservant vos règles métier.

## 1. Tâche B.1 : Le Grand Nettoyage

**Objectif :** Supprimer le code "mort". Puisque Keycloak gère maintenant la connexion, toute votre logique d'authentification maison devient obsolète et dangereuse (car non maintenue).

**Actions à réaliser :**
Il faut supprimer (ou commenter pour archivage) les fichiers suivants :
*   `AuthController` : Les endpoints `/login` et `/register` n'existent plus chez vous. C'est Keycloak qui s'en charge.
*   `JwtUtils` : Votre API ne *génère* plus de tokens, elle ne fait que les *lire* (via le starter Spring OAuth2).
*   `RefreshTokenService` et l'entité `RefreshToken` : Keycloak gère le cycle de vie des tokens et le "Remember me".
*   `UserDetailsServiceImpl` : Spring Security ne va plus charger les users depuis votre DB pour le login, il va faire confiance au Token JWT.

## 2. Tâche B.2 : Refactor de l'Entité User

**Objectif :** Adapter votre table `users` à sa nouvelle fonction. Elle ne sert plus à "identifier" (login/password) mais à "stocker des données métier" (Commandes, Profil).

**Explication des changements :**
1.  **Suppression du password** : ⚠️ Critique. Ne stockez jamais de mots de passe inutiles. Keycloak est le seul à les connaître.
2.  **Ajout de `keycloakId`** : C'est le "pont" unique entre Keycloak et votre DB.
    *   L'email peut changer.
    *   Le `sub` (Subject ID) du token JWT (un UUID) ne change jamais. C'est lui qu'il faut stocker pour reconnaître l'utilisateur à coup sûr.

**Ce que vous devez modifier dans `User.java` :**
```java
// User.java
// Supprimer : private String password;
// Ajouter :
@Column(name = "keycloak_id", unique = true)
private String keycloakId; // Correspondra au claim "sub" du JWT
```

## 3. Tâche B.3 : Synchronisation "Just-In-Time" (Le Miroir)

**Concept :**
Quand un utilisateur se connecte pour la première fois via Keycloak, il existe "là-bas" mais pas "chez vous".
Si vous essayez de créer une commande (`Order`) liée à cet utilisateur, la Foreign Key va échouer.
Il faut donc une stratégie de "miroir" : on crée l'utilisateur localement à la volée, juste avant d'en avoir besoin.

**Implémentation recommandée :**
Créez une méthode dans votre `UserService` qui prend le JWT en entrée.

```java
public User syncUser(Jwt jwt) {
    // 1. Récupérer l'ID unique Keycloak (le plus fiable)
    String keycloakId = jwt.getSubject();
    
    // 2. Récupérer les infos utiles (Email, Nom...)
    String email = jwt.getClaim("email");
    
    // 3. Chercher par ID Keycloak (ou Email pour la migration)
    return userRepository.findByEmail(email) // Ou findByKeycloakId(keycloakId)
        .map(existingUser -> {
            // Mise à jour optionnelle : si l'utilisateur a changé de nom dans Keycloak
            if (!existingUser.getKeycloakId().equals(keycloakId)) {
                 existingUser.setKeycloakId(keycloakId);
                 return userRepository.save(existingUser);
            }
            return existingUser;
        })
        .orElseGet(() -> {
            // 4. Création à la volée ("Onboarding")
            User newUser = new User();
            newUser.setKeycloakId(keycloakId);
            newUser.setEmail(email);
            newUser.setActive(true);
            newUser.setRole(Role.CLIENT); // Rôle par défaut, ou extrait du JWT
            return userRepository.save(newUser);
        });
}
```

## 4. Tâche B.4 : Utilisation dans les Contrôleurs

**Objectif :** Ne plus jamais faire confiance à "l'utilisateur connecté" implicite, mais toujours passer par la synchro.

**Dans vos Contrôleurs (ex: `OrderController`) :**
Au lieu de juste faire `@AuthenticationPrincipal`, vous devez "résoudre" l'utilisateur local.

```java
@PostMapping
public ResponseEntity<?> createOrder(@AuthenticationPrincipal Jwt jwt, @RequestBody OrderDto dto) {
    // ÉTAPE CRITIQUE : Qui est cet utilisateur dans MA base de données ?
    User clientLocal = userService.syncUser(jwt);

    // Maintenant je peux lier ma commande
    SalesOrder order = new SalesOrder();
    order.setClient(clientLocal); // La FK sera correcte
    // ... suite de la logique
}
```

---

Ces 4 étapes garantissent que votre application reste **autonome** pour ses données métier (commandes, historique) tout en déléguant **100% de la sécurité** à Keycloak.
