/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.api;

import org.apache.camel.model.ProcessorDefinition;

/**
 * Strategy which produces identifiers for logging.
 */
public interface IdentifierStrategy {

    /**
     * Returns exchange-wide unique identifier of provided processor.
     *
     * @param node       node to produce identifier for, never <tt>null</tt>.
     * @param traceDepth current nesting depth of trace. Note that same processor can be
     *                   executed at different tracing depth because of recursive calls.
     * @param exchangeId Camel-assigned exchange identifier, never <tt>null</tt>.
     * @see org.apache.camel.Exchange#getExchangeId()
     */
    Object getIdentifierOf(ProcessorDefinition<?> node, int traceDepth, String exchangeId);
}
