package com.payneteasy.srvlog;

import com.payneteasy.srvlog.config.IStartupConfig;
import com.payneteasy.startup.parameters.StartupParametersFactory;
import org.eclipse.jetty.plus.webapp.EnvConfiguration;
import org.eclipse.jetty.plus.webapp.PlusConfiguration;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class StartSrvlogUIApp {

    private static final Logger LOG = LoggerFactory.getLogger(StartSrvlogUIApp.class);

    public static void main(String[] args) throws Exception {

        try {

            IStartupConfig config = StartupParametersFactory.getStartupParameters(IStartupConfig.class);

            Server server = new Server(config.getJettyPort());

            WebAppContext webAppContext = new WebAppContext();
            webAppContext.setContextPath(config.getJettyContext());
            webAppContext.setDefaultsDescriptor("src/main/webapp/WEB-INF/web.xml");
            webAppContext.setDescriptor(config.webDescriptorPath());
            webAppContext.setWar("src/main/webapp");

            EnvConfiguration envConfiguration = new EnvConfiguration();
            envConfiguration.setJettyEnvXml(new File(config.getJettyEnvConfigPath()).toURI().toURL());

            Configuration[] configurations = new Configuration[]{
                    new WebInfConfiguration(),
                    new WebXmlConfiguration(),
                    new MetaInfConfiguration(),
                    new FragmentConfiguration(),
                    envConfiguration,
                    new PlusConfiguration(),
                    new JettyWebXmlConfiguration()
            };

            webAppContext.setConfigurations(configurations);
            webAppContext.setServer(server);

            server.setHandler(webAppContext);

            server.start();
            server.setStopAtShutdown(true);

        } catch (Exception e) {
            LOG.error("Cannot start server app", e);
            System.exit(1);
        }
    }
}
