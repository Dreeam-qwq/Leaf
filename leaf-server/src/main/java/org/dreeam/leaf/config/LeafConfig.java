package org.dreeam.leaf.config;

import io.papermc.paper.configuration.GlobalConfiguration;
import org.dreeam.leaf.config.modules.misc.SentryDSN;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;

/*
 *  Yoinked from: https://github.com/xGinko/AnarchyExploitFixes/ & https://github.com/LuminolMC/Luminol
 *  @author: @xGinko & @MrHua269
 */
public class LeafConfig {

    public static final Logger LOGGER = LogManager.getLogger(LeafConfig.class.getSimpleName());
    protected static final File I_CONFIG_FOLDER = new File("config");
    protected static final String I_CONFIG_PKG = "org.dreeam.leaf.config.modules";
    protected static final String I_GLOBAL_CONFIG_FILE = "leaf-global.yml";
    protected static final String I_LEVEL_CONFIG_FILE = "leaf-world-defaults.yml"; // Leaf TODO - Per level config

    private static LeafGlobalConfig leafGlobalConfig;

    /* Load & Reload */

    public static void reload() {
        try {
            long begin = System.nanoTime();
            LOGGER.info("Reloading config...");

            loadConfig(false);

            LOGGER.info("Successfully reloaded config in {}ms.", (System.nanoTime() - begin) / 1_000_000);
        } catch (Exception e) {
            LOGGER.error("Failed to reload config.", e);
        }
    }

    @Contract(" -> new")
    public static @NotNull CompletableFuture<Void> reloadAsync() {
        return new CompletableFuture<>();
    }

    public static void loadConfig() {
        try {
            long begin = System.nanoTime();
            LOGGER.info("Loading config...");

            purgeOutdated();
            loadConfig(true);

            LOGGER.info("Successfully loaded config in {}ms.", (System.nanoTime() - begin) / 1_000_000);
        } catch (Exception e) {
            LeafConfig.LOGGER.error("Failed to load config modules!", e);
        }
    }

    /* Load Global Config */

    private static void loadConfig(boolean init) throws Exception {
        // Create config folder
        createDirectory(LeafConfig.I_CONFIG_FOLDER);

        leafGlobalConfig = new LeafGlobalConfig(init);

        // Load config modules
        ConfigModules.initModules();

        // Save config to disk
        leafGlobalConfig.saveConfig();
    }

    public static LeafGlobalConfig config() {
        return leafGlobalConfig;
    }

    /* Create config folder */

    protected static void createDirectory(File dir) throws IOException {
        try {
            Files.createDirectories(dir.toPath());
        } catch (FileAlreadyExistsException e) { // Thrown if dir exists but is not a directory
            if (dir.delete()) createDirectory(dir);
        }
    }

    /* Scan classes under package */

