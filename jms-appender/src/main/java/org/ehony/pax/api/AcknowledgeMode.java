/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.pax.api;

/**
 * Wrapper enumeration which allows to avoid numerical configuration of
 * destination acknowledge mode and use human readable strings in config.
 */
public enum AcknowledgeMode {

    /**
     * Session automatically acknowledges receipt of a client message either
     * when the session has successfully returned from a call to receive or when
     * the message listener the session has called to process the message
     * successfully returns.
     *
     * @see javax.jms.Session#AUTO_ACKNOWLEDGE
     */
    Auto(javax.jms.Session.AUTO_ACKNOWLEDGE),

    /**
     * The client acknowledges a consumed message by calling the acknowledge
     * method of the message.
     *
     * <p>Acknowledging a consumed message acknowledges all messages that the
     * session has consumed.</p>
     *
     * <p>When client acknowledgment mode is used, a client may build up a large
     * number of unacknowledged messages while attempting to process them. A JMS
     * provider should provide administrators with a way to limit client overrun
     * so that clients are not driven to resource exhaustion and ensuing failure
     * when some resource they are using is temporarily blocked.</p>
     *
     * @see javax.jms.Session#CLIENT_ACKNOWLEDGE
     */
    Client(javax.jms.Session.CLIENT_ACKNOWLEDGE),

    /**
     * Instructs the session to lazily acknowledge the delivery of messages.
     *
     * <p>This is likely to result in the delivery of some duplicate messages if
     * the JMS provider fails, so it should only be used by consumers that can
     * tolerate duplicate messages. Use of this mode can reduce session overhead
     * by minimizing the work the session does to prevent duplicates.</p>
     *
     * @see javax.jms.Session#DUPS_OK_ACKNOWLEDGE
     */
    PermitDuplicates(javax.jms.Session.DUPS_OK_ACKNOWLEDGE);
    
    private int value;

    AcknowledgeMode(int value) {
        this.value = value;
    }

    /**
     * Returns native acknowledge mode integer representation.
     */
    public int toInteger() {
        return value;
    }
}
