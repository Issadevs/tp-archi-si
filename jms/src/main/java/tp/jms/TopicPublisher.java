package tp.jms;

import org.apache.activemq.ActiveMQConnectionFactory;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * Éditeur JMS – publie 3 actualités sur le topic {@code newsTopic}.
 *
 * <p><b>Comportement Pub/Sub :</b> chaque abonné actif au moment de la publication
 * reçoit une copie du message. Si aucun abonné n'est connecté, le message est
 * <em>perdu</em> (abonnements non-durables par défaut).</p>
 *
 * <p><b>Important :</b> démarrer au moins un {@link TopicSubscriber} <em>avant</em>
 * ce publisher.</p>
 *
 * <pre>
 *   cd jms
 *   mvn exec:java -Dexec.mainClass=tp.jms.TopicPublisher
 * </pre>
 */
public class TopicPublisher {

    private static final String[] NEWS = {
        "Nouvelle inscription validée pour le cours Architecture SI",
        "Planning TP mis à jour – séance supplémentaire le vendredi",
        "Résultats du contrôle intermédiaire disponibles sur l'ENT"
    };

    public static void main(String[] args) throws Exception {
        System.out.println("=== TopicPublisher – connexion sur " + JmsConfig.BROKER_URL + " ===");

        ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(JmsConfig.BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Topic topic = session.createTopic(JmsConfig.TOPIC_NAME);
        MessageProducer publisher = session.createProducer(topic);

        try {
            for (String news : NEWS) {
                TextMessage msg = session.createTextMessage(news);
                publisher.send(msg);
                System.out.println("[PUBLIÉ]  " + news);
            }
            System.out.println(">>> " + NEWS.length + " messages publiés sur le topic '"
                    + JmsConfig.TOPIC_NAME + "'.");
        } finally {
            publisher.close();
            session.close();
            connection.close();
        }
    }
}
