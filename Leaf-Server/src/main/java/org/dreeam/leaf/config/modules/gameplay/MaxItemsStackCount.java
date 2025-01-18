package org.dreeam.leaf.config.modules.gameplay;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class MaxItemsStackCount extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.GAMEPLAY.getBaseKeyName() + ".max-item-stack-count";
    }

    public static int maxItemStackCount = 0;
    public static int maxContainerDestroyCount = 0;

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(),
                "Don't touch this unless you know what you are doing!",
                "不要动该项, 除非你知道自己在做什么!");

        maxItemStackCount = config.getInt(getBasePath() + ".max-dropped-items-stack-count", maxItemStackCount);
        maxContainerDestroyCount = config.getInt(getBasePath() + ".max-container-destroy-count", maxContainerDestroyCount);

        if (maxItemStackCount < 0) maxItemStackCount = 0;
        if (maxContainerDestroyCount < 0) maxContainerDestroyCount = 0;
    }
}
