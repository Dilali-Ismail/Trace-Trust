# Guide de Nettoyage (Facultatif)

Si vous décidez plus tard de supprimer l'ancien code d'authentification pour alléger le projet, voici les étapes à suivre.

## 1. Nettoyer `UserController`
Dans `src/main/java/org/usermanagement/traceandtrust/controller/UserController.java` :
- Supprimez la méthode `login`.
- Supprimez l'import `LoginRequest`.

## 2. Supprimer les Fichiers Inutiles
Supprimez les fichiers suivants qui ne sont plus utilisés par Keycloak :
- `src/main/java/org/usermanagement/traceandtrust/security/JwtUtils.java` (si existant)
- `src/main/java/org/usermanagement/traceandtrust/dto/LoginRequest.java`
- `src/main/java/org/usermanagement/traceandtrust/dto/JwtResponse.java` (si existant)
- `src/main/java/org/usermanagement/traceandtrust/service/RefreshTokenService.java` (si existant)

## 3. Nettoyer le `pom.xml`
Si vous utilisiez une librairie JWT manuelle (comme `jjwt`), vous pouvez retirer ces dépendances car `spring-boot-starter-oauth2-resource-server` gère tout maintenant.

Cherchez et supprimez ceci :
```xml
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    ...
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    ...
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    ...
</dependency>
```

## 4. Nettoyer `UserServiceImpl`
Dans `src/main/java/org/usermanagement/traceandtrust/service/UserServiceImpl.java` :
- Supprimez la méthode `login(LoginRequest request)`.
- Supprimez la vérification du mot de passe (Keycloak s'en charge).

---
**Note :** Ces actions ne sont pas urgentes. Le code ancien peut cohabiter avec le nouveau tant qu'il n'est pas appelé.
