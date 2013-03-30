/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.api;

import org.apache.camel.CamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.osgi.framework.Bundle;

public interface StackFrame {

    /**
     * Timestamp when this stack frame was registered.
     */
    long getTimestamp();

    /**
     * Exchange-wide unique identifier of parent stack frame or <tt>null</tt> when this frame is root.
     */
    Object getParentId();

    /**
     * Exchange-wide unique identifier of this stack frame.
     */
    Object getId();

    /**
     * Unique exchange identifier.
     */
    String getExchangeId();

    /**
     * Camel context where exchange was traced.
     */
    CamelContext getCamelContext();

    /**
     * Bundle where exchange was traced or <tt>null</tt> when not on OSGi environment.
     */
    Bundle getBundle();

    /**
     * Processor where exchange was traced.
     */
    ProcessorDefinition<?> getTargetProcessor();

    /**
     * Type of exchange position relatively to processor.
     */
    TraceType getTraceType();
}
