#!/bin/sh
# run-exercice3.sh – Exercice 3 : Queue + Topic
#
# Scénario : Plateforme d'inscription universitaire
#
#   [InscriptionProducer] --Queue--> [AdminProcessor] --Topic--> [NotificationSubscriber x3]
#        (étudiants)                 (valide + notifie)          (portail / prof / secrétariat)
#
# Ordre de démarrage CRITIQUE :
#   1. NotificationSubscribers (Topic : non-persistant, doit écouter avant la publication)
#   2. AdminProcessor          (pont Queue → Topic)
#   3. InscriptionProducer     (Queue : persistant, peut envoyer en dernier)

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo ">>> Compilation..."
mvn compile -q

echo ">>> Construction du classpath..."
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/jms_cp.txt
CP="target/classes:$(cat /tmp/jms_cp.txt)"

# ── Étape 1 : démarrer les 3 subscribers Topic ────────────────────────────────
echo "[TEST] Démarrage des 3 NotificationSubscribers..."
java -cp "$CP" tp.jms.ex3.NotificationSubscriber PortailEtudiant > /tmp/sub_etudiant.log    2>&1 &
PID1=$!
java -cp "$CP" tp.jms.ex3.NotificationSubscriber TableauProfesseur > /tmp/sub_prof.log       2>&1 &
PID2=$!
java -cp "$CP" tp.jms.ex3.NotificationSubscriber Secretariat       > /tmp/sub_secretariat.log 2>&1 &
PID3=$!
echo "[TEST] Subscribers démarrés (PIDs: $PID1 $PID2 $PID3)"

# Laisser le temps aux subscribers de se connecter au broker
sleep 3

# ── Étape 2 : démarrer AdminProcessor (Queue consumer + Topic publisher) ──────
echo "[TEST] Démarrage AdminProcessor (pont Queue→Topic)..."
java -cp "$CP" tp.jms.ex3.AdminProcessor > /tmp/admin.log 2>&1 &
PID_ADMIN=$!
echo "[TEST] AdminProcessor démarré (PID=$PID_ADMIN)"

sleep 1

# ── Étape 3 : envoyer les demandes d'inscription dans la Queue ────────────────
echo "[TEST] Envoi des demandes d'inscription..."
java -cp "$CP" tp.jms.ex3.InscriptionProducer 2>/dev/null

# ── Étape 4 : attendre la fin de tous les processus ───────────────────────────
echo "[TEST] Attente de la fin des processus (max ~15 s)..."
wait $PID_ADMIN
wait $PID1 $PID2 $PID3

# ── Étape 5 : afficher les résultats ─────────────────────────────────────────
echo ""
echo "======================================================="
echo "=== SORTIE AdminProcessor (Queue → Topic)          ==="
echo "======================================================="
cat /tmp/admin.log

echo ""
echo "======================================================="
echo "=== SORTIE NotificationSubscriber [PortailEtudiant] ==="
echo "======================================================="
cat /tmp/sub_etudiant.log

echo ""
echo "======================================================="
echo "=== SORTIE NotificationSubscriber [TableauProfesseur] ==="
echo "======================================================="
cat /tmp/sub_prof.log

echo ""
echo "======================================================="
echo "=== SORTIE NotificationSubscriber [Secretariat]     ==="
echo "======================================================="
cat /tmp/sub_secretariat.log
