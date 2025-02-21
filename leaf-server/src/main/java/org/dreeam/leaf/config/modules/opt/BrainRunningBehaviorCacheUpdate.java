package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class BrainRunningBehaviorCacheUpdate extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName();
    }

    public static int interval = 5;

    @Override
    public void onLoaded() {
        interval = config.getInt(getBasePath() + ".entity-running-behavior-cache-update-interval", interval,
            config.pickStringRegionBased(
                "How often entity update current brain running behavior list.",
                "生物更新现有 Brain Behavior 列表缓存的间隔."));
    }
}
