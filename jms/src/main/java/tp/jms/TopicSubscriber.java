package tp.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * Abonné JMS – écoute le topic {@code newsTopic} pendant
 * {@value JmsConfig#LISTEN_TIMEOUT_MS} ms puis se termine.
 *
 * <p><b>Important :</b> lancer ce subscriber <em>avant</em> le publisher.
 * Les messages publiés sans abonné actif sont perdus (abonnement non-durable).</p>
 *
 * <p>Plusieurs instances de ce subscriber peuvent être lancées simultanément ;
 * chacune recevra une copie de chaque message publié – c'est la différence
 * fondamentale avec le modèle Queue.</p>
 *
 * <pre>
 *   cd jms
 *   mvn exec:java -Dexec.mainClass=tp.jms.TopicSubscriber
 * </pre>
 */
public class TopicSubscriber {

    public static void main(String[] args) throws Exception {
        System.out.println("=== TopicSubscriber – connexion sur " + JmsConfig.BROKER_URL + " ===");

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(JmsConfig.BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(JmsConfig.TOPIC_NAME);
        MessageConsumer subscriber = session.createConsumer(topic);

        System.out.println("Abonné au topic '" + JmsConfig.TOPIC_NAME
                + "' – écoute pendant " + (JmsConfig.LISTEN_TIMEOUT_MS / 1000) + " s...");

        subscriber.setMessageListener(message -> {
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
            subscriber.close();
            session.close();
            connection.close();
            System.out.println(">>> Abonné fermé.");
        }
    }
}
