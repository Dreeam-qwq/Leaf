package org.dreeam.leaf.config.modules.misc;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class RemoveChangeNonEditableSignWarning extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName();
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".remove-change-non-editable-sign-warning", enabled,
                config.pickStringRegionBased(
                        "Enable to prevent console spam.",
                        "移除修改无法编辑的告示牌时输出的警告."
                ));
    }
}
