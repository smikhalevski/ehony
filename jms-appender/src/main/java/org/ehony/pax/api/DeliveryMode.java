/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.pax.api;

/**
 * Wrapper enumeration which allows to avoid numerical configuration of
 * destination delivery mode and use human readable strings.
 */
public enum DeliveryMode {

    /**
     * Messages are posted to destination persistently.
     *
     * @see javax.jms.DeliveryMode#PERSISTENT
     */
    Persistent(javax.jms.DeliveryMode.PERSISTENT),

    /**
     * Messages are posted to destination non-persistently.
     *
     * @see javax.jms.DeliveryMode#NON_PERSISTENT
     */
    NonPersistent(javax.jms.DeliveryMode.NON_PERSISTENT);
    
    private int value;

    DeliveryMode(int value) {
        this.value = value;
    }

    /**
     * Returns native delivery mode integer representation.
     */
    public int toInteger() {
        return value;
    }
}