    public static @NotNull Set<Class<?>> getClasses(String pack) {
        Set<Class<?>> classes = new LinkedHashSet<>();
        String packageDirName = pack.replace('.', '/');
        Enumeration<URL> dirs;

        try {
            dirs = Thread.currentThread().getContextClassLoader().getResources(packageDirName);
            while (dirs.hasMoreElements()) {
                URL url = dirs.nextElement();
                String protocol = url.getProtocol();
                if ("file".equals(protocol)) {
                    String filePath = URLDecoder.decode(url.getFile(), StandardCharsets.UTF_8);
                    findClassesInPackageByFile(pack, filePath, classes);
                } else if ("jar".equals(protocol)) {
                    JarFile jar;
                    try {
                        jar = ((JarURLConnection) url.openConnection()).getJarFile();
                        Enumeration<JarEntry> entries = jar.entries();
                        findClassesInPackageByJar(pack, entries, packageDirName, classes);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return classes;
    }

    private static void findClassesInPackageByFile(String packageName, String packagePath, Set<Class<?>> classes) {
        File dir = new File(packagePath);

        if (!dir.exists() || !dir.isDirectory()) {
            return;
        }

        File[] dirfiles = dir.listFiles((file) -> file.isDirectory() || file.getName().endsWith(".class"));
        if (dirfiles != null) {
            for (File file : dirfiles) {
                if (file.isDirectory()) {
                    findClassesInPackageByFile(packageName + "." + file.getName(), file.getAbsolutePath(), classes);
                } else {
                    String className = file.getName().substring(0, file.getName().length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private static void findClassesInPackageByJar(String packageName, Enumeration<JarEntry> entries, String packageDirName, Set<Class<?>> classes) {
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            String name = entry.getName();

            if (name.charAt(0) == '/') {
                name = name.substring(1);
            }

            if (name.startsWith(packageDirName)) {
                int idx = name.lastIndexOf('/');

                if (idx != -1) {
                    packageName = name.substring(0, idx).replace('/', '.');
                }

                if (name.endsWith(".class") && !entry.isDirectory()) {
                    String className = name.substring(packageName.length() + 1, name.length() - 6);
                    try {
                        classes.add(Class.forName(packageName + '.' + className));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    /* Register Spark profiler extra server configurations */

    private static List<String> buildSparkExtraConfigs() {
        List<String> extraConfigs = new ArrayList<>(Arrays.asList(
                "config/leaf-global.yml",
                "config/gale-global.yml",
                "config/gale-world-defaults.yml"
        ));

        for (World world : Bukkit.getWorlds()) {
            extraConfigs.add(world.getWorldFolder().getName() + "/gale-world.yml"); // Gale world config
        }

        return extraConfigs;
    }

    private static String[] buildSparkHiddenPaths() {
        return new String[]{
                SentryDSN.sentryDsnConfigPath // Hide Sentry DSN key
        };
    }

    public static void regSparkExtraConfig() {
        if (GlobalConfiguration.get().spark.enabled || Bukkit.getServer().getPluginManager().getPlugin("spark") != null) {
            String extraConfigs = String.join(",", buildSparkExtraConfigs());
            String hiddenPaths = String.join(",", buildSparkHiddenPaths());

            System.setProperty("spark.serverconfigs.extra", extraConfigs);
            System.setProperty("spark.serverconfigs.hiddenpaths", hiddenPaths);
        }
    }

    /* Purge and backup old Leaf config & Pufferfish config */

    private static void purgeOutdated() {
        boolean foundLegacy = false;
        String pufferfishConfig = "pufferfish.yml";
        String leafConfigV1 = "leaf.yml";
        String leafConfigV2 = "leaf_config";

        Date date = new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyMMddhhmmss");
        String backupDir = "config/backup" + dateFormat.format(date) + "/";

        File pufferfishConfigFile = new File(pufferfishConfig);
        File leafConfigV1File = new File(leafConfigV1);
        File leafConfigV2File = new File(leafConfigV2);
        File backupDirFile = new File(backupDir);

        try {
            if (pufferfishConfigFile.exists() && pufferfishConfigFile.isFile()) {
                createDirectory(backupDirFile);
                Files.move(pufferfishConfigFile.toPath(), Path.of(backupDir + pufferfishConfig), StandardCopyOption.REPLACE_EXISTING);
                foundLegacy = true;
            }
            if (leafConfigV1File.exists() && leafConfigV1File.isFile()) {
                createDirectory(backupDirFile);
                Files.move(leafConfigV1File.toPath(), Path.of(backupDir + leafConfigV1), StandardCopyOption.REPLACE_EXISTING);
                foundLegacy = true;
            }
            if (leafConfigV2File.exists() && leafConfigV2File.isDirectory()) {
                createDirectory(backupDirFile);
                Files.move(leafConfigV2File.toPath(), Path.of(backupDir + leafConfigV2), StandardCopyOption.REPLACE_EXISTING);
                foundLegacy = true;
            }

            if (foundLegacy) {
                LOGGER.warn("Found legacy Leaf config files, move to backup directory: {}", backupDir);
                LOGGER.warn("New Leaf config located at config/ folder, You need to transfer config to the new one manually and restart the server!");
            }
        } catch (IOException e) {
            LOGGER.error("Failed to purge old configs.", e);
        }
    }
}
