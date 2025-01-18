package org.dreeam.leaf.config.modules.async;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;
import org.dreeam.leaf.config.LeafConfig;

public class AsyncPathfinding extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".async-pathfinding";
    }

    public static boolean enabled = false;
    public static int asyncPathfindingMaxThreads = 0;
    public static int asyncPathfindingKeepalive = 60;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);
        asyncPathfindingMaxThreads = config.getInt(getBasePath() + ".max-threads", asyncPathfindingMaxThreads);
        asyncPathfindingKeepalive = config.getInt(getBasePath() + ".keepalive", asyncPathfindingKeepalive);

        if (asyncPathfindingMaxThreads < 0)
            asyncPathfindingMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() + asyncPathfindingMaxThreads, 1);
        else if (asyncPathfindingMaxThreads == 0)
            asyncPathfindingMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);
        if (!enabled)
            asyncPathfindingMaxThreads = 0;
        else
            LeafConfig.LOGGER.info("Using {} threads for Async Pathfinding", asyncPathfindingMaxThreads);
    }
}
