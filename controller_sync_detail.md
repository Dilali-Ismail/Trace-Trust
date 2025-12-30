# Explication Détaillée : Synchronisation Keycloak <-> Base Locale

Vous avez tout compris sur le principe ! 💡
Juste une petite nuance importante : ce n'est pas **Keycloak** qui utilise le service, c'est **votre Application Spring Boot**. Keycloak est juste le "distributeur de badges" à l'extérieur. C'est votre application, à l'intérieur, qui décide de vérifier le badge et de noter le nom du visiteur dans son registre.

Voici l'explication ligne par ligne demandée.

---

## 1. Dans le Contrôleur (`OrderController`)

C'est le point d'entrée. L'utilisateur veut créer une commande.

```java
@PostMapping // 1. Définit que cette méthode répond aux requêtes HTTP POST
public ResponseEntity<?> createOrder(
    @AuthenticationPrincipal Jwt jwt, // 2. Spring injecte le TOKEN (le badge) ici
    @RequestBody OrderDto dto         // 3. Spring injecte les données de la commande (JSON)
) {
    
    // 4. APPEL CRITIQUE : "Qui est-ce ?"
    // Le contrôleur donne le badge (jwt) au service et dit :
    // "Trouve-moi le dossier de cet utilisateur dans NOTRE base de données.
    // S'il n'existe pas, crée-le."
    User user = userService.syncUser(jwt); 

    // À partir d'ici, 'user' est un objet Java relié à votre table 'users' locale.
    // On peut donc l'utiliser pour faire des relations (Foreign Keys).

    SalesOrder order = new SalesOrder();
    
    // 5. LIAISON : On attache la commande à cet utilisateur spécifique
    // C'est ce qui permettra plus tard de faire : order.getClient().getName()
    order.setClient(user); 

    // ... (suite de la logique métier, mapping DTO, calculs...)

    return ResponseEntity.ok(orderRepository.save(order));
}
```

---

## 2. Dans le Service (`UserService.syncUser`)

C'est la méthode "magique" qui fait le pont.

```java
public User syncUser(Jwt jwt) {
    // 1. Extraction de l'ID Unique Keycloak
    // jwt.getSubject() retourne le champ "sub" du token. 
    // C'est un UUID (ex: "f08b2444-f78e...") qui ne change JAMAIS pour une personne donnée.
    String keycloakId = jwt.getSubject();

    // 2. Extraction de l'email
    // On lit le champ "email" dans le token.
    String email = jwt.getClaim("email");

    // 3. Recherche dans la base locale
    // On demande à Hibernate : "As-tu quelqu'un avec cet email ?"
    return userRepository.findByEmail(email) 
            
            // 4. CAS A : L'utilisateur EXISTE déjà (.map)
            .map(existingUser -> {
                // On vérifie si son ID Keycloak est bien à jour
                if (!existingUser.getKeycloakId().equals(keycloakId)) {
                    existingUser.setKeycloakId(keycloakId);
                    return userRepository.save(existingUser); // On sauvegarde la mise à jour
                }
                return existingUser; // On retourne l'utilisateur existant
            })
            
            // 5. CAS B : L'utilisateur n'existe pas (.orElseGet)
            .orElseGet(() -> {
                User newUser = new User();
                // On remplit la nouvelle fiche avec les infos du badge Keycloak
                newUser.setKeycloakId(keycloakId);
                newUser.setEmail(email);
                newUser.setActive(true);
                newUser.setRole(Role.CLIENT); 
                
                // On l'enregistre dans la base locale pour la première fois
                return userRepository.save(newUser);
            });
}
```

### En résumé
*   **Le Contrôleur** dit : *"J'ai besoin d'un utilisateur local pour valider cette commande, débrouille-toi avec ce token."*
*   **Le Service** dit : *"Je regarde dans le registre. Il est là ? Je le rends. Il n'est pas là ? Je le crée et je le rends."*
*   **La Base de Données** est contente car toutes les clés étrangères sont respectées.
