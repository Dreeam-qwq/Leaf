package org.dreeam.leaf.config.modules.gameplay;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class DisableMovedWronglyThreshold extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.GAMEPLAY.getBaseKeyName() + ".player";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".disable-moved-wrongly-threshold", enabled,
            config.pickStringRegionBased(
                "Disable moved quickly/wrongly checks.",
                "关闭 moved wrongly/too quickly! 警告."
            ));
    }
}
