# Personne A : Configuration Complète du Realm

Maintenant que le serveur tourne, nous allons configurer le cerveau de la sécurité : le **Realm**.

## Étape 1 : Création du Realm (Si pas encore fait)

1.  Connectez-vous : `admin` / `admin`.
2.  Dans le menu déroulant en haut à gauche (où il est écrit "Master"), cliquez sur **Create Realm**.
    *   **Realm name** : `logistics-realm`.
    *   Cliquez sur **Create**.

> ⚠️ **IMPORTANT** : Pour la suite, assurez-vous toujours d'être sur `logistics-realm` et PAS sur `Master`. Le nom du realm doit être affiché en haut à gauche.

## Étape 2 : Sécurisation du Login

Allez dans **Realm settings** (menu de gauche) > Onglet **Login**.

Configurez comme suit :
*   [x] **User registration** : `ON` (Permet aux utilisateurs de créer leur compte, utile pour les tests).
*   [x] **Forgot password** : `ON` (Self-service pour mot de passe oublié).
*   [x] **Remember me** : `ON`.
*   [x] **Verify email** : `OFF` (Pour l'instant, pour éviter de bloquer les tests sans serveur mail).

Cliquez sur **Save**.

## Étape 3 : Durée de vie des Tokens et Sessions

### 1. Access Token (Onglet Tokens)
Allez dans **Realm settings** > Onglet **Tokens**.
1.  **Access Token Lifespan** : Changez la valeur par défaut pour `15 Minutes`.
    *   *Pourquoi ?* Si un token est volé, il ne sera valide que 15 min.
2.  Cliquez sur **Save**.

### 2. Refresh Token / Sessions (Onglet Sessions)
Allez dans **Realm settings** > Onglet **Sessions**.
1.  **SSO Session Idle** : Mettez `7 Days`.
    *   *C'est quoi ?* C'est la durée de vie du Refresh Token. Tant que l'utilisateur est actif au moins une fois tous les 7 jours, il reste connecté.
2.  **SSO Session Max** : Mettez `30 Days`.
    *   *C'est quoi ?* Au bout de 30 jours, même s'il est actif, on force une reconnexion par sécurité.
3.  Cliquez sur **Save**.

## Étape 4 : Activation de l'Audit

Nous voulons savoir qui fait quoi.
1.  Allez dans **Realm settings** (menu de gauche).
2.  Cliquez sur l'onglet **Events** (en haut, à droite des onglets General, Login, etc.).
3.  Sous l'onglet **User events**, activez le switch **Save events** : `ON`.
4.  **Expiration** : Vous pouvez laisser par défaut ou mettre `30 Days`.
5.  **Saved types** (ou Included events) : Ajoutez `LOGIN`, `LOGIN_ERROR`, `REGISTER`, `LOGOUT`.
6.  Cliquez sur **Save**.

---

**Félicitations !** Vous avez terminé la partie "Personne A" (Infrastructure).
Votre serveur est prêt à accueillir la configuration fonctionnelle (Clients, Rôles, Utilisateurs).

Dites "**pass**" pour basculer vers le rôle de la **Personne B** et commencer l'intégration de l'application !
