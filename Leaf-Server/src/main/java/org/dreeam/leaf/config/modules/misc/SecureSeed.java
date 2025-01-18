package org.dreeam.leaf.config.modules.misc;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class SecureSeed extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".secure-seed";
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(), """
                Once you enable secure seed, all ores and structures are generated with 1024-bit seed
                instead of using 64-bit seed in vanilla, made seed cracker become impossible.""",
                """
                安全种子开启后, 所有矿物与结构都将使用1024位的种子进行生成, 无法被破解.""");

        enabled = config.getBoolean(getBasePath() + ".enabled", enabled);
    }
}
