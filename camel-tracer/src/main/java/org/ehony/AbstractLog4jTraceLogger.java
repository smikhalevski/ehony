/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import org.apache.camel.LoggingLevel;
import org.apache.camel.processor.CamelLogger;
import org.ehony.api.TraceLogger;
import org.slf4j.*;

import static org.apache.camel.LoggingLevel.*;
import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;
import static org.apache.commons.lang3.StringUtils.defaultString;

public abstract class AbstractLog4jTraceLogger implements TraceLogger {

    private CamelLogger camelLogger;

    public AbstractLog4jTraceLogger() {
        this(LoggerFactory.getLogger(AbstractLog4jTraceLogger.class));
    }

    public AbstractLog4jTraceLogger(Logger logger) {
        this.camelLogger = new CamelLogger(logger, INFO);
    }

    public void setLoggingLevel(LoggingLevel level) {
        camelLogger.setLevel(defaultIfNull(level, OFF));
    }

    public void setLogName(String name) {
        camelLogger.setLogName(defaultString(name, getClass().getPackage().getName()));
    }

    public CamelLogger getCamelLogger() {
        return camelLogger;
    }
}
