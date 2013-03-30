/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.api;

import com.google.gson.annotations.Expose;

import java.util.Map;

public class Entry {

    public enum Status {
        // Preserve order in this enum to allow further simple ordinal comparison of statuses.
        Success, Warning, Exception, Fault
    }

    public enum ExchangePattern {
        InOnly, RobustInOnly, InOut, InOptionalOut, OutOnly, RobustOutOnly, OutIn, OutOptionalIn,
        /**
         * Camel-assigned exchange pattern cannot be cast to this any value in this enum.
         */
        Unknown
    }

    @Expose public String masterId,
                          breadcrumbId;
    @Expose public Object exchangeId,
                          parentId,
                          id;
    @Expose public Integer index;
    @Expose public Long timestamp;

    @Expose public Long osgiBundleId;
    @Expose public String osgiBundleName,
                          camelContextId,
                          camelRouteId;
    @Expose public Object processor;
    @Expose public TraceType traceType;

    // Only data-rich records must have fields below this line.

    @Expose public ExchangePattern exchangePattern;
    @Expose public Status status;
    @Expose public String exception;

    @Expose public Map<String, Object> properties;
    @Expose public Message in, out;

    public static class Message {

        @Expose public Map<String, Object> headers;
        @Expose public String bodyClass;
        @Expose public Object body;
    }
}