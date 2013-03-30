/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony.api;

public enum TraceType {

    StepInto, StepOut,
    /**
     * Exchange was extracted during traversing, not direct tracing.
     * {@link org.ehony.Tracer} did not intercept corresponding processor.
     */
    Transient
}
