# Guide de Configuration et Déploiement - SOCOBIS

## Vue d'ensemble du Projet

SOCOBIS est une application Java EE composée de :
- **socobis-ejb** : Module EJB contenant la logique métier
- **socobis-war** : Module Web (interface utilisateur)
- **Base de données Oracle** : Stockage des données
- **WildFly 10.1.0** : Serveur d'application

## Prérequis Système

### 1. Java Development Kit (JDK)
- **Version requise** : JDK 8 (OpenJDK 8 ou Oracle JDK 8)
- **Installation** :
  ```bash
  # Vérifier la version Java
  java -version
  javac -version
  ```
- **Configuration** : Définir `JAVA_HOME` dans les variables d'environnement

### 2. Apache Ant
- **Version recommandée** : Apache Ant 1.9+
- **Installation** : Télécharger depuis https://ant.apache.org/
- **Configuration** : Ajouter `ANT_HOME/bin` au PATH

### 3. Base de Données Oracle
- **Version** : Oracle Database 11g/12c/19c
- **Configuration requise** :
  - Instance : `ORCL`
  - Port : `1521`
  - Utilisateur : `socobisprod`
  - Mot de passe : `socobisprod`

### 4. WildFly Application Server
- **Version** : WildFly 10.1.0.Final
- **Téléchargement** : https://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip

## Configuration de l'Environnement

### 1. Configuration de la Base de Données

#### A. Création de l'utilisateur Oracle
```sql
-- Se connecter en tant que SYSDBA
CREATE USER socobisprod IDENTIFIED BY socobisprod;
GRANT CONNECT, RESOURCE, DBA TO socobisprod;
GRANT UNLIMITED TABLESPACE TO socobisprod;
```

#### B. Import des données
```bash
# Naviguer vers le dossier bdd
cd g:\ITU\S5\PROG\socobis\socobis\socobis-prod\socobis-prod\bdd

# Importer le dump de base (si disponible)
sqlplus socobisprod/socobisprod@localhost:1521/ORCL @SOCOBIS-11072025.sql
```

### 2. Configuration WildFly

#### A. Installation WildFly
```bash
# Télécharger et extraire WildFly
wget https://download.jboss.org/wildfly/10.1.0.Final/wildfly-10.1.0.Final.zip
unzip wildfly-10.1.0.Final.zip
mv wildfly-10.1.0.Final /opt/wildfly
```

#### B. Configuration du DataSource Oracle
Créer le fichier `/opt/wildfly/standalone/configuration/oracle-ds.xml` :
```xml
<?xml version="1.0" encoding="UTF-8"?>
<datasources xmlns="http://www.jboss.org/ironjacamar/schema">
    <datasource jndi-name="java:/jdbc/socobis" pool-name="OracleDS">
        <connection-url>jdbc:oracle:thin:@192.168.88.168:1521:ORCL</connection-url>
        <driver>oracle</driver>
        <security>
            <user-name>socobisprod</user-name>
            <password>socobisprod</password>
        </security>
    </datasource>
</datasources>
```

#### C. Ajouter le driver Oracle
1. Télécharger `ojdbc8.jar` depuis Oracle
2. Copier dans `/opt/wildfly/standalone/deployments/`

### 3. Configuration des Propriétés

#### A. Modifier les URLs dans `project.properties`
```properties
# Adapter selon votre environnement
cdnUri=http://localhost:82/cnaps-cdn/uploadcontent.php
cdnDeleteUri=http://localhost:82/cnaps-cdn/deleteuploaded.php
cdnReadUri=http://localhost:8080/dossier/
dossierAmadia=http://localhost:8080/dossier/
cdnPDF=D:/PDFCnaPS
```

#### B. Vérifier `apj.properties`
```properties
# Adapter l'IP et les credentials selon votre environnement
apj.connection.url=jdbc:oracle:thin:@localhost:1521:ORCL
apj.connection.user=socobisprod
apj.connection.password=socobisprod
```

## Processus de Build et Déploiement

### 1. Build avec Ant

#### A. Préparer l'environnement de build
```bash
cd g:\ITU\S5\PROG\socobis\socobis\socobis-prod\socobis-prod
```

#### B. Créer le dossier lib et ajouter les dépendances
```bash
mkdir -p build-file/lib
# Copier toutes les JAR nécessaires dans build-file/lib/
# Notamment : ojdbc8.jar, javaee-api.jar, etc.
```

#### C. Exécuter le build
```bash
# Build complet
ant clean deploy

# Ou étape par étape
ant clean
ant init
ant compile
ant buildEjbJar
ant compileWar
ant copieProperties
ant deploy
```

### 2. Déploiement Manuel

#### A. Copier vers WildFly
```bash
# Copier l'application buildée
cp -r build-file/socobis_war/* /opt/wildfly/standalone/deployments/socobis.war/
touch /opt/wildfly/standalone/deployments/socobis.war.dodeploy
```

#### B. Démarrer WildFly
```bash
cd /opt/wildfly/bin
./standalone.sh -b 0.0.0.0
```

## Déploiement avec Docker

### 1. Build de l'image Docker
```bash
# Depuis le répertoire racine du projet
docker build -t socobis:latest .
```

### 2. Exécution du conteneur
```bash
docker run -d \
  --name socobis-app \
  -p 8080:8080 \
  -p 9990:9990 \
  socobis:latest
```

## Vérification du Déploiement

### 1. Vérifier WildFly
- Console d'administration : http://localhost:9990
- Application : http://localhost:8080/socobis

### 2. Logs à surveiller
```bash
# Logs WildFly
tail -f /opt/wildfly/standalone/log/server.log

# Logs de déploiement
ls -la /opt/wildfly/standalone/deployments/
```

### 3. Tests de connectivité
```bash
# Test de la base de données
sqlplus socobisprod/socobisprod@localhost:1521/ORCL

# Test de l'application
curl http://localhost:8080/socobis
```

## Résolution des Problèmes Courants

### 1. Erreurs de Base de Données
- Vérifier que Oracle est démarré
- Contrôler les credentials dans `apj.properties`
- Vérifier la connectivité réseau

### 2. Erreurs de Compilation
- Vérifier que toutes les JAR sont dans `build-file/lib/`
- Contrôler la version de Java (JDK 8 requis)
- Vérifier les chemins dans `build.xml`

### 3. Erreurs de Déploiement
- Contrôler les logs WildFly
- Vérifier que le DataSource est configuré
- S'assurer que les ports ne sont pas occupés

## Structure des Répertoires

```
socobis-prod/
├── bdd/                    # Scripts SQL
├── build-file/             # Répertoire de build
│   ├── lib/               # Dépendances JAR
│   ├── socobis_war/       # WAR buildé
│   └── socobis_jar/       # EJB JAR buildé
├── socobis-ejb/           # Module EJB
│   └── src/java/          # Sources Java EJB
├── socobis-war/           # Module Web
│   ├── src/java/          # Sources Java Web
│   └── web/               # Ressources Web
├── build.xml              # Script de build Ant
├── Dockerfile             # Configuration Docker
└── README.md              # Documentation
```

## Contacts et Support

Pour toute question ou problème :
1. Vérifier les logs d'application
2. Consulter la documentation WildFly
3. Vérifier la connectivité Oracle

---
*Guide créé le 13 novembre 2025*
