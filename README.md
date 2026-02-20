# TP Architecture des Systèmes d'Information – Compte rendu

**Module :** Architecture des Systèmes d'Information
**Sujet :** Middleware orienté message (MOM) avec JMS et ActiveMQ Classic
**Technologies :** Java 21 LTS · Maven · ActiveMQ Classic 5.18.3 · JMS 1.1

---

## Table des matières

1. [Rappels théoriques – MOM et JMS](#1-rappels-théoriques--mom-et-jms)
2. [Architecture du projet](#2-architecture-du-projet)
3. [Prérequis et installation](#3-prérequis-et-installation)
4. [Compilation](#4-compilation)
5. [Test Queue – modèle Point-à-Point](#5-test-queue--modèle-point-à-point)
6. [Test Topic – modèle Publication/Abonnement](#6-test-topic--modèle-publicationabonnement)
7. [Données XML et JSON](#7-données-xml-et-json)
8. [Analyse et conclusion](#8-analyse-et-conclusion)

---

## 1. Rappels théoriques – MOM et JMS

### 1.1 Message-Oriented Middleware (MOM)

Un **MOM** est un intergiciel (middleware) qui permet à des applications hétérogènes d'échanger des messages de manière **asynchrone** via un **broker** (serveur de messages). Le producteur dépose son message dans le broker et continue son exécution sans attendre la réponse du consommateur.

Caractéristiques clés d'un MOM :

| Propriété | Description |
|-----------|-------------|
| **Découplage** | Producteur et consommateur ignorent mutuellement leur existence |
| **Asynchronisme** | L'émetteur n'attend pas de réponse immédiate |
| **Persistance** | Les messages peuvent survivre à un redémarrage du broker |
| **Scalabilité** | Plusieurs consommateurs peuvent traiter les messages en parallèle |
| **Fiabilité** | Garantie de livraison configurable (at-most-once, at-least-once, exactly-once) |

Exemples de MOM : Apache ActiveMQ, RabbitMQ, IBM MQ, Apache Kafka.

### 1.2 Java Message Service (JMS)

**JMS** est une API standard Java (JSR-343) qui définit une interface uniforme pour interagir avec différents MOM. Elle ne fournit pas d'implémentation ; c'est le broker (ici ActiveMQ Classic) qui l'implémente.

Principaux objets JMS :

```
ConnectionFactory  →  Connection  →  Session  →  Producer / Consumer
                                         ↓
                                   Queue | Topic
```

| Objet JMS | Rôle |
|-----------|------|
| `ConnectionFactory` | Fabrique de connexions au broker |
| `Connection` | Connexion TCP au broker |
| `Session` | Contexte de transaction, créateur de messages |
| `MessageProducer` | Envoie des messages vers une destination |
| `MessageConsumer` | Reçoit des messages d'une destination |
| `TextMessage` | Message dont le corps est une chaîne de caractères |

### 1.3 Modèles de messagerie JMS

#### Modèle Point-à-Point (Queue)

```
Producteur ──→ [■■■■■ Queue ■■■■■] ──→ Consommateur unique
```

- Le message est stocké dans la **queue** jusqu'à sa consommation.
- Un seul consommateur reçoit chaque message (même si plusieurs écoutent).
- La **persistance** est garantie : un message envoyé avant le démarrage du consommateur est livré dès que celui-ci se connecte.
- Usage typique : traitement de commandes, file d'attente de travaux.

#### Modèle Publication/Abonnement (Topic)

```
Éditeur ──→ [★ Topic ★] ──→ Abonné 1
                       └──→ Abonné 2
                       └──→ Abonné N
```

- Chaque abonné actif reçoit **une copie** du message.
- Par défaut, les abonnements sont **non-durables** : un abonné déconnecté perd les messages publiés pendant son absence.
- L'abonné doit être connecté **avant** la publication pour recevoir le message.
- Usage typique : diffusion d'actualités, broadcast d'événements système.

#### Comparatif Queue vs Topic

| Critère | Queue (P2P) | Topic (Pub/Sub) |
|---------|------------|-----------------|
| Destinataires | 1 seul consommateur | N abonnés simultanés |
| Persistance | Oui (par défaut) | Non (sauf abonnements durables) |
| Ordre démarrage | Indépendant | Abonné avant l'éditeur |
| Cas d'usage | Traitement de tâches | Diffusion d'événements |

---

## 2. Architecture du projet

```
tp-archi-si/                        ← Projet Maven multi-module
├── pom.xml                         ← POM parent (Java 21, gestion dépendances)
├── README.md                       ← Ce compte rendu
│
├── jms/                            ← Module Maven JMS
│   ├── pom.xml                     ← Dépendances ActiveMQ + exec-maven-plugin
│   └── src/main/java/tp/jms/
│       ├── JmsConfig.java          ← Constantes (URL broker, noms destinations)
│       ├── QueueProducer.java      ← Envoie 5 messages → customerQueue
│       ├── QueueConsumer.java      ← Écoute customerQueue (10 s)
│       ├── TopicPublisher.java     ← Publie 3 actualités → newsTopic
│       └── TopicSubscriber.java   ← S'abonne à newsTopic (10 s)
│
└── data/
    ├── xml/
    │   ├── agenda.xml              ← Document XML (3 cours)
    │   ├── agenda.xsd              ← Schéma XSD de l'agenda
    │   └── bank.xsd                ← Schéma XSD d'une banque
    └── json/
        ├── etudiants.json          ← Liste de 3 étudiants avec notes
        └── etudiants.schema.json   ← JSON Schema Draft-07
```

**Destination JMS configurées :**

| Destination | Type | Nom |
|-------------|------|-----|
| Queue | Point-à-Point | `customerQueue` |
| Topic | Pub/Sub | `newsTopic` |

---

## 3. Prérequis et installation

### 3.1 Prérequis logiciels

- **Java 21 LTS** : `java -version` doit afficher `openjdk 21`
- **Maven 3.9+** : `mvn -version`
- **ActiveMQ Classic 5.18.x** : broker de messages

### 3.2 Téléchargement et démarrage d'ActiveMQ Classic

```bash
# Télécharger ActiveMQ Classic 5.18.3
curl -O https://archive.apache.org/dist/activemq/5.18.3/apache-activemq-5.18.3-bin.tar.gz
tar xzf apache-activemq-5.18.3-bin.tar.gz
cd apache-activemq-5.18.3

# Démarrer le broker
./bin/activemq start

# Vérifier que le broker écoute sur le port 61616
# Console d'administration : http://localhost:8161/admin  (admin/admin)
```

> **macOS avec Homebrew :**
> ```bash
> brew install activemq
> activemq start
> ```

Le broker expose :
- **`tcp://localhost:61616`** – connecteur OpenWire (JMS)
- **`http://localhost:8161/admin`** – console web (login : `admin` / `admin`)

---

## 4. Compilation

```bash
# Depuis la racine du projet
mvn clean package

# Résultat attendu
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Reactor build order:
[INFO]   TP Architecture SI                                     [pom]
[INFO]   TP Architecture SI – Module JMS                        [jar]
```

---

## 5. Test Queue – modèle Point-à-Point

### 5.1 Scénario

1. Démarrer le **consommateur** dans un terminal (il écoute pendant 10 s).
2. Démarrer le **producteur** dans un autre terminal (il envoie 5 messages).
3. Observer la réception des messages par le consommateur.

> La queue persiste les messages : le producteur peut être lancé **avant** le consommateur.

### 5.2 Commandes exactes

**Terminal 1 – Consommateur :**
```bash
cd /chemin/vers/tp-archi-si/jms
mvn exec:java -Dexec.mainClass=tp.jms.QueueConsumer
```

**Terminal 2 – Producteur :**
```bash
cd /chemin/vers/tp-archi-si/jms
mvn exec:java -Dexec.mainClass=tp.jms.QueueProducer
```

### 5.3 Résultats attendus

**Sortie du QueueProducer :**
```
=== QueueProducer – connexion sur tcp://localhost:61616 ===
[ENVOYÉ]  Commande client #1 [ts=1740000000001]
[ENVOYÉ]  Commande client #2 [ts=1740000000012]
[ENVOYÉ]  Commande client #3 [ts=1740000000023]
[ENVOYÉ]  Commande client #4 [ts=1740000000034]
[ENVOYÉ]  Commande client #5 [ts=1740000000045]
>>> 5 messages envoyés sur la queue 'customerQueue'.
```

**Sortie du QueueConsumer :**
```
=== QueueConsumer – connexion sur tcp://localhost:61616 ===
En attente de messages sur 'customerQueue' pendant 10 s...
[REÇU]   Commande client #1 [ts=1740000000001]
[REÇU]   Commande client #2 [ts=1740000000012]
[REÇU]   Commande client #3 [ts=1740000000023]
[REÇU]   Commande client #4 [ts=1740000000034]
[REÇU]   Commande client #5 [ts=1740000000045]
>>> Consommateur fermé.
```

**Observation :** si deux instances de QueueConsumer sont lancées simultanément, les 5 messages sont répartis entre elles (load balancing automatique du broker). Chaque message n'est reçu qu'une seule fois au total.

---

## 6. Test Topic – modèle Publication/Abonnement

### 6.1 Scénario

1. Démarrer **deux abonnés** dans deux terminaux distincts (ils écoutent 10 s).
2. Démarrer le **publisher** dans un troisième terminal.
3. Vérifier que les **deux abonnés** reçoivent chacun les 3 messages.

> **Ordre impératif :** les abonnés doivent être démarrés **avant** le publisher.

### 6.2 Commandes exactes

**Terminal 1 – Abonné 1 :**
```bash
cd /chemin/vers/tp-archi-si/jms
mvn exec:java -Dexec.mainClass=tp.jms.TopicSubscriber
```

**Terminal 2 – Abonné 2 (optionnel, pour valider le broadcast) :**
```bash
cd /chemin/vers/tp-archi-si/jms
mvn exec:java -Dexec.mainClass=tp.jms.TopicSubscriber
```

**Terminal 3 – Publisher :**
```bash
cd /chemin/vers/tp-archi-si/jms
mvn exec:java -Dexec.mainClass=tp.jms.TopicPublisher
```

### 6.3 Résultats attendus

**Sortie du TopicPublisher :**
```
=== TopicPublisher – connexion sur tcp://localhost:61616 ===
[PUBLIÉ]  Nouvelle inscription validée pour le cours Architecture SI
[PUBLIÉ]  Planning TP mis à jour – séance supplémentaire le vendredi
[PUBLIÉ]  Résultats du contrôle intermédiaire disponibles sur l'ENT
>>> 3 messages publiés sur le topic 'newsTopic'.
```

**Sortie de chaque TopicSubscriber (abonné 1 ET abonné 2) :**
```
=== TopicSubscriber – connexion sur tcp://localhost:61616 ===
Abonné au topic 'newsTopic' – écoute pendant 10 s...
[REÇU]   Nouvelle inscription validée pour le cours Architecture SI
[REÇU]   Planning TP mis à jour – séance supplémentaire le vendredi
[REÇU]   Résultats du contrôle intermédiaire disponibles sur l'ENT
>>> Abonné fermé.
```

**Observation :** contrairement à la queue, les deux abonnés reçoivent chacun les 3 messages en totalité. C'est le mécanisme de **broadcast** du topic.

---

## 7. Données XML et JSON

### 7.1 XML – Agenda et schéma XSD

Le fichier `data/xml/agenda.xml` liste 3 cours avec leur titre, date, horaire, salle et enseignant. Il est validable contre `data/xml/agenda.xsd`.

**Validation avec Java (ligne de commande) :**
```bash
# Avec xmllint (macOS via brew install libxml2)
xmllint --schema data/xml/agenda.xsd data/xml/agenda.xml --noout
# Résultat attendu : data/xml/agenda.xml validates
```

**Structure XSD de l'agenda :**
```
agenda
└── cours [1..*]
    ├── titre      (xs:string)
    ├── date       (xs:date)
    ├── horaire    @debut @fin  (pattern HH:MM)
    ├── salle      (xs:string)
    └── enseignant (xs:string)
```

Le fichier `data/xml/bank.xsd` définit le schéma d'une banque avec :
- `accounts` → liste de comptes (`checking` | `savings`) avec titulaire, solde et devise (ISO 4217)
- `transactions` (optionnel) → liste de virements (from, to, amount, dateTime)

### 7.2 JSON – Étudiants et JSON Schema

Le fichier `data/json/etudiants.json` contient 3 étudiants avec leurs notes par matière.

**Validation avec ajv-cli :**
```bash
npm install -g ajv-cli
ajv validate -s data/json/etudiants.schema.json -d data/json/etudiants.json
# Résultat attendu : data/json/etudiants.json valid
```

**Contraintes JSON Schema (Draft-07) :**

| Champ | Type | Contrainte |
|-------|------|------------|
| `id` | string | Pattern `E[0-9]{3}` |
| `email` | string | Format `email` |
| `note` | number | `[0, 20]` |
| `etudiants` | array | `minItems: 1` |

---

## 8. Analyse et conclusion

### 8.1 Différences observées entre Queue et Topic

| Comportement | Queue | Topic |
|--------------|-------|-------|
| Réception si consommateur absent | Messages conservés | Messages perdus |
| Deux consommateurs simultanés | Répartition des messages | Copie à chacun |
| Ordre de démarrage | Libre | Abonné en premier |

### 8.2 Avantages du MOM dans une architecture SI

- **Résilience** : les systèmes producteurs et consommateurs peuvent redémarrer indépendamment.
- **Scalabilité horizontale** : ajouter des consommateurs de queue permet de traiter plus de messages en parallèle sans modifier le producteur.
- **Intégration hétérogène** : JMS permet d'interfacer des applications Java, Python, .NET via le protocole STOMP ou AMQP d'ActiveMQ.

### 8.3 Limites identifiées

- Le modèle Topic non-durable perd les messages si aucun abonné n'est connecté → solution : **abonnements durables** (`createDurableSubscriber`).
- Pas de garantie d'ordre strict avec plusieurs consommateurs concurrents sur une queue.
- ActiveMQ Classic est en maintenance ; pour de nouveaux projets, préférer **ActiveMQ Artemis** ou **Apache Kafka** pour des charges élevées.

---

*Projet généré pour le TP Architecture SI – Java 21 LTS · ActiveMQ Classic 5.18.3*
