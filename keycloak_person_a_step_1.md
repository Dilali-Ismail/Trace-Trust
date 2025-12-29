# Personne A : Configuration de l'Infrastructure Keycloak

Vous êtes l'administrateur système de la solution. Votre rôle est de monter le serveur et de préparer le terrain pour les développeurs.

## Étape 1 : Ajouter Keycloak à `docker-compose.yml`

Ouvrez votre fichier `docker-compose.yml` et ajoutez le service suivant à la liste des services.

> **⚠️ Attention** : Il ne faut pas supprimer les autres services (db, api, pgadmin), juste ajouter `keycloak` à la suite. Notez que j'ai changé le port de l'API Spring Boot de 8080 à **8081** pour éviter le conflit car Keycloak utilise le 8080 par défaut.

```yaml
  keycloak:
    image: quay.io/keycloak/keycloak:23.0.0
    container_name: logistics-keycloak
    environment:
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://db:5432/keycloak
      KC_DB_USERNAME: admin
      KC_DB_PASSWORD: password
    command: start-dev
    ports:
      - "8080:8080"
    depends_on:
      - db
```

**Action requise :**
1. Copiez ce bloc dans `docker-compose.yml`.
2. Modifiez le port de votre service `api` pour qu'il soit sur `"8081:8080"` (nous le configurerons plus tard).
3. **Important** : Connectez-vous à votre base de données (via PgAdmin sur le port 5050 ou en ligne de commande) et créez une base de données vide nommée `keycloak`.

## Étape 2 : Démarrer et Initialiser

Lancez la commande suivante dans votre terminal :

```bash
docker-compose up -d
```

Attendez environ 1 minute que Keycloak démarre. Vous pouvez suivre les logs avec `docker logs -f logistics-keycloak`.

Rendez-vous sur : [http://localhost:8080](http://localhost:8080)
Connectez-vous à la **Administration Console** avec :
*   User: `admin`
*   Pass: `admin`

## Étape 3 : Créer le Realm `logistics-realm`

Une fois connecté :
1.  Passez votre souris sur **Master** (en haut à gauche).
2.  Cliquez sur **Create Realm**.
3.  Realm name : `logistics-realm`.
4.  Cliquez sur **Create**.

Dites "**pass**" quand vous avez accès à Keycloak et que le realm est créé !
