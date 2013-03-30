/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import org.apache.camel.model.ProcessorDefinition;
import org.ehony.api.IdentifierStrategy;

import static java.lang.Long.*;
import static java.lang.System.identityHashCode;
import static org.apache.commons.lang3.StringUtils.*;

/**
 * Identifier strategy that uses system identity hash code of provided node.
 */
public class HashCodeIdentifierStrategy implements IdentifierStrategy {

    @Override
    public Object getIdentifierOf(ProcessorDefinition<?> node, int traceDepth, String exchangeId) {
        String exchangeOffset = substringAfterLast(exchangeId, "-");
        if (!isNumeric(exchangeOffset)) {
            exchangeOffset = "0";
        }
        return toHexString(parseLong(identityHashCode(node) + exchangeOffset + traceDepth));
    }
}
