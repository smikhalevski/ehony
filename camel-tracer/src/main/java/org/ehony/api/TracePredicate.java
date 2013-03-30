/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.api;

import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;

public interface TracePredicate {

    /**
     * Invoked <b>before entering</b> intercepted processor.
     */
    boolean permitLogOnStepInto(Exchange exchange, ProcessorDefinition<?> to);

    /**
     * Invoked <b>after completing</b> intercepted processor.
     */
    boolean permitLogOnStepOut(Exchange exchange, ProcessorDefinition<?> from);
}
