package com.example.demo;

import org.springframework.boot.web.embedded.undertow.UndertowBuilderCustomizer;
import org.springframework.boot.web.embedded.undertow.UndertowServletWebServerFactory;
import org.springframework.context.annotation.Bean;

public class UndertowContainer {

@Bean
    public UndertowServletWebServerFactory embeddedServletContainerFactory() {
    UndertowServletWebServerFactory factory =
                new UndertowServletWebServerFactory();

        factory.addBuilderCustomizers(new UndertowBuilderCustomizer() {
            @Override
            public void customize(io.undertow.Undertow.Builder builder) {
                builder.addHttpListener(8080, "0.0.0.0");
            }
        });

        return factory;
    }

}
