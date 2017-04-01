package com.necoutezpas.logging;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.Converter;
import ch.qos.logback.core.pattern.ConverterUtil;
import ch.qos.logback.core.pattern.parser.Node;
import ch.qos.logback.core.pattern.parser.Parser;
import ch.qos.logback.core.spi.ScanException;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by pascal on 01/04/2017.
 */
public class GoogleCloudLoggingV2Layout extends com.google.cloud.logging.GoogleCloudLoggingV2Layout {

    static final int INTIAL_STRING_BUILDER_SIZE = 256;
    /**
     * Default pattern string for log output.
     */
    static final String DEFAULT_CONVERSION_PATTERN = "%mdc%msg";

    protected String pattern;

    protected Converter<ILoggingEvent> head;

    public GoogleCloudLoggingV2Layout() {
        super();
        pattern = DEFAULT_CONVERSION_PATTERN;
    }

    /**
     * Set the <b>ConversionPattern </b> option. This is the string which controls
     * formatting and consists of a mix of literal content and conversion
     * specifiers.
     */
    public void setPattern(String conversionPattern) {
        pattern = conversionPattern;
    }

    /**
     * Returns the value of the <b>ConversionPattern </b> option.
     */
    public String getPattern() {
        return pattern;
    }

    /**
     * Parses the pattern and creates the Converter linked list.
     */
    @Override
    public void start() {
        int errorCount = 0;

        try {
            Parser<ILoggingEvent> p = new Parser<ILoggingEvent>(pattern);
            p.setContext(getContext());
            Node t = p.parse();
            this.head = p.compile(t, getEffectiveConverterMap());
            ConverterUtil.startConverters(this.head);
        } catch (ScanException ex) {
            addError("Incorrect pattern found", ex);
            errorCount++;
        }

        if (errorCount == 0) {
            super.started = true;
        }
        super.start();
    }

    protected Map<String, String> getDefaultConverterMap() {
        return PatternLayout.defaultConverterMap;
    }

    /**
     * Returns a map where the default converter map is merged with the map
     * contained in the context.
     */
    public Map<String, String> getEffectiveConverterMap() {
        Map<String, String> effectiveMap = new HashMap<String, String>();

        // add the least specific map fist
        Map<String, String> defaultMap = getDefaultConverterMap();
        if (defaultMap != null) {
            effectiveMap.putAll(defaultMap);
        }

        // contextMap is more specific than the default map
        Context context = getContext();
        if (context != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> contextMap = (Map<String, String>) context.getObject(CoreConstants.PATTERN_RULE_REGISTRY);
            if (contextMap != null) {
                effectiveMap.putAll(contextMap);
            }
        }
        return effectiveMap;
    }


    @Override
    protected Map toJsonMap(ILoggingEvent event) {
        @SuppressWarnings("unchecked")
        Map<String,Object> map = super.toJsonMap(event);
        String message = (String) map.get("message");
        String convertedMessage = writeLoopOnConverters(event);
        map.put("message", convertedMessage);
        return map;
    }

    protected String writeLoopOnConverters(ILoggingEvent event) {
        StringBuilder strBuilder = new StringBuilder(INTIAL_STRING_BUILDER_SIZE);
        Converter<ILoggingEvent> c = head;
        while (c != null) {
            c.write(strBuilder, event);
            c = c.getNext();
        }
        return strBuilder.toString();
    }
}
