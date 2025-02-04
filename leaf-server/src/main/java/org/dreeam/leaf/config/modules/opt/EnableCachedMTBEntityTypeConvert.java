package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class EnableCachedMTBEntityTypeConvert extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName();
    }

    public static boolean enabled = true;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".enable-cached-minecraft-to-bukkit-entitytype-convert", enabled, config.pickStringRegionBased("""
                Whether to cache expensive CraftEntityType#minecraftToBukkit call.""",
            """
                是否缓存Minecraft到Bukkit的实体类型转换."""));
    }
}
