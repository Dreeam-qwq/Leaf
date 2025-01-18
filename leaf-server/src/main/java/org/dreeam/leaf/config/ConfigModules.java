package org.dreeam.leaf.config;

import org.dreeam.leaf.config.annotations.Experimental;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class ConfigModules extends LeafConfig {

    private static final Set<ConfigModules> modules = new HashSet<>();
    public LeafGlobalConfig config;

    public ConfigModules() {
        this.config = LeafConfig.config();
    }

    public static void initModules() throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        List<Class<?>> enabledExperimentalModules = new ArrayList<>();
        for (Class<?> clazz : LeafConfig.getClasses(LeafConfig.I_CONFIG_PKG)) {
            ConfigModules module = (ConfigModules) clazz.getConstructor().newInstance();
            module.onLoaded();

            modules.add(module);
            for (Field field : getAnnotatedStaticFields(clazz, Experimental.class)) {
                Object obj = field.get(null);
                if (!(obj instanceof Boolean)) continue;
                boolean enabled = (Boolean) obj;
                if (enabled) {
                    enabledExperimentalModules.add(clazz);
                    break;
                }
            }
        }
        if (!enabledExperimentalModules.isEmpty()) {
            LeafConfig.LOGGER.warn("You have following experimental module(s) enabled: {}, please report any bugs you found!", enabledExperimentalModules.stream().map(Class::getSimpleName).toList());
        }
    }

    private static List<Field> getAnnotatedStaticFields(Class<?> clazz, Class<? extends Annotation> annotation) {
        List<Field> fields = new ArrayList<>();
        for (Field field : clazz.getDeclaredFields()) {
            if (field.isAnnotationPresent(annotation) && Modifier.isStatic(field.getModifiers())) {
                field.setAccessible(true);
                fields.add(field);
            }
        }
        return fields;
    }

    public abstract void onLoaded();
}
