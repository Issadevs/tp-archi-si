package tp.jms.ex3;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

/**
 * Exercice 3 – Subscriber Topic
 * Représente un abonné aux notifications de validation d'inscription
 * (portail étudiant, tableau du professeur, secrétariat…).
 * Chaque instance reçoit une copie de TOUS les messages publiés
 * sur le topic (pattern Publish/Subscribe – broadcast).
 *
 * Usage : java tp.jms.ex3.NotificationSubscriber <role>
 *   ex.  : java tp.jms.ex3.NotificationSubscriber PortailEtudiant
 */
public class NotificationSubscriber {

    static final String BROKER_URL = "tcp://localhost:61616";
    static final String TOPIC_NAME = "validationsTopic";
    static final long   LISTEN_MS  = 15_000L;

    public static void main(String[] args) throws Exception {

        String role = (args.length > 0) ? args[0] : "Abonne";

        ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination topic = session.createTopic(TOPIC_NAME);
        MessageConsumer subscriber = session.createConsumer(topic);

        System.out.println("=== NotificationSubscriber [" + role
                + "] – abonne a '" + TOPIC_NAME + "' ===");
        System.out.println("Ecoute pendant " + (LISTEN_MS / 1000) + " s...");

        subscriber.setMessageListener(msg -> {
            try {
                if (msg instanceof TextMessage) {
                    System.out.println("[" + role + "] " + ((TextMessage) msg).getText());
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        });

        Thread.sleep(LISTEN_MS);
        System.out.println(">>> [" + role + "] Abonne ferme.");

        subscriber.close();
        session.close();
        connection.close();
    }
}
