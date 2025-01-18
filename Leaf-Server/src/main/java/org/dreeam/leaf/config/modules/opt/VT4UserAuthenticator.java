package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class VT4UserAuthenticator extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName();
    }

    public static boolean enabled = true;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".use-virtual-thread-for-user-authenticator", enabled,
                config.pickStringRegionBased(
                        "Use the new Virtual Thread introduced in JDK 21 for User Authenticator.",
                        "是否为用户验证器使用虚拟线程."));
    }
}
