/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.predicates;

import org.apache.camel.Exchange;
import org.apache.camel.model.ProcessorDefinition;
import org.ehony.api.TracePredicate;

public class VerboseTracePredicate implements TracePredicate {

    @Override
    public boolean permitLogOnStepInto(Exchange exchange, ProcessorDefinition<?> to) {
        return true;
    }

    @Override
    public boolean permitLogOnStepOut(Exchange exchange, ProcessorDefinition<?> from) {
        return true;
    }
}
