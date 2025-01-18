package gg.pufferfish.pufferfish.sentry;

import io.sentry.Sentry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SentryManager {

    private static final Logger logger = LogManager.getLogger(SentryManager.class);

    private SentryManager() {

    }

    private static boolean initialized = false;

    public static synchronized void init(Level logLevel) {
        if (initialized) {
            return;
        }
        if (logLevel == null) {
            logger.error("Invalid log level, defaulting to WARN.");
            logLevel = Level.WARN;
        }
        try {
            initialized = true;

            Sentry.init(options -> {
                options.setDsn(org.dreeam.leaf.config.modules.misc.SentryDSN.sentryDsn);
                options.setMaxBreadcrumbs(100);
            });

            PufferfishSentryAppender appender = new PufferfishSentryAppender(logLevel);
            appender.start();
            ((org.apache.logging.log4j.core.Logger) LogManager.getRootLogger()).addAppender(appender);
            logger.info("Sentry logging started!");
        } catch (Exception e) {
            logger.warn("Failed to initialize sentry!", e);
            initialized = false;
        }
    }

}
