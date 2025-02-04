package org.dreeam.leaf.config.modules.async;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;
import org.dreeam.leaf.config.annotations.Experimental;

public class AsyncPlayerDataSave extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.ASYNC.getBaseKeyName() + ".async-playerdata-save";
    }

    @Experimental
    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(),
            """
                **Experimental feature, may have data lost in some circumstances!**
                Make PlayerData saving asynchronously.""",
            """
                **实验性功能, 在部分场景下可能丢失玩家数据!**
                异步保存玩家数据.""");

        enabled = config().getBoolean(getBasePath() + ".enabled", enabled);
    }
}
