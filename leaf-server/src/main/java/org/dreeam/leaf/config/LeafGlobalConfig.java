package org.dreeam.leaf.config;

import io.github.thatsmusic99.configurationmaster.api.ConfigFile;
import io.github.thatsmusic99.configurationmaster.api.ConfigSection;

import java.io.File;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeafGlobalConfig {

    protected static ConfigFile configFile;
    private static final String CURRENT_REGION = Locale.getDefault().getCountry().toUpperCase(Locale.ROOT); // It will be in uppercase by default, just make sure
    protected static final boolean isCN = CURRENT_REGION.equals("CN");

    public LeafGlobalConfig(boolean init) throws Exception {
        configFile = ConfigFile.loadConfig(new File(LeafConfig.I_CONFIG_FOLDER, LeafConfig.I_GLOBAL_CONFIG_FILE));
        configFile.set("config-version", 3.0);
        configFile.addComments("config-version", pickStringRegionBased("""
                Leaf Config
                GitHub Repo: https://github.com/Winds-Studio/Leaf
                Discord: https://discord.com/invite/gfgAwdSEuM""",
            """
                Leaf Config
                GitHub Repo: https://github.com/Winds-Studio/Leaf
                QQ Group: 619278377"""));

        // Pre-structure to force order
        structureConfig();
    }

    protected void structureConfig() {
        for (EnumConfigCategory configCate : EnumConfigCategory.getCategoryValues()) {
            createTitledSection(configCate.name(), configCate.getBaseKeyName());
        }
    }

    public void saveConfig() throws Exception {
        configFile.save();
    }

    // Config Utilities

    public void createTitledSection(String title, String path) {
        configFile.addSection(title);
        configFile.addDefault(path, null);
    }

    public boolean getBoolean(String path, boolean def, String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getBoolean(path, def);
    }

    public boolean getBoolean(String path, boolean def) {
        configFile.addDefault(path, def);
        return configFile.getBoolean(path, def);
    }

    public String getString(String path, String def, String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getString(path, def);
    }

    public String getString(String path, String def) {
        configFile.addDefault(path, def);
        return configFile.getString(path, def);
    }

    public double getDouble(String path, double def, String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getDouble(path, def);
    }

    public double getDouble(String path, double def) {
        configFile.addDefault(path, def);
        return configFile.getDouble(path, def);
    }

    public int getInt(String path, int def, String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getInteger(path, def);
    }

    public int getInt(String path, int def) {
        configFile.addDefault(path, def);
        return configFile.getInteger(path, def);
    }

    public List<String> getList(String path, List<String> def, String comment) {
        configFile.addDefault(path, def, comment);
        return configFile.getStringList(path);
    }

    public List<String> getList(String path, List<String> def) {
        configFile.addDefault(path, def);
        return configFile.getStringList(path);
    }

    public ConfigSection getConfigSection(String path, Map<String, Object> defaultKeyValue) {
        configFile.addDefault(path, null);
        configFile.makeSectionLenient(path);
        defaultKeyValue.forEach((string, object) -> configFile.addExample(path + "." + string, object));
        return configFile.getConfigSection(path);
    }

    public ConfigSection getConfigSection(String path, Map<String, Object> defaultKeyValue, String comment) {
        configFile.addDefault(path, null, comment);
        configFile.makeSectionLenient(path);
        defaultKeyValue.forEach((string, object) -> configFile.addExample(path + "." + string, object));
        return configFile.getConfigSection(path);
    }

    public void addComment(String path, String comment) {
        configFile.addComment(path, comment);
    }

    public void addCommentIfCN(String path, String comment) {
        if (isCN) {
            configFile.addComment(path, comment);
        }
    }

    public void addCommentIfNonCN(String path, String comment) {
        if (!isCN) {
            configFile.addComment(path, comment);
        }
    }

    public void addCommentRegionBased(String path, String en, String cn) {
        configFile.addComment(path, isCN ? cn : en);
    }

    public String pickStringRegionBased(String en, String cn) {
        return isCN ? cn : en;
    }
}
