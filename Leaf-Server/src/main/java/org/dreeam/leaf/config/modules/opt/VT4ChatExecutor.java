package org.dreeam.leaf.config.modules.opt;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class VT4ChatExecutor extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.PERF.getBaseKeyName();
    }

    public static boolean enabled = true;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".use-virtual-thread-for-async-chat-executor", enabled,
                config.pickStringRegionBased(
                        "Use the new Virtual Thread introduced in JDK 21 for Async Chat Executor.",
                        "是否为异步聊天线程使用虚拟线程."));
    }
}
