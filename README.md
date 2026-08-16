# Gestion Session CEPE — Madagascar

Application de gestion et de délibération des examens du CEPE (Certificat d'Études Primaires Élémentaires) développée en Java Swing et PostgreSQL.

Elle permet de centraliser la gestion des établissements, des candidats, la saisie des notes par matière, le calcul automatique des moyennes avec pondération des coefficients, ainsi que la délibération et la génération de statistiques par école.

---

## ✅ Fonctionnalités Principales

* **Résultats & Délibération :**
  * Calcul dynamique des moyennes générales avec coefficients.
  * Seuil de délibération ajustable (admission / ajournement automatique).
  * Classement global des candidats et classement par établissement.
  * Recherche multi-critères et filtrage par école.

* **Statistiques par École :**
  * Indicateurs clés (KPI) : nombre total de candidats, admis, ajournés et taux de réussite global.
  * Tableau récapitulatif du taux de réussite et de la moyenne générale par école.

* **Gestion Multi-Sessions :**
  * Support de plusieurs années scolaires (ex: 2025-2026, 2026-2027) via un sélecteur global.
  * Possibilité de créer de nouvelles sessions d'examen dynamiquement.

* **Administration & Saisie :**
  * Saisie fluide des notes par élève.
  * Saisie et gestion des matières, coefficients et établissements.

---

## 🛠️ Stack Technique

* **Langage :** Java 17+
* **Interface Graphique :** Java Swing + Look & Feel FlatLaf (Thème moderne)
* **Base de données :** PostgreSQL
* **Gestionnaire de build :** Maven / Gradle

---

## ⚙️ Configuration & Installation

### 1. Prérequis
* JDK 17 ou supérieur
* PostgreSQL d'installé et démarré

### 2. Base de données
1. Créez une base de données nommée `cepe_db` (ou selon votre configuration).
2. Exécutez le script d'initialisation présent dans `/database/schema.sql` pour créer les tables nécessaires.
3. Configurez vos identifiants de connexion PostgreSQL dans le fichier de configuration de l'application (ex: `db.properties`).

### 3. Lancement
Compilez et exécutez la classe principale `MainFrame.java` depuis votre IDE ou via la commande :

```bash
mvn clean compile exec:java -Dexec.mainClass="com.cepe.ui.MainFrame"
