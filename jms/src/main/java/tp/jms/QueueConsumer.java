package tp.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;

/**
 * Consommateur JMS – écoute la queue {@code customerQueue} pendant
 * {@value JmsConfig#LISTEN_TIMEOUT_MS} ms puis se termine.
 *
 * <p><b>Comportement P2P :</b> les messages persistés dans la queue sont livrés
 * même si le consommateur démarre <em>après</em> le producteur. Un seul
 * consommateur reçoit chaque message.</p>
 *
 * <pre>
 *   cd jms
 *   mvn exec:java -Dexec.mainClass=tp.jms.QueueConsumer
 * </pre>
 */
public class QueueConsumer {

    public static void main(String[] args) throws Exception {
        System.out.println("=== QueueConsumer – connexion sur " + JmsConfig.BROKER_URL + " ===");

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(JmsConfig.BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Queue queue = session.createQueue(JmsConfig.QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(queue);

        System.out.println("En attente de messages sur '" + JmsConfig.QUEUE_NAME
                + "' pendant " + (JmsConfig.LISTEN_TIMEOUT_MS / 1000) + " s...");

        consumer.setMessageListener(message -> {
            if (message instanceof TextMessage textMessage) {
                try {
                    System.out.println("[REÇU]   " + textMessage.getText());
                } catch (Exception e) {
                    System.err.println("Erreur lecture message : " + e.getMessage());
                }
            }
        });

        try {
            Thread.sleep(JmsConfig.LISTEN_TIMEOUT_MS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            consumer.close();
            session.close();
            connection.close();
            System.out.println(">>> Consommateur fermé.");
        }
    }
}
