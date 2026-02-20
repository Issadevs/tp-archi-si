package tp.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Producteur JMS – envoie 5 messages texte sur la queue {@code customerQueue}.
 *
 * <p>Modèle Point-à-Point (P2P) : chaque message est consommé par <em>un seul</em>
 * consommateur. Les messages sont persistés dans la queue jusqu'à leur livraison.</p>
 *
 * <pre>
 *   cd jms
 *   mvn exec:java -Dexec.mainClass=tp.jms.QueueProducer
 * </pre>
 */
public class QueueProducer {

    public static void main(String[] args) throws Exception {
        System.out.println("=== QueueProducer – connexion sur " + JmsConfig.BROKER_URL + " ===");

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(JmsConfig.BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(JmsConfig.QUEUE_NAME);
        MessageProducer producer = session.createProducer(queue);

        try {
            for (int i = 1; i <= 5; i++) {
                String body = "Commande client #" + i + " [ts=" + System.currentTimeMillis() + "]";
                TextMessage msg = session.createTextMessage(body);
                producer.send(msg);
                System.out.println("[ENVOYÉ]  " + body);
            }
            System.out.println(">>> 5 messages envoyés sur la queue '" + JmsConfig.QUEUE_NAME + "'.");
        } finally {
            producer.close();
            session.close();
            connection.close();
        }
    }
}
