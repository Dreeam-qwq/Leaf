package org.dreeam.leaf.config.modules.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.dreeam.leaf.config.ConfigModules;
import org.dreeam.leaf.config.EnumConfigCategory;
import org.dreeam.leaf.config.LeafConfig;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HiddenItemComponents extends ConfigModules {

    public String getBasePath() {
        return EnumConfigCategory.MISC.getBaseKeyName();
    }

    public static List<DataComponentType<?>> hiddenItemComponentTypes = List.of();

    @Override
    public void onLoaded() {
        List<String> list = config.getList(getBasePath() + ".hidden-item-components", new ArrayList<>(), config.pickStringRegionBased("""
                Controls whether specified component information is sent to clients.
                This may break resource packs and mods that rely on this information.
                It needs a component type list, incorrect things will not work.
                You can fill it with ["custom_data"] to hide components of CUSTOM_DATA.
                Also, it can avoid some frequent client animations.
                NOTICE: You must know what you're filling in and how it works! It will handle all itemStacks!""",
                """
                控制哪些物品组件信息会被发送至客户端.
                可能会导致依赖物品组件的资源包/模组无法正常工作.
                该配置项接受一个物品组件列表, 格式不正确将不会启用.
                可以填入 ["custom_data"] 来隐藏自定义数据物品组件 CUSTOM_DATA.
                也可以避免一些客户端动画效果.
                注意: 你必须知道你填进去的是什么, 有什么用, 该项配置会处理所有的ItemStack!"""));

        List<DataComponentType<?>> types = new ArrayList<>(list.size());

        for (String id : list) {
            // Find and check
            Optional<Holder.Reference<DataComponentType<?>>> optional = BuiltInRegistries.DATA_COMPONENT_TYPE.get(ResourceLocation.parse(id));

            if (optional.isEmpty()) continue;

            DataComponentType<?> type = optional.get().value();

            if (type != null) {
                types.add(type);
            } else {
                LeafConfig.LOGGER.warn("Unknown component type: {}", id);
            }
        }

        hiddenItemComponentTypes = types;
    }

}
