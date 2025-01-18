package org.dreeam.leaf.config.modules.network;

import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;

import java.util.concurrent.ThreadLocalRandom;

public class ProtocolSupport extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.NETWORK.getBaseKeyName() + ".protocol-support";
    }

    public static boolean jadeProtocol = false;
    public static boolean appleskinProtocol = false;
    public static int appleskinSyncTickInterval = 20;
    public static boolean asteorBarProtocol = false;
    public static boolean chatImageProtocol = false;
    public static boolean xaeroMapProtocol = false;
    public static int xaeroMapServerID = ThreadLocalRandom.current().nextInt(); // Leaf - Faster Random
    public static boolean syncmaticaProtocol = false;
    public static boolean syncmaticaQuota = false;
    public static int syncmaticaQuotaLimit = 40000000;

    @Override
    public void onLoaded() {
        jadeProtocol = config.getBoolean(getBasePath() + ".jade-protocol", jadeProtocol);
        appleskinProtocol = config.getBoolean(getBasePath() + ".appleskin-protocol", appleskinProtocol);
        appleskinSyncTickInterval = config.getInt(getBasePath() + ".appleskin-protocol-sync-tick-interval", appleskinSyncTickInterval);
        asteorBarProtocol = config.getBoolean(getBasePath() + ".asteorbar-protocol", asteorBarProtocol);
        chatImageProtocol = config.getBoolean(getBasePath() + ".chatimage-protocol", chatImageProtocol);
        xaeroMapProtocol = config.getBoolean(getBasePath() + ".xaero-map-protocol", xaeroMapProtocol);
        xaeroMapServerID = config.getInt(getBasePath() + ".xaero-map-server-id", xaeroMapServerID);
        syncmaticaProtocol = config.getBoolean(getBasePath() + ".syncmatica-protocol", syncmaticaProtocol);
        syncmaticaQuota = config.getBoolean(getBasePath() + ".syncmatica-quota", syncmaticaQuota);
        syncmaticaQuotaLimit = config.getInt(getBasePath() + ".syncmatica-quota-limit", syncmaticaQuotaLimit);

        if (syncmaticaProtocol) {
            org.leavesmc.leaves.protocol.syncmatica.SyncmaticaProtocol.init();
        }
    }
}
