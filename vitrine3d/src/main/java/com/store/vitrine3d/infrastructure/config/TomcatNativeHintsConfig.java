package com.store.vitrine3d.infrastructure.config;

import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

import java.util.List;

/**
 * Bundles de mensagens do Tomcat/Servlet que o Spring AOT não registra sozinho. Sem eles, o
 * binário nativo lança MissingResourceException quando o Tomcat tenta montar uma mensagem
 * de log ou de erro.
 */
@Configuration(proxyBeanMethods = false)
@ImportRuntimeHints(TomcatNativeHintsConfig.TomcatBundleHints.class)
public class TomcatNativeHintsConfig {

    static class TomcatBundleHints implements RuntimeHintsRegistrar {

        private static final List<String> BUNDLES = List.of(
                "jakarta.servlet.LocalStrings",
                "jakarta.servlet.http.LocalStrings",
                "org.apache.catalina.authenticator.LocalStrings",
                "org.apache.catalina.authenticator.jaspic.LocalStrings",
                "org.apache.catalina.connector.LocalStrings",
                "org.apache.catalina.core.LocalStrings",
                "org.apache.catalina.deploy.LocalStrings",
                "org.apache.catalina.loader.LocalStrings",
                "org.apache.catalina.mapper.LocalStrings",
                "org.apache.catalina.mbeans.LocalStrings",
                "org.apache.catalina.realm.LocalStrings",
                "org.apache.catalina.security.LocalStrings",
                "org.apache.catalina.session.LocalStrings",
                "org.apache.catalina.startup.LocalStrings",
                "org.apache.catalina.util.LocalStrings",
                "org.apache.catalina.valves.LocalStrings",
                "org.apache.catalina.webresources.LocalStrings",
                "org.apache.coyote.LocalStrings",
                "org.apache.coyote.http11.LocalStrings",
                "org.apache.coyote.http11.filters.LocalStrings",
                "org.apache.naming.LocalStrings",
                "org.apache.tomcat.util.LocalStrings",
                "org.apache.tomcat.util.buf.LocalStrings",
                "org.apache.tomcat.util.compat.LocalStrings",
                "org.apache.tomcat.util.descriptor.web.LocalStrings",
                "org.apache.tomcat.util.http.LocalStrings",
                "org.apache.tomcat.util.http.parser.LocalStrings",
                "org.apache.tomcat.util.modeler.LocalStrings",
                "org.apache.tomcat.util.net.LocalStrings",
                "org.apache.tomcat.util.scan.LocalStrings",
                "org.apache.tomcat.util.threads.LocalStrings",
                "org.apache.tomcat.websocket.LocalStrings",
                "org.apache.tomcat.websocket.server.LocalStrings");

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            BUNDLES.forEach(hints.resources()::registerResourceBundle);
        }
    }
}
