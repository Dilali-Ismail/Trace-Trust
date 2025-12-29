# Tâches Intégrateur (Personne B) : Configuration Fonctionnelle

Ce guide détaille comment structurer le realm `logistics-realm` pour l'application TraceAndTrust.

## Pré-requis
Attendre que la Personne A ait créé le realm `logistics-realm`.

## Étape 1 : Création des Clients OIDC

### 1. Client Frontend (Angular/React)
*   Allez dans **Clients** > **Create client**.
*   **Client type** : `OpenID Connect`.
*   **Client ID** : `traceandtrust-front`.
*   **Next**.
*   **Client authentication** : `OFF` (Public).
*   **Standard flow** : `ON` (Authorization Code).
*   **Next**.
*   **Valid redirect URIs** : `http://localhost:4200/*` (et URL de prod).
*   **Web origins** : `http://localhost:4200` (Pour CORS).
*   **Save**.

### 2. Client API (Spring Boot)
*   Allez dans **Clients** > **Create client**.
*   **Client ID** : `traceandtrust-api`.
*   **Client authentication** : `ON` (Confidential/Bearer-only) -> Cela générera un Client Secret.
*   **Standard flow** : `OFF`.
*   **Service accounts roles** : `ON`.
*   **Save**.

## Étape 2 : Création des Rôles

Allez dans **Realm roles** > **Create role**. Créez les rôles suivants :
1.  **Name** : `ADMIN`
    *   Description : Administrateur global.
2.  **Name** : `WAREHOUSE_MANAGER`
    *   Description : Gestionnaire de stock.
3.  **Name** : `CLIENT`
    *   Description : Client final.

## Étape 3 : Création des Groupes (Simplification)

Allez dans **Groups** > **Create group**.
1.  Créez le groupe `Admins`.
    *   Sélectionnez le groupe `Admins`.
    *   Onglet **Role mapping** > **Assign role** > Sélectionnez `ADMIN` > **Assign**.
2.  Créez le groupe `Managers`.
    *   Mappez le rôle `WAREHOUSE_MANAGER`.
3.  Créez le groupe `Clients`.
    *   Mappez le rôle `CLIENT`.

## Étape 4 : Création des Utilisateurs de Test

Allez dans **Users** > **Create new user**.

### User 1 : Grand Admin
*   **Username** : `admin_user`.
*   **Email** : `admin@traceandtrust.com`.
*   **Email verified** : `Yes`.
*   **Create**.
*   Onglet **Credentials** > **Set password** : `admin123` (Temporary: `OFF`).
*   Onglet **Groups** > **Join Group** : `Admins`.

### User 2 : Chef d'Entrepôt
*   **Username** : `manager_user`.
*   **Email** : `manager@traceandtrust.com`.
*   **Credentials** : `manager123`.
*   **Groups** : `Managers`.

### User 3 : Client Lambda
*   **Username** : `client_user`.
*   **Email** : `client@gmail.com`.
*   **Credentials** : `client123`.
*   **Groups** : `Clients`.

## Étape 5 : Token Mapper (Important pour Spring Security)
Par défaut, Keycloak met les rôles dans `realm_access.roles`. Parfois, Spring attend une structure plate ou différente.

Pour s'assurer que les rôles sont lisibles :
1.  Dans **Client scopes**, cliquez sur `roles`.
2.  Onglet **Mappers** > `realm roles`.
3.  Vérifiez que **Token Claim Name** est bien `realm_access.roles` (c'est le standard Keycloak, nous adapterons Spring Boot pour lire ce chemin).

## Livrable
Le realm est prêt. Vous pouvez tenter de vous logger sur la console Account de Keycloak (`http://localhost:8080/realms/logistics-realm/account`) avec `client_user` pour tester.
