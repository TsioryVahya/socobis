# Dépendances Requises - SOCOBIS

## JAR Files à placer dans `build-file/lib/`

### 1. Driver Oracle JDBC
- **Fichier** : `ojdbc8.jar` ou `ojdbc6.jar`
- **Source** : https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
- **Version recommandée** : ojdbc8.jar (pour Oracle 12c+)

### 2. Java EE API
- **Fichier** : `javaee-api-7.0.jar`
- **Source** : Maven Central ou WildFly
- **Description** : APIs Java EE pour EJB, Servlets, etc.

### 3. Dépendances WildFly (optionnelles)
Ces JAR sont généralement fournis par WildFly, mais peuvent être nécessaires pour la compilation :

- `wildfly-ejb-client-bom-10.1.0.Final.jar`
- `jboss-ejb-api_3.2_spec-1.0.0.Final.jar`
- `jboss-servlet-api_3.1_spec-1.0.0.Final.jar`

### 4. Utilitaires (si utilisés dans le projet)
- `commons-lang3-3.x.jar`
- `commons-io-2.x.jar`
- `log4j-core-2.x.jar`
- `gson-2.x.jar` (pour JSON)

## Téléchargement Automatique

### Script PowerShell pour télécharger les dépendances principales

```powershell
# Créer le répertoire lib s'il n'existe pas
New-Item -ItemType Directory -Force -Path "build-file\lib"

# URLs de téléchargement (à adapter selon les versions disponibles)
$dependencies = @{
    "javaee-api-7.0.jar" = "https://repo1.maven.org/maven2/javax/javaee-api/7.0/javaee-api-7.0.jar"
}

foreach ($jar in $dependencies.Keys) {
    $url = $dependencies[$jar]
    $output = "build-file\lib\$jar"
    
    Write-Host "Téléchargement de $jar..."
    try {
        Invoke-WebRequest -Uri $url -OutFile $output
        Write-Host "✓ $jar téléchargé avec succès"
    } catch {
        Write-Host "✗ Erreur lors du téléchargement de $jar : $_"
    }
}

Write-Host "`nATTENTION: Le driver Oracle (ojdbc8.jar) doit être téléchargé manuellement depuis Oracle."
```

## Installation Manuelle

### 1. Driver Oracle
1. Aller sur https://www.oracle.com/database/technologies/appdev/jdbc-downloads.html
2. Télécharger `ojdbc8.jar` (ou ojdbc6.jar pour Oracle 11g)
3. Copier dans `build-file/lib/ojdbc8.jar`

### 2. Java EE API
1. Télécharger depuis Maven Central : https://repo1.maven.org/maven2/javax/javaee-api/7.0/javaee-api-7.0.jar
2. Copier dans `build-file/lib/javaee-api-7.0.jar`

### 3. Vérification
Après installation, le répertoire `build-file/lib/` devrait contenir au minimum :
```
build-file/lib/
├── ojdbc8.jar
├── javaee-api-7.0.jar
└── [autres dépendances selon les besoins]
```

## Configuration WildFly

### DataSource Oracle
Créer le fichier `standalone/configuration/oracle-ds.xml` dans WildFly :

```xml
<?xml version="1.0" encoding="UTF-8"?>
<datasources xmlns="http://www.jboss.org/ironjacamar/schema">
    <datasource jndi-name="java:/jdbc/socobis" pool-name="OracleDS" enabled="true">
        <connection-url>jdbc:oracle:thin:@192.168.88.168:1521:ORCL</connection-url>
        <driver>oracle</driver>
        <security>
            <user-name>socobisprod</user-name>
            <password>socobisprod</password>
        </security>
        <validation>
            <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleValidConnectionChecker"/>
            <stale-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleStaleConnectionChecker"/>
            <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.oracle.OracleExceptionSorter"/>
        </validation>
    </datasource>
</datasources>
```

### Driver Oracle dans WildFly
1. Copier `ojdbc8.jar` dans `wildfly/standalone/deployments/`
2. Ou configurer comme module dans `wildfly/modules/`

## Résolution des Problèmes

### ClassNotFoundException
- Vérifier que toutes les JAR sont dans `build-file/lib/`
- Contrôler les chemins dans `build.xml`

### SQLException
- Vérifier la configuration du DataSource
- Tester la connectivité Oracle
- Contrôler les credentials dans `apj.properties`

### Compilation Errors
- Vérifier la version de Java (JDK 8 requis)
- S'assurer que `javaee-api.jar` est présent
- Contrôler l'encoding des fichiers sources (ISO-8859-1)

---
*Dernière mise à jour : 13 novembre 2025*
