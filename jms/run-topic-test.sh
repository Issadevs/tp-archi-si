#!/bin/sh
# run-topic-test.sh – Test Topic Pub/Sub

set -e
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

# 1. Classpath
mvn -q dependency:build-classpath -Dmdep.outputFile=/tmp/jms_cp.txt
CP="target/classes:$(cat /tmp/jms_cp.txt)"

# 2. Subscriber en background (stdout → /tmp/subscriber.log)
java -cp "$CP" tp.jms.TopicSubscriber > /tmp/subscriber.log 2>&1 &
SUB_PID=$!
echo "[TEST] TopicSubscriber démarré (PID=$SUB_PID)"

# 3. Attendre que le subscriber soit connecté
sleep 3

# 4. Publisher en foreground
echo "[TEST] Lancement TopicPublisher..."
java -cp "$CP" tp.jms.TopicPublisher 2>/dev/null

# 5. Attendre la fin du subscriber (10 s max)
echo "[TEST] Attente de la fin du subscriber..."
wait $SUB_PID

# 6. Afficher les résultats
echo ""
echo "=== SORTIE TopicSubscriber ==="
cat /tmp/subscriber.log
