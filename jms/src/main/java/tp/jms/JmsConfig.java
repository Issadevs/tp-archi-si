package tp.jms;

/**
 * Constantes de configuration JMS partagées entre toutes les classes du TP.
 *
 * <ul>
 *   <li>Broker : ActiveMQ Classic sur {@value #BROKER_URL}</li>
 *   <li>Queue P2P : {@value #QUEUE_NAME}</li>
 *   <li>Topic Pub/Sub : {@value #TOPIC_NAME}</li>
 * </ul>
 */
public final class JmsConfig {

    /** URL du broker ActiveMQ Classic. */
    public static final String BROKER_URL = "tcp://localhost:61616";

    /** Nom de la queue point-à-point (P2P). */
    public static final String QUEUE_NAME = "customerQueue";

    /** Nom du topic publication/abonnement (Pub/Sub). */
    public static final String TOPIC_NAME = "newsTopic";

    /** Durée d'écoute en millisecondes pour les consommateurs/abonnés. */
    public static final long LISTEN_TIMEOUT_MS = 10_000L;

    private JmsConfig() {}
}
