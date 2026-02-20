package tp.jms.ex3;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.*;

/**
 * Exercice 3 – Producteur Queue
 * Simule des étudiants qui soumettent des demandes d'inscription.
 * Chaque message est déposé dans la Queue "inscriptionQueue" et sera
 * traité par UN SEUL AdminProcessor (pattern Point-to-Point).
 */
public class InscriptionProducer {

    static final String BROKER_URL = "tcp://localhost:61616";
    static final String QUEUE_NAME  = "inscriptionQueue";

    public static void main(String[] args) throws Exception {

        String[] demandes = {
            "Alice Martin      → Architecture des SI",
            "Bob Dupont         → Réseaux et Protocoles",
            "Clara Nguyen       → Bases de données avancées",
            "David Leroy        → Architecture des SI"
        };

        ConnectionFactory factory = new ActiveMQConnectionFactory(BROKER_URL);
        Connection connection = factory.createConnection();
        connection.start();

        Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        Destination queue = session.createQueue(QUEUE_NAME);
        MessageProducer producer = session.createProducer(queue);

        System.out.println("=== InscriptionProducer – envoi vers '" + QUEUE_NAME + "' ===");

        for (String demande : demandes) {
            TextMessage msg = session.createTextMessage(demande);
            producer.send(msg);
            System.out.println("[DEMANDE]  " + demande);
        }

        System.out.println(">>> " + demandes.length + " demandes déposées dans la queue.");

        producer.close();
        session.close();
        connection.close();
    }
}
