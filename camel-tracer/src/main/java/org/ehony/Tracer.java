/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import org.apache.camel.*;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;
import org.ehony.api.*;
import org.ehony.predicates.VerboseTracePredicate;
import org.slf4j.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.ehony.api.TraceType.*;

public class Tracer implements InterceptStrategy {

    /**
     * Exchange property key which keeps identifier of {@link ProcessorDefinition}
     * where {@link org.apache.camel.Exchange} was traced previously.
     *
     * <p>If after this processor exchange would be routed to another bundle,
     * parent-child relationship in hierarchical log would be preserved.</p>
     *
     * <p>Property can keep any {@link java.io.Serializable} object.</p>
     */
    public static final String ROOT_ID = "EhonyRootId";
    public static final String MASTER_ID = "EhonyMasterId";
    /**
     * Name of the property where Ehony system info is stored.
     */
    public static final String TRACE_INFO = "EhonyTraceInfo";

    private static Logger logger = LoggerFactory.getLogger(Tracer.class);

    private TraceLogger traceLogger = new JsonTraceLogger();
    private TracePredicate predicate = new VerboseTracePredicate();
    private IdentifierStrategy identifierStrategy = new HashCodeIdentifierStrategy();

    private boolean enabled = true;

    public void setTraceLogger(TraceLogger logger) {
        this.traceLogger = logger;
    }

    /**
     * Exchange is logged only when <tt>predicate</tt> returns <tt>true</tt>.
     */
    public void setTracePredicate(TracePredicate predicate) {
        this.predicate = predicate;
    }

    /**
     * Sets strategy which produces new identifiers for logging.
     */
    public void setIdentifierStrategy(IdentifierStrategy strategy) {
        this.identifierStrategy = strategy;
    }

    /**
     * Sets flag which allows to stop or resume tracing activities.
     */
    public void setEnabled(boolean flag) {
        this.enabled = flag;
    }

    @Override
    public Processor wrapProcessorInInterceptors(final CamelContext context, final ProcessorDefinition<?> to, Processor processor, final Processor nextProcessor) {
        if (to.isAbstract()) {
            // Do not wrap abstract nodes: OnException, OnCompletion, etc.
            // They are to be restored as elements of trace while traversing.
            return processor;
        } else {
            return new DelegateAsyncProcessor(processor) {

                @Override
                @SuppressWarnings("unchecked")
                public boolean process(final Exchange exchange, final AsyncCallback callback) {
                    AsyncCallback traceCallback = callback;
                    if (enabled) {
                        try {
                            final TraceInfo info = getTraceInfo(exchange);
                            info.stepInto(to);
                            if (predicate.permitLogOnStepInto(exchange, to)) {
                                traceLogger.log(exchange, info.getTraceCache());
                                info.flush();
                            }
                            traceCallback = new AsyncCallback() {
                                @Override
                                public void done(boolean doneSync) {
                                    try {
                                        info.stepOut(to);
                                        if (predicate.permitLogOnStepOut(exchange, to)) {
                                            traceLogger.log(exchange, info.getTraceCache());
                                            info.flush();
                                        }
                                    } catch (Exception e) {
                                        logger.error("Cannot trace outbound exchange.", e);
                                    }
                                    callback.done(doneSync);
                                }
                            };
                        } catch (Exception e) {
                            logger.error("Cannot trace inbound exchange.", e);
                        }
                    }
                    return super.process(exchange, traceCallback);
                }
            };
        }
    }

    private TraceInfo getTraceInfo(Exchange exchange) {
        TraceInfo info = exchange.getProperty(TRACE_INFO, TraceInfo.class);
        if (info != null) {
            if (info.exchange != exchange) {
                if (info.previous != null && info.previous.exchange == exchange) {
                    info = info.previous;
                } else {
                    info = new TraceInfo(info, exchange);
                    exchange.setProperty(TRACE_INFO, info);
                }
            }
        } else {
            info = new TraceInfo(exchange);
            exchange.setProperty(TRACE_INFO, info);
        }
        return info;
    }

    private class TraceInfo {

        private String masterId;
        private Exchange exchange;
        public TraceInfo previous;
        private Stack<StackFrame> traceCache = new Stack<StackFrame>();

        private AtomicInteger traceDepth = new AtomicInteger();
        private Stack<Object> parentIds = new Stack<Object>();
        private Stack<String> exchangeIds = new Stack<String>();

        private TraceInfo(Exchange exchange) {
            this(null, exchange);
        }

        private TraceInfo(TraceInfo previous, Exchange exchange) {
            this.exchange = exchange;
            this.previous = previous;
            masterId = exchange.getProperty(MASTER_ID, String.class);
            if (previous != null) {
                traceDepth.set(previous.traceDepth.intValue());
                parentIds.addAll(previous.parentIds);
                exchangeIds.addAll(previous.exchangeIds);
                traceCache = previous.traceCache;
                masterId = previous.masterId;
            }
            if (masterId == null) {
                masterId = UUID.randomUUID().toString();
            }
            exchange.setProperty(MASTER_ID, masterId);
        }

        void flush() {
            traceCache.clear();
        }

        /**
         * Reveals transitions that must be performed to reach processor <tt>to</tt>.
         */
        private void populateStackFrames(Object rootId, ProcessorDefinition<?> to, TraceType traceType) {
            ProcessorDefinition<?> parent = to.getParent();
            if (!to.isAbstract() && parent != null) {
                Object parentId = identifyParent(parent),
                        siblingId = identifySibling(parent);
                if (!parentIds.isEmpty() && parentIds.peek().equals(parentId)) {
                    rootId = parentId;
                } else {
                    if (parentIds.isEmpty() || !parentIds.peek().equals(siblingId)) {
                        populateStackFrames(rootId, parent, Transient);
                    }
                    rootId = siblingId;
                }
            }
            Object id = identifySibling(to);
            parentIds.add(id);
            traceCache.add(new DefaultStackFrame(exchange, rootId, id, to, traceType));
        }

        void stepInto(ProcessorDefinition<?> to) {
            exchangeIds.add(exchange.getExchangeId());
            populateStackFrames(exchange.getProperty(ROOT_ID), to, StepInto);
            exchange.setProperty(ROOT_ID, parentIds.peek());
            traceDepth.incrementAndGet();
        }

        void stepOut(ProcessorDefinition<?> from) {
            traceDepth.decrementAndGet();
            parentIds.setSize(parentIds.lastIndexOf(identifySibling(from)));
            traceCache.add(new DefaultStackFrame(exchange, parentIds.peek(), identifySibling(from), from, StepOut));
            exchange.setProperty(ROOT_ID, identifySibling(from));
            exchangeIds.pop();
        }

        private Object identifySibling(ProcessorDefinition<?> node) {
            return identifierStrategy.getIdentifierOf(node, traceDepth.intValue(), exchangeIds.peek());
        }

        private Object identifyParent(ProcessorDefinition<?> node) {
            if (exchangeIds.size() < 2) {
                return null;
            }
            return identifierStrategy.getIdentifierOf(node, traceDepth.intValue() - 1, exchangeIds.get(exchangeIds.size() - 2));
        }

        public Stack<StackFrame> getTraceCache() {
            return traceCache;
        }
    }
}