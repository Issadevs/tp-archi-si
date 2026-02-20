package tp.jms.ex3;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

/**
 * Exercice 3 – Pont Queue → Topic
 * Lit les demandes d'inscription depuis la Queue (P2P),
 * valide chacune, puis publie une notification sur le Topic (Pub/Sub)
 * pour informer tous les abonnés en broadcast.
 */
public class AdminProcessor {

    static final String BROKER_URL  = "tcp://localhost:61616";
    static final String QUEUE_NAME  = "inscriptionQueue";
    static final String TOPIC_NAME  = "validationsTopic";
    static final long   TIMEOUT_MS  = 10_000L;

    public static void main(String[] args) throws Exception {

        ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);

        // Consommateur Queue (P2P)
        Destination queue = session.createQueue(QUEUE_NAME);
        MessageConsumer consumer = session.createConsumer(queue);

        // Producteur Topic (Pub/Sub)
        Destination topic = session.createTopic(TOPIC_NAME);
        MessageProducer publisher = session.createProducer(topic);

        System.out.println("=== AdminProcessor – Queue '" + QUEUE_NAME
                + "' → Topic '" + TOPIC_NAME + "' ===");
        System.out.println("Traitement des demandes pendant " + (TIMEOUT_MS / 1000) + " s...");

        long deadline = System.currentTimeMillis() + TIMEOUT_MS;
        int count = 0;

        while (System.currentTimeMillis() < deadline) {
            Message msg = consumer.receive(1_000);
            if (msg instanceof TextMessage) {
                String demande = ((TextMessage) msg).getText();
                System.out.println("[TRAITEMENT] " + demande);

                // Validation et notification broadcast via Topic
                String notification = "Inscription validee : " + demande.trim();
                publisher.send(session.createTextMessage(notification));
                System.out.println("[NOTIFIE]    → " + notification);
                count++;
            }
        }

        System.out.println(">>> AdminProcessor : " + count + " dossier(s) traite(s).");

        consumer.close();
        publisher.close();
        session.close();
        connection.close();
    }
}
