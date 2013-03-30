/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import org.apache.camel.*;
import org.apache.camel.model.ProcessorDefinition;
import org.ehony.api.*;
import org.osgi.framework.Bundle;

import java.lang.reflect.Method;

public class DefaultStackFrame implements StackFrame {

    private long timestamp = System.currentTimeMillis();
    private String exchangeId;
    private Object parentId, id;
    private CamelContext camelContext;
    private ProcessorDefinition node;
    private TraceType traceType;

    public DefaultStackFrame(Exchange exchange, Object parentId, Object id, ProcessorDefinition<?> node, TraceType traceType) {
        this.exchangeId = exchange.getExchangeId();
        this.parentId = parentId;
        this.id = id;
        this.camelContext = exchange.getContext();
        this.node = node;
        this.traceType = traceType;
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public Object getParentId() {
        return parentId;
    }

    @Override
    public Object getId() {
        return id;
    }

    @Override
    public String getExchangeId() {
        return exchangeId;
    }

    @Override
    public CamelContext getCamelContext() {
        return camelContext;
    }

    /**
     * Fetches information about OSGi bundle where exchange was created.
     *
     * <p>When Camel is not being run in OSGi context, then <tt>null</tt> is
     * returned. This method uses reflection-based invocation of
     * <code>getBundle()</code> method of application context class loader.
     * Reflection is used to avoid additional dependencies and increase
     * portability among different class loader implementations.</p>
     *
     * @return {@link org.osgi.framework.Bundle} or <tt>null</tt> when failed
     *         to fetch bundle info.
     */
    @Override
    public Bundle getBundle() {
        try {
            ClassLoader loader = getCamelContext().getApplicationContextClassLoader();
            Method method = loader.getClass().getMethod("getBundle");
            method.setAccessible(true);
            return (Bundle) method.invoke(loader);
        } catch (Throwable t) {
            // Cannot invoke ClassLoader#getBundle()
            return null;
        }
    }

    /**
     * Reference to <b>actual</b> {@link ProcessorDefinition} where exchange was traced.
     *
     * <p><b>Important!</b> Processor returned by this method can differ from one stored in
     * {@link org.apache.camel.spi.UnitOfWork} exploited by exchange.</p>
     */
    @Override
    public ProcessorDefinition<?> getTargetProcessor() {
        return node;
    }

    @Override
    public TraceType getTraceType() {
        return traceType;
    }
}