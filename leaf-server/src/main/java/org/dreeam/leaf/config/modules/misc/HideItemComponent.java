package org.dreeam.leaf.config.modules.misc;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;
import org.dreeam.leaf.config.LeafConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * HideItemComponent
 *
 * @author TheFloodDragon
 * @since 2025/2/4 18:30
 */
public class HideItemComponent extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName() + ".hide-item-component";
    }

    public static boolean enabled = false;
    public static List<DataComponentType<?>> hiddenTypes = List.of();

    @Override
    public void onLoaded() {
        config.addCommentRegionBased(getBasePath(), """
            Controls whether specified component information would be sent to clients.
            It may break resource packs and mods that rely on the information.
            Also, it can avoid some frequent client animations.
            Attention: This is not same as Paper does, we only hide specified component information from player's inventory.
            """, """
            控制哪些物品组件信息会被发送至客户端.
            可能会导致依赖物品组件的资源包/模组无法正常工作.
            可以避免一些客户端动画效果.
            注意: 此项与 Paper 的 item-obfuscation 不同, 我们只从玩家的库存中隐藏指定的组件信息.
            """);
        List<String> list = config.getList(getBasePath() + ".hidden-types", List.of("custom_data"), config.pickStringRegionBased("""
            Which type of components will be hidden from clients.
            It needs a component type list, incorrect things will not work.
            """, "被隐藏的物品组件类型列表."));
        enabled = config.getBoolean(getBasePath() + ".enabled", enabled, config.pickStringRegionBased(
            "If enables, specified component information from player's inventory will be hid.",
            "启用后, 玩家背包内的指定物品组件信息会被隐藏."
        ));

        final List<DataComponentType<?>> types = new ArrayList<>(list.size());
        for (String name : list) {
            BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(name)).ifPresentOrElse(
                optional -> types.add(optional.value()),
                () -> LeafConfig.LOGGER.warn("Unknown component type: {}", name)
            );
        }
        hiddenTypes = types;
    }

}