# Comprendre `MdcLogFilter.java`

Ce fichier est une pièce maîtresse pour l'observabilité (monitoring) de votre application. Voici son explication détaillée.

## 1. C'est quoi le MDC ?
**MDC** signifie **Mapped Diagnostic Context**.
Imaginez que chaque thread (chaque requête utilisateur) a une petite poche secrète (un Map/Dictionnaire) où l'on peut glisser des étiquettes.
*   Quand vous faites `log.info("Message")`, le système de log regarde dans cette poche et ajoute automatiquement les étiquettes au message.

**Sans MDC :**
`2024-01-01 12:00:00 INFO Service - Commande créée`
*(On ne sait pas qui a fait ça)*

**Avec MDC :**
`2024-01-01 12:00:00 INFO Service - Commande créée {user=admin@test.com, role=ADMIN, ip=192.168.1.1}`
*(Là, on sait tout !)*

---

## 2. Analyse du Code Ligne par Ligne

### La Classe
```java
public class MdcLogFilter extends OncePerRequestFilter {
```
*   `OncePerRequestFilter` : Une classe de Spring qui garantit que ce filtre s'exécute **une seule fois** par requête HTTP. C'est le comportement standard pour les filtres de sécurité ou de log.

### La Méthode Principale
```java
@Override
protected void doFilterInternal(...) {
    try {
        // ... Logique d'ajout des infos
```
*   On ouvre un bloc `try` pour garantir que le code de nettoyage (`finally`) s'exécutera toujours, même si l'application plante au milieu.

### L'Enrichissement (Le cœur du fichier)
```java
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
if (auth != null && auth.isAuthenticated() ...) {
    MDC.put("user", auth.getName());
    MDC.put("role", auth.getAuthorities().iterator().next().getAuthority());
}
```
*   **C'est ici qu'on fait le lien avec la Sécurité.**
*   On demande à Spring Security : *"Qui est connecté ?"*
*   Si quelqu'un est connecté, on ajoute son **email** (`user`) et son **rôle** (`role`) dans le MDC.
*   **Résultat** : Tous les logs qui suivront pendant cette requête auront ces infos attachées.

### L'Exécution de la Requête
```java
filterChain.doFilter(request, response);
```
*   C'est le moment où on laisse passer la requête vers le contrôleur.
*   Tout ce qui se passe dans le contrôleur (création de commande, erreurs...) sera logué **AVEC** les infos qu'on a mises juste avant.

### Le Nettoyage (CRITIQUE ⚠️)
```java
} finally {
    MDC.clear();
}
```
*   **C'est la ligne la plus importante techniquement.**
*   Tomcat (le serveur Web) utilise un "Pool de Threads". Quand une requête est finie, le thread n'est pas détruit, il est remis dans le frigo pour servir le prochain client.
*   **Si on ne nettoie pas le MDC** : Le prochain client qui utilisera ce thread héritera des étiquettes du client précédent !
    *   *Exemple désastreux* : Le Client B fait une action, mais les logs disent que c'est l'Admin A (parce que l'étiquette est restée collée).
*   `MDC.clear()` vide la poche avant de rendre le thread.

---

## 3. Pourquoi c'est utile pour ELK (Kibana) ?

Quand Logstash reçoit le log, il voit le JSON :
```json
{
  "message": "Erreur lors du paiement",
  "user": "client@gmail.com",
  "role": "CLIENT"
}
```

Grâce à `MdcLogFilter`, dans Kibana, vous pourrez taper :
*   `user: "client@gmail.com"` -> Pour voir tout l'historique de ce client précis.
*   `role: "ADMIN"` -> Pour surveiller ce que font vos admins.

C'est ce qui transforme des "fichiers textes illisibles" en "données exploitables".
