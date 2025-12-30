# Nouvelle Fonction `createSalesOrder` Sécurisée

Voici le code exact à mettre dans votre contrôleur, avec l'explication détaillée de chaque ligne.

## Le Code Java

```java
@PostMapping
public ResponseEntity<SalesOrderDto> createSalesOrder(
        @AuthenticationPrincipal Jwt jwt,                        // (1) Le Token
        @Valid @RequestBody CreateSalesOrderRequest request      // (2) Les Données
) {

    // (3) Synchronisation & Identification
    User user = userService.syncUser(jwt); 
    
    // (4) Appel Métier avec l'ID sécurisé
    SalesOrderDto createdOrder = salesOrderService.createSalesOrder(request, user.getId());
    
    return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
}
```

---

## Explication Pas-à-Pas

### (1) `@AuthenticationPrincipal Jwt jwt`
*   **Avant** : Vous utilisiez `@RequestHeader("X-Actor-ID")`. C'était dangereux car n'importe qui pouvait envoyer un faux ID.
*   **Maintenant** : Spring Security injecte automatiquement le **Token JWT** validé. C'est votre preuve d'identité infalsifiable `(Badge Keycloak)`.

### (2) `@Valid @RequestBody ...`
*   Rien ne change ici, c'est toujours le corps de la requête (le JSON de la commande).

### (3) `User user = userService.syncUser(jwt);`
*   C'est la ligne la plus importante.
*   On donne le **Badge Keycloak** (`jwt`) au service.
*   Le service vérifie dans la base de données locale :
    *   *"Est-ce que je connais ce badge ?"*
    *   **Si OUI** : Il retourne l'utilisateur existant.
    *   **Si NON** : Il le crée immédiatement.
*   **Résultat** : On obtient un objet `User` qui vient de **VOTRE** base de données.

### (4) `salesOrderService.createSalesOrder(..., user.getId())`
*   On appelle votre logique métier habituelle.
*   **La différence** : Au lieu de passer un `actorId` qui venait d'un Header non fiable, on passe `user.getId()` qui vient directement de la base de données, certifié par le token.
*   La commande sera donc liée au bon client, sans risque d'erreur ou de fraude.
