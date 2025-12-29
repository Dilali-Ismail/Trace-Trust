# Tâches Administrateur (Personne A) : Infrastructure Keycloak

Ce document détaille les étapes techniques pour mettre en service le serveur Keycloak.

## Étape 1 : Configuration Docker Compose

Ajoutez le service Keycloak à votre fichier `docker-compose.yml`.

**Code à ajouter :**
```yaml
  keycloak:
    image: quay.io/keycloak/keycloak:23.0.0
    container_name: keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://db:5432/keycloak
      KC_DB_USERNAME: postgres  # Assurez-vous d'avoir créé cette DB ou user si différent
      KC_DB_PASSWORD: postgres
    command: start-dev # Mode dev pour commencer (HTTP activé)
    ports:
      - "8080:8080"
    depends_on:
      - db
    networks:
      - spring-network
```

*Note : Créez une base de données `keycloak` dans votre service PostgreSQL si elle n'existe pas, ou laissez Keycloak la créer si le user a les droits.*

## Étape 2 : Initialisation du Realm

1.  **Démarrage** : Lancez `docker-compose up -d keycloak`.
2.  **Accès** : Allez sur `http://localhost:8080`.
3.  **Login Console** : Identifiants `admin` / `admin`.
4.  **Création** :
    *   Cliquez sur le menu déroulant en haut à gauche ("Keycloak").
    *   Cliquez sur **"Create Realm"**.
    *   Nom : `logistics-realm`.
    *   Cliquez sur **Create**.

## Étape 3 : Configuration Globale du Realm

### Login
*   Allez dans **Realm Settings** > **Login**.
*   Activez **User registration** : `ON` (si souhaité).
*   Activez **Forgot password** : `ON`.
*   Activez **Remember me** : `ON`.

### Email
*   Allez dans **Realm Settings** > **Email**.
*   SMTP Host : ex. `smtp.gmail.com` (ou `mailhog` pour le dev local).
*   Port : `587`.
*   Auth : `ON` (votre email/pwd d'app).

### Sécurité des Tokens
*   Allez dans **Realm Settings** > **Tokens**.
*   **Access Token Lifespan** : Mettre `15 Minutes`.
*   **SSO Session Idle** : Mettre `7 Days` (Durée du Refresh Token).
*   **SSO Session Max** : Mettre `30 Days` (Déconnexion forcée après 1 mois).

### Audit
*   Allez dans **Events** > **Config**.
*   Activez **Save Events** : `ON`.
*   Expiration : `Save events for` -> `30 Days`.
*   Dans **Included Events**, sélectionnez : `LOGIN`, `LOGIN_ERROR`, `LOGOUT`, `REGISTER`.

## Livrable
Une fois terminé, confirmez à la Personne B que le serveur est prêt et que le realm `logistics-realm` est en ligne.
