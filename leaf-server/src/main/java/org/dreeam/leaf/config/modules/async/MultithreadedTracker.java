package org.dreeam.leaf.config.modules.async;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;
import org.dreeam.leaf.config.LeafConfig;

public class MultithreadedTracker extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".async-entity-tracker";
    }

    public static boolean enabled = false;
    public static boolean compatModeEnabled = false;
    public static boolean autoResize = false;
    public static int asyncEntityTrackerMaxThreads = 0;
    public static int asyncEntityTrackerKeepalive = 60;

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(), """
                Make entity tracking saving asynchronously, can improve performance significantly,
                especially in some massive entities in small area situations.""",
            """
                异步实体跟踪,
                在实体数量多且密集的情况下效果明显.""");

        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);
        compatModeEnabled = config.getBoolean(getBasePath() + ".compat-mode", compatModeEnabled, config.pickStringRegionBased("""
                Enable compat mode ONLY if Citizens or NPC plugins using real entity has installed,
                Compat mode fixed visible issue with player type NPCs of Citizens,
                But still recommend to use packet based / virtual entity NPC plugin, e.g. ZNPC Plus, Adyeshach, Fancy NPC or else.""",
            """
                是否启用兼容模式,
                如果你的服务器安装了 Citizens 或其他类似非发包 NPC 插件, 请开启此项."""));
        autoResize = config.getBoolean(getBasePath() + ".auto-resize", autoResize, config.pickStringRegionBased("""
                Auto adjust thread pool size based on server load,
                This will tweak thread pool size dynamically,
                overrides max-threads and keepalive.""",
            """
                根据服务器负载自动调整线程池大小,
                这会使线程池大小动态调整,
                覆盖设置 max-threads 和 keepalive."""));
        asyncEntityTrackerMaxThreads = config.getInt(getBasePath() + ".max-threads", asyncEntityTrackerMaxThreads);
        asyncEntityTrackerKeepalive = config.getInt(getBasePath() + ".keepalive", asyncEntityTrackerKeepalive);

        if (asyncEntityTrackerMaxThreads < 0)
            asyncEntityTrackerMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() + asyncEntityTrackerMaxThreads, 1);
        else if (asyncEntityTrackerMaxThreads == 0)
            asyncEntityTrackerMaxThreads = Math.max(Runtime.getRuntime().availableProcessors() / 4, 1);

        if (!enabled)
            asyncEntityTrackerMaxThreads = 0;
        else
            LeafConfig.LOGGER.info("Using {} threads for Async Entity Tracker", asyncEntityTrackerMaxThreads);
    }
}
