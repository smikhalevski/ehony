/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.api;

import org.apache.camel.Exchange;

import java.util.List;

public interface TraceLogger {

    void log(Exchange exchange, List<StackFrame> trace);
}
