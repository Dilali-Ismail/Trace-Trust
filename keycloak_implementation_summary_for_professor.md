# Rapport d'Implémentation : Sécurisation via Keycloak

Ce document résume la démarche technique adoptée pour intégrer Keycloak dans le projet TraceAndTrust. Il est structuré pour répondre aux questions d'un jury ou d'un professeur.

## 1. Pourquoi ce choix ? (La Justification)

Plutôt que de recoder un système d'authentification "maison" (avec gestion de mots de passe, hachage, sessions...), nous avons choisi de **déléguer** la sécurité à une solution IAM (Identity and Access Management) éprouvée : **Keycloak**.

**Avantages mis en avant :**
*   **Sécurité** : Nous ne stockons plus aucun mot de passe dans notre base de données.
*   **Standardisation** : Utilisation du protocole **OpenID Connect (OIDC)** et **OAuth2**.
*   **Scalabilité** : Keycloak gère le SSO (Single Sign-On) et la montée en charge.

---

## 2. Les Étapes Techniques Réalisées

### Étape 1 : Infrastructure (Docker)
Nous avons intégré Keycloak dans notre environnement conteneurisé.
*   **Fichier** : `docker-compose.yml`.
*   **Base de données** : Création d'une base dédiée `keycloak` (séparée de la base business) pour une isolation propre.
*   **Configuration** : Import automatique du realm au démarrage pour avoir un environnement reproductible.

### Étape 2 : Configuration du Domaine (Realm)
Nous avons configuré un royaume logique (`logistics-realm`) :
*   **Clients** :
    *   `traceandtrust-front` (Type **Public**) : Pour l'application Frontend (Angular).
    *   `traceandtrust-api` (Type **Confidential/Bearer-Only**) : Pour protéger l'API Spring Boot.
*   **Rôles** : Définition des rôles métier (`ADMIN`, `CLIENT`, `WAREHOUSE_MANAGER`).

### Étape 3 : Intégration Spring Boot (Resource Server)
Nous avons transformé l'API en "Resource Server" OAuth2.
*   **Dépendance** : Ajout de `spring-boot-starter-oauth2-resource-server`.
*   **Configuration** : Suppression de notre ancien code (`JwtUtils`, `AuthController`) qui était une source de vulnérabilités potentielles.
*   **SecurityConfig** : Configuration de Spring Security pour valider automatiquement la signature des tokens JWT émis par Keycloak.

### Étape 4 : La Synchronisation "Just-In-Time" (Point Clé)
C'est la partie la plus "intelligente" de l'intégration.
*   **Problème** : Keycloak gère l'identité (Qui je suis), mais notre base de données gère le métier (Mes commandes). Nous avions besoin de lier les deux.
*   **Solution** : Implémentation d'une méthode `syncUser(Jwt jwt)`.
    *   À chaque action critique (ex: Créer une commande), on vérifie si l'utilisateur existe dans notre base locale via son ID unique Keycloak.
    *   S'il n'existe pas, on le crée à la volée ("Onboarding").
    *   Cela garantit l'intégrité référentielle (Foreign Keys) de nos données.

---

## 3. Antisèche : Questions Possibles du Professeur

**Q : Pourquoi gardez-vous une table `User` si Keycloak gère les utilisateurs ?**
> **R** : Keycloak gère l'authentification (Login/Password). Notre table `User` gère les données **métier**. Nous avons besoin d'une table locale pour faire des jointures (ex: `Select * from Orders where user_id = ...`). Nous ne stockons plus de mot de passe, juste l'ID Keycloak.

**Q : Comment l'API vérifie-t-elle que le token n'est pas falsifié ?**
> **R** : Grâce au **Resource Server**. Au démarrage, l'API télécharge la clé publique de Keycloak (via l'URL `.well-known`). À chaque requête, elle utilise cette clé pour vérifier la signature cryptographique du JWT.

**Q : Que se passe-t-il si Keycloak tombe en panne ?**
> **R** : Plus personne ne peut se connecter (obtenir un token). C'est le principe d'un service centralisé. En production, on mettrait Keycloak en cluster (Haute Disponibilité).

**Q : Pourquoi avoir changé le port 8080 ?**
> **R** : Keycloak et Spring Boot utilisent tous les deux le port 8080 par défaut. Pour éviter le conflit de ports ('Port already in use'), nous avons déplacé l'API sur le port 8081.
