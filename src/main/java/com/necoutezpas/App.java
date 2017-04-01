package com.necoutezpas;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

/**
 * Hello world!
 *
 */
public class App
{
    private static final Logger logger = LoggerFactory.getLogger(App.class);

    public static void main( String[] args )
    {
        String name = "Pascal";
        MDC.put("id", "hop");
        logger.info("Hello from Bar.");
        logger.debug("Hello, {}.", name);
    }
}
