package org.dreeam.leaf.config.modules.misc;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class UnknownCommandMessage extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".message";
    }

    public static String unknownCommandMessage = "<red><lang:command.unknown.command><newline><detail>";

    @Override
    public void onLoaded() {
        unknownCommandMessage = config.getString(getBasePath() + ".unknown-command", unknownCommandMessage, config.pickStringRegionBased("""
                Unknown command message, using MiniMessage format, set to "default" to use vanilla message,
                placeholder: <detail>, shows detail of the unknown command information.""",
                """
                发送未知命令时的消息, 使用 MiniMessage 格式, 设置为 "default" 使用原版消息.
                变量: <detail>, 显示未知命令详细信息."""));
    }
}
