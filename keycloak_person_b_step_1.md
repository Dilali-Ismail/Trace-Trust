# Personne B : Configuration des Clients et Rôles

Bienvenue **Personne B** ! L'infrastructure est prête, à vous de jouer pour connecter nos applications.

## Étape 1 : Le Client Frontend (Angular)

Ce client permet à l'utilisateur de se connecter via le navigateur.

1.  Allez dans **Clients** (menu de gauche) > **Create client**.
2.  **General Settings** :
    *   **Client type** : `OpenID Connect`.
    *   **Client ID** : `traceandtrust-front`.
    *   **Name** : `TraceAndTrust Frontend`.
    *   Cliquez sur **Next**.
3.  **Capability config** :
    *   **Client authentication** : `OFF` (C'est un client Public, pas de secret stocké dans le navigateur).
    *   **Standard flow** : `ON` (C'est le code flow standard).
    *   **Direct access grants** : `OFF` (On ne veut pas que l'app manipule les mots de passe).
    *   Cliquez sur **Next**.
4.  **Login settings** :
    *   **Valid redirect URIs** : `http://localhost:4200/*` (L'URL où Keycloak renvoie l'utilisateur après login).
    *   **Web origins** : `+` (Autorise CORS pour toutes les URIs de redirection validées).
    *   Cliquez sur **Save**.

## Étape 2 : Le Client API (Spring Boot)

Ce client représente votre Backend. Il sert à valider les tokens.

1.  Allez dans **Clients** > **Create client**.
2.  **General Settings** :
    *   **Client ID** : `traceandtrust-api`.
    *   Cliquez sur **Next**.
3.  **Capability config** :
    *   **Client authentication** : `ON` (Confidential).
    *   **Authorization** : `OFF`.
    *   **Standard flow** : `OFF` (L'API ne se connecte pas elle-même, elle reçoit des tokens).
    *   **Service accounts roles** : `ON` (Utile si l'API doit parler à Keycloak plus tard).
    *   Cliquez sur **Next**.
4.  **Login settings** :
    *   Laissez vide ou mettez des valeurs par défaut, ce n'est pas critique pour un bearer-only.
    *   Cliquez sur **Save**.

## Étape 3 : Création des Rôles Métier

Keycloak va gérer vos rôles `ADMIN`, `WAREHOUSE_MANAGER` et `CLIENT`.

1.  Allez dans **Realm roles** (menu de gauche) > **Create role**.
2.  Créez les 3 rôles suivants (répétez l'opération) :
    *   Role name : `ADMIN`
    *   Role name : `WAREHOUSE_MANAGER`
    *   Role name : `CLIENT`

## Étape 4 : Création des Groupes (Pour simplifier la gestion)

1.  Allez dans **Groups** (menu de gauche) > **Create group**.
2.  Nom : `Admins`.
    *   Cliquez sur le groupe créé.
    *   Onglet **Role mapping**.
    *   Cliquez sur **Assign role**.
    *   Cochez `ADMIN` et cliquez sur **Assign**.
3.  Répétez pour :
    *   Groupe `Managers` -> Rôle `WAREHOUSE_MANAGER`.
    *   Groupe `Clients` -> Rôle `CLIENT`.

---

**Vérification :**
Allez dans **Clients**, vous devez voir `traceandtrust-front` et `traceandtrust-api`.
Allez dans **Realm roles**, vous devez voir vos 3 rôles.

Dites "**pass**" quand c'est fait, nous créerons ensuite les utilisateurs de test !
