package org.dreeam.leaf.config.modules.fixes;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

public class DontPlacePlayerIfFull extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.FIXES.getBaseKeyName();
    }

    public static boolean enabled = false;

    @Override
    public void onLoaded() {
        enabled = config.getBoolean(getBasePath() + ".dont-place-player-if-server-full", enabled, config.pickStringRegionBased("""
                Don't let player join server if the server is full.
                If enable this, you should use 'purpur.joinfullserver' permission instead of
                PlayerLoginEvent#allow to let player join full server.""",
                """
                服务器已满时禁止玩家加入.
                开启后需使用权限 'purpur.joinfullserver' 而不是
                PlayerLoginEvent#allow 让玩家进入已满的服务器."""));
    }
}
