/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.model.*;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.ehony.api.*;

import java.util.*;

import static org.apache.camel.Exchange.*;
import static org.apache.camel.model.ProcessorDefinitionHelper.getRoute;
import static org.apache.camel.util.MessageHelper.getBodyTypeName;
import static org.ehony.api.Entry.ExchangePattern.Unknown;
import static org.ehony.api.Entry.Status.*;
import static org.ehony.api.TraceType.*;

public class JsonTraceLogger extends AbstractLog4jTraceLogger {

    private Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

    private boolean showProperties = true,
                    showHeaders = true,
                    showBody = true;

    public void setShowProperties(boolean showProperties) {
        this.showProperties = showProperties;
    }

    public void setShowHeaders(boolean showHeaders) {
        this.showHeaders = showHeaders;
    }

    public void setShowBody(boolean showBody) {
        this.showBody = showBody;
    }

    @Override
    public void log(Exchange exchange, List<StackFrame> trace) {
        exchange = exchange.copy();
        LinkedList<Entry> entries = new LinkedList<Entry>();
        for (StackFrame branch : trace) {
            entries.add(createEntry(exchange, branch));
        }
        enrichEntry(entries.getLast(), exchange);
        getCamelLogger().log(gson.toJson(entries));
    }

    /**
     * Core system identifiers are populated, required for all logged records.
     */
    private Entry createEntry(Exchange exchange, StackFrame branch) {
        Entry entry = new Entry();

        entry.masterId = exchange.getProperty(Tracer.MASTER_ID, String.class);
        entry.breadcrumbId = exchange.getProperty(BREADCRUMB_ID, String.class);
        entry.exchangeId = branch.getExchangeId();
        entry.parentId = branch.getParentId();
        entry.id = branch.getId();

        ProcessorDefinition<?> to = branch.getTargetProcessor();
        if (to.getParent() != null) {
            entry.index = to.getParent().getOutputs().indexOf(to);
        }
        entry.timestamp = branch.getTimestamp();

        if (branch.getBundle() != null) {
            entry.osgiBundleId = branch.getBundle().getBundleId();
            entry.osgiBundleName = branch.getBundle().getSymbolicName();
        }
        entry.camelContextId = exchange.getContext().getName();
        RouteDefinition route = getRoute(branch.getTargetProcessor());
        if (route != null && route.hasCustomIdAssigned()) {
            entry.camelRouteId = route.getId();
        }
        entry.processor = branch.getTargetProcessor().toString();
        entry.traceType = Transient;

        return entry;
    }

    /**
     * Only data-rich records must be enriched with these fields.
     */
    @SuppressWarnings("unchecked")
    private void enrichEntry(Entry entry, Exchange exchange) {

        try {
            entry.exchangePattern = Entry.ExchangePattern.valueOf(exchange.getPattern().toString());
        } catch (Exception e) {
            entry.exchangePattern = Unknown;
        }
        entry.status = Success;
        if (exchange.getException() != null) {
            entry.status = Exception;
            entry.exception = ExceptionUtils.getStackTrace(exchange.getException());
        }

        // Properties
        if (showProperties) {
            try {
                exchange.getProperties().remove(Tracer.TRACE_INFO);
                gson.toJson(exchange.getProperties());
                entry.properties = exchange.getProperties();
            } catch (Exception e) {
            }
        }

        // Inbound message.
        {
            Message in = exchange.getIn();
            entry.in = new Entry.Message();
            if (showHeaders) {
                entry.in.headers = in.getHeaders();
            }
            if (showBody) {
                entry.in.body = in.getBody();
            }
            entry.in.bodyClass = getBodyTypeName(in);
            if (in.isFault()) {
                entry.status = Fault;
            }
        }

        // Outbound message.
        if (exchange.hasOut()) {
            Message out = exchange.getOut();
            entry.out = new Entry.Message();
            if (showHeaders) {
                entry.out.headers = out.getHeaders();
            }
            if (showBody) {
                entry.out.body = out.getBody();
            }
            entry.out.bodyClass = getBodyTypeName(out);
            if (out.isFault()) {
                entry.status = Fault;
            }
        }
    }
}
