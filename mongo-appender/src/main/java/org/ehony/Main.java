/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import org.springframework.context.support.ClassPathXmlApplicationContext;

public class Main {

    public static void main(String... args) {
        new ClassPathXmlApplicationContext("/META-INF/spring/context.xml", Main.class);
    }
}
