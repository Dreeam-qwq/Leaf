package org.dreeam.leaf.config.modules.misc;

import org.apache.logging.log4j.Level;
import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class SentryDSN extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".sentry";
    }

    public static String sentryDsnConfigPath;
    public static String sentryDsn = "";
    public static String logLevel = "WARN";
    public static boolean onlyLogThrown = true;

    @Override
    public void onLoaded() {
        String sentryEnvironment = System.getenv("SENTRY_DSN");
        String sentryConfig = config.getString(sentryDsnConfigPath = getBasePath() + ".dsn", sentryDsn, config.pickStringRegionBased("""
                Sentry DSN for improved error logging, leave blank to disable,
                Obtain from https://sentry.io/""",
                """
                Sentry DSN (出现严重错误时将发送至配置的Sentry DSN地址) (留空关闭)"""));

        sentryDsn = sentryEnvironment == null
                ? sentryConfig
                : sentryEnvironment;
        logLevel = config.getString(getBasePath() + ".log-level", logLevel, config.pickStringRegionBased("""
                Logs with a level higher than or equal to this level will be recorded.""",
                """
                大于等于该等级的日志会被记录."""));
        onlyLogThrown = config.getBoolean(getBasePath() + ".only-log-thrown", onlyLogThrown, config.pickStringRegionBased("""
                Only log with a Throwable will be recorded after enabling this.""",
                """
                是否仅记录带有 Throwable 的日志."""));

        if (sentryDsn != null && !sentryDsn.isBlank()) {
            gg.pufferfish.pufferfish.sentry.SentryManager.init(Level.getLevel(logLevel));
        }
    }
}
