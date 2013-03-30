package org.ehony;

import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.model.language.SpELExpression;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.ehony.api.*;
import org.ehony.predicates.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Long.parseLong;
import static org.apache.commons.lang3.StringUtils.isNumeric;
import static org.apache.commons.lang3.StringUtils.substringAfterLast;

public class TraceFormatterTest extends CamelTestSupport {

    Tracer tracer = new Tracer();

    class TestIdentifierStrategy implements IdentifierStrategy {

        List<ProcessorDefinition> nodes = new ArrayList<ProcessorDefinition>();

        @Override
        synchronized public Object getIdentifierOf(ProcessorDefinition<?> node, int nestingDepth, String exchangeId) {
            int index = nodes.indexOf(node);
            if (index < 0) {
                index = nodes.size();
                nodes.add(node);
            }
            String exchangeOffset = substringAfterLast(exchangeId, "-");
            if (!isNumeric(exchangeOffset)) {
                exchangeOffset = "0";
            }
            return "_" + index + "_" + exchangeOffset + "_" + nestingDepth + "_";
        }
    }

    class TestTraceLogger implements TraceLogger {

        @Override
        public void log(Exchange exchange, List<StackFrame> trace) {
            String text = "";
            for (StackFrame branch : trace) {
                if (branch.getTraceType() == TraceType.StepInto) {
                    text += "-=>";
                }
                if (branch.getTraceType() == TraceType.StepOut) {
                    text += "<=-";
                }
                if (branch.getTraceType() == TraceType.Transient) {
                    text += " ^ ";
                }
                text += "{" + branch.getParentId() + "->" + branch.getId() + ":" + branch.getTargetProcessor().getShortName() + "}\n";
            }
            System.out.println(text);
        }
    }

    @Test
    public void testTracerTraversing() throws Exception {
        sendBody("direct:start", null);


//        Assert.assertEquals(
//                output.toString(),
//                "-=>{null->0:route}-=>{0->1:choice}"
//                        + "-=>{1->39:when}-=>{39->40:setProperty}"
//                        + "<=-{39->40:setProperty}"
//                        + "-=>{39->41:to}"
//                        + "-=>{41->80:route}-=>{80->79:multicast}-=>{79->81:pipeline}"
//                        + "-=>{81->119:log}"
//                        + "<=-{81->119:log}"
//                        + "<=-{79->81:pipeline}"
//                        + "<=-{39->41:to}"
//                        + "-=>{39->46:log}"
//                        + "<=-{39->46:log}"
//                        + "-=>{39->47:to}"
//                        + "-=>{47->74:route}-=>{74->75:choice}"
//                        + "-=>{75->122:when}-=>{122->123:setProperty}"
//                        + "<=-{122->123:setProperty}"
//                        + "-=>{122->124:log}"
//                        + "<=-{122->124:log}"
//                        + "-=>{122->125:to}"
//                        + "-=>{125->148:route}-=>{148->149:choice}"
//                        + "-=>{149->196:when}-=>{196->197:setProperty}"
//                        + "<=-{196->197:setProperty}"
//                        + "-=>{196->198:log}"
//                        + "<=-{196->198:log}"
//                        + "-=>{196->199:to}"
//                        + "-=>{199->222:route}-=>{222->223:choice}"
//                        + "-=>{223->274:otherwise}-=>{274->275:log}"
//                        + "<=-{274->275:log}"
//                        + "<=-{222->223:choice}"
//                        + "-=>{222->239:setBody}"
//                        + "<=-{222->239:setBody}"
//                        + "-=>{222->240:split}"
//                        + "-=>{240->278:log}"
//                        + "<=-{240->278:log}"
//                        + "-=>{240->278:log}"
//                        + "<=-{240->278:log}"
//                        + "-=>{240->278:log}"
//                        + "<=-{240->278:log}"
//                        + "<=-{222->240:split}"
//                        + "-=>{222->242:log}"
//                        + "<=-{222->242:log}"
//                        + "<=-{196->199:to}"
//                        + "<=-{148->149:choice}"
//                        + "-=>{148->165:setBody}"
//                        + "<=-{148->165:setBody}"
//                        + "-=>{148->166:split}"
//                        + "-=>{166->204:log}"
//                        + "<=-{166->204:log}"
//                        + "-=>{166->204:log}"
//                        + "<=-{166->204:log}"
//                        + "-=>{166->204:log}"
//                        + "<=-{166->204:log}"
//                        + "<=-{148->166:split}"
//                        + "-=>{148->168:log}"
//                        + "<=-{148->168:log}"
//                        + "<=-{122->125:to}"
//                        + "<=-{74->75:choice}"
//                        + "-=>{74->91:setBody}"
//                        + "<=-{74->91:setBody}"
//                        + "-=>{74->92:split}"
//                        + "-=>{92->130:log}"
//                        + "<=-{92->130:log}"
//                        + "-=>{92->130:log}"
//                        + "<=-{92->130:log}"
//                        + "-=>{92->130:log}"
//                        + "<=-{92->130:log}"
//                        + "<=-{74->92:split}"
//                        + "-=>{74->94:log}"
//                        + "<=-{74->94:log}"
//                        + "<=-{39->47:to}"
//                        + "<=-{0->1:choice}"
//                        + "-=>{0->17:setBody}"
//                        + "<=-{0->17:setBody}"
//                        + "-=>{0->18:split}"
//                        + "-=>{18->56:log}"
//                        + "<=-{18->56:log}"
//                        + "-=>{18->56:log}"
//                        + "<=-{18->56:log}"
//                        + "-=>{18->56:log}"
//                        + "<=-{18->56:log}"
//                        + "<=-{0->18:split}"
//                        + "-=>{0->20:log}"
//                        + "<=-{0->20:log}"
//        );
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                tracer.setIdentifierStrategy(new TestIdentifierStrategy());
                tracer.setTraceLogger(new TestTraceLogger());
//                tracer.setTracePredicate(new OnFailureTracePredicate());

                context.addInterceptStrategy(tracer);

                from("direct:start")
                    .choice()
                        .when(header("i").isNull())
                            .setProperty("i", constant(0))
                            .to("direct:pipeTest")
                            .log("i = ${property.i}")
                            .to("direct:start")
                        .when(header("i").isLessThan(2))
                            .setProperty("i", new SpELExpression("#{exchange.properties['i'] + 1}"))
                            .log("i = ${property.i}")
                            .to("direct:start")
                        .otherwise()
                            .log("finish")
                    .end()
                    .setBody(simple("1,2,3"))
                    .split(body(String.class).tokenize(","))
                        .log("${body}")
                    .end()
                    .log("exit");

                from("direct:pipeTest")
                    .multicast()
                        .pipeline()
                            .log("pipeline")
                        .end()
                    .end();
            }
        };
    }
}
