# Plan de Division des Tâches : Implémentation Keycloak (2 Personnes)

Ce document propose une stratégie de répartition du travail pour intégrer Keycloak dans le projet TraceAndTrust. Le travail est divisé entre **l'Administrateur du Realm (Personne A)** et **l'Intégrateur Fonctionnel (Personne B)**.

---

## 1. Définitions et Concepts Clés

Avant de commencer, voici un rappel des termes techniques utilisés :

*   **Realm (Royaume)** : C'est un espace cloisonné dans Keycloak qui gère ses propres utilisateurs, applications, rôles et clés. Nous utiliserons `logistics-realm` (et non le `master` qui sert à l'administration du serveur).
*   **Client OIDC** : Définit une application qui "parle" à Keycloak.
    *   *Public (Frontend)* : Ne peut pas garder un secret (ex: React, Angular). Utilise le flux "Authorization Code" avec PCKE.
    *   *Bearer-only / Confidential (API)* : Sert à protéger le Backend. Vérifie la signature du token.
*   **Access Token (JWT)** : Le "badge" d'accès courte durée (ex: 15min). Contient les infos utilisateur et les rôles.
*   **Refresh Token** : Le "ticket de renouvellement" longue durée (ex: 7 jours). Permet de rester connecté sans ressaisir son mot de passe.
*   **Flow** : Le protocole d'échange (ex: redirection vers page de login -> retour avec code -> échange code contre token).

---

## 2. Répartition des Tâches

### 👤 Personne A : Administrateur du Realm (Infrastructure & Sécurité)
**Objectif** : Mettre en place le serveur, créer le conteneur logique (Realm), et configurer les politiques de sécurité globales.

#### Étape A1 : Initialisation de l'Infrastructure
1.  **Installation** : Configurer le service `keycloak` (via Docker/Docker Compose) avec une base de données dédiée (PostgreSQL).
2.  **Premier accès** : Créer le compte admin initial du serveur.

#### Étape A2 : Configuration du Realm `logistics-realm`
1.  **Création du Realm** : Créer un nouveau realm nommé `logistics-realm`.
    *   *Note* : Ne jamais rien configurer dans `master`.
2.  **Paramètres de Login** :
    *   Activer "User registration" (si auto-inscription désirée).
    *   Activer "Forgot password".
    *   Configurer l'email comme identifiant principal ("Email as username").

#### Étape A3 : Sécurité des Tokens & Sessions
1.  **Réglage des délais (Token Lifespan)** :
    *   *Access Token* : Régler à 15 minutes (sécurité accrue).
    *   *SSO Session Idle* : Régler à l'équivalent de la durée du Refresh Token (ex: 7 jours).
2.  **Signature (Keys)** : Vérifier que les algorithmes de signature (ex: RSA RS256) sont actifs.

#### Étape A4 : Audit & Monitoring
1.  **Activation des Events** : Activer "Save Events" dans la section "Events" du Realm.
2.  **Types d'événements** : Cocher les événements critiques (LOGIN, LOGIN_ERROR, REGISTER, LOGOUT).
3.  **Logs** : Vérifier que les erreurs s'affichent bien dans la console serveur.

---

### 👤 Personne B : Intégrateur Fonctionnel (Clients, Rôles, Utilisateurs)
**Objectif** : Modéliser l'organisation de TraceAndTrust dans Keycloak et configurer les accès pour les applications.

#### Étape B1 : Configuration des Clients OIDC
1.  **Client Frontend (`traceandtrust-front`)** :
    *   *Type* : Public (pas de `client_secret`).
    *   *Valid Redirect URIs* : L'URL de votre front (ex: `http://localhost:4200/*`).
    *   *Web Origins* : `+` (CORS).
    *   *Flow* : Standard Flow (Authorization Code) activé.
2.  **Client API (`traceandtrust-api`)** :
    *   *Type* : Confidential (ou Bearer-Only selon version Keycloak).
    *   *Rôle* : Valider les tokens reçus.

#### Étape B2 : Modélisation des Rôles (RBAC)
1.  **Création des Rôles du Realm** :
    *   `ADMIN`
    *   `WAREHOUSE_MANAGER`
    *   `CLIENT`
2.  **Création des Groupes (Recommandé)** :
    *   Groupe `Admins` -> Mapper avec le rôle `ADMIN`.
    *   Groupe `Managers` -> Mapper avec le rôle `WAREHOUSE_MANAGER`.
    *   Groupe `Clients` -> Mapper avec le rôle `CLIENT`.

#### Étape B3 : Gestion des Utilisateurs Test
1.  **Créer des utilisateurs représentatifs** :
    *   `admin_user` (assigné au groupe Admins).
    *   `manager_user` (assigné au groupe Managers).
    *   `client_user` (assigné au groupe Clients).
2.  **Mots de Passe** : Définir des mots de passe temporaires (et décocher "Temporary" pour faciliter les tests dev).

#### Étape B4 : Mappers (Enrichissement du Token)
1.  **Token Mapper** : S'assurer que les rôles du Realm remontent bien dans le token JWT.
2.  **User Attribute Mapper** : Si besoin, ajouter des attributs spécifiques (ex: `business_id`) dans le token pour que l'API les reçoive.

---

## 3. Phase de Vérification (A & B ensemble)

Une fois les configurations terminées, les deux personnes se réunissent pour tester le flux complet :

1.  **Test Login** :
    *   Accéder à l'URL de login du frontend.
    *   Vérifier la redirection vers la page de login Keycloak.
    *   Se connecter avec `admin_user`.
2.  **Inspection du Token (via jwt.io)** :
    *   Récupérer le token généré.
    *   Vérifier la présence de l'Issuer (`iss`: .../realms/logistics-realm).
    *   Vérifier la présence du rôle `ADMIN`.
    *   Vérifier l'expiration (approx 15min).
3.  **Test Logout** :
    *   Vérifier que le Refresh Token est bien révoqué (session terminée dans la console admin Keycloak).

---

## 4. Prochaines Étapes Techniques (Hors scope Keycloak pur)

*   Intégration Spring Boot (Resource Server).
*   Intégration Frontend (Redirection Login).
