/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.pax;

import org.ehony.pax.api.AcknowledgeMode;
import org.ehony.pax.api.DeliveryMode;
import org.ehony.pax.api.DestinationType;
import org.ops4j.pax.logging.spi.PaxAppender;
import org.ops4j.pax.logging.spi.PaxLoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.naming.InitialContext;
import java.util.*;
import java.util.Queue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static org.ehony.pax.api.DestinationType.Topic;

public class JmsAppender extends TimerTask implements PaxAppender {

    private static Logger logger = LoggerFactory.getLogger(JmsAppender.class);

    private Queue<PaxLoggingEvent> events = new SynchronousQueue<PaxLoggingEvent>();
    private Lock lock = new ReentrantLock();

    private Properties contextProperties;
    private int threshold;
    private long bufferFlushPeriod;
    private boolean failoverManaged;
    private boolean transacted;
    private AcknowledgeMode acknowledgeMode;
    private DestinationType destinationType;
    private DeliveryMode deliveryMode;
    private String subject;

    private Timer timer;
    private Connection connection;
    private Session session;
    private MessageProducer producer;

    @Override
    public void run() {
        for (PaxLoggingEvent event : new ArrayDeque<PaxLoggingEvent>(events)) {
            if (events.remove(event)) {
                logger.info("Dispatching message from temporary queue.");
                doAppend(event);
            }
        }
    }

    @Override
    public void doAppend(PaxLoggingEvent event) {
        try {
            producer.send(session.createTextMessage(event.getMessage()));
        } catch (Exception e) {
            if (events.size() < threshold) {
                events.offer(event);
                logger.warn("Dispatch of message failed. Temporary queue contains " + events.size() + " out of " + threshold + " messages.", e);
            } else {
                logger.error("Temporary queue exhausted.");
            }
            if (!failoverManaged) {
                reconnect();
            }
        }
    }

    public void reconnect() {
        if (lock.tryLock()) {
            lock.lock();
            try {
                disconnect();
                connect();
            } finally {
                lock.unlock();
            }
        }
    }

    public void connect() {
        timer = new Timer();
        timer.schedule(this, bufferFlushPeriod);
        try {
            InitialContext jndi = new InitialContext(contextProperties);
            ConnectionFactory factory = ((ConnectionFactory) jndi.lookup("ConnectionFactory"));
            connection = factory.createConnection();
            connection.start();
            session = connection.createSession(transacted, acknowledgeMode.toInteger());
            Destination destination;
            if (Topic.equals(destinationType)) {
                destination = session.createTopic(subject);
            } else {
                destination = session.createQueue(subject);
            }
            producer = session.createProducer(destination);
            producer.setDeliveryMode(deliveryMode.toInteger());
        } catch (Exception e) {
            logger.error("Provider connection failed.", e);
        }
    }

    public void disconnect() {
        timer.cancel();
        if (events.size() < threshold) {
            logger.warn("Discarding " + events.size() + " undispatched messages.");
        }
        try {
            session.close();
            connection.close();
        } catch (Exception e) {
            logger.error("Connection termination failed.", e);
        }
    }

    public void setContextProperties(Properties properties) {
        this.contextProperties = properties;
    }

    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    public void setBufferFlushPeriod(long period) {
        this.bufferFlushPeriod = period;
    }

    public void setFailoverManaged(boolean flag) {
        this.failoverManaged = flag;
    }

    public void setTransacted(boolean transacted) {
        this.transacted = transacted;
    }

    public void setAcknowledgeMode(AcknowledgeMode mode) {
        this.acknowledgeMode = mode;
    }

    public void setDestinationType(DestinationType type) {
        this.destinationType = type;
    }

    public void setDeliveryMode(DeliveryMode mode) {
        this.deliveryMode = mode;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }
}