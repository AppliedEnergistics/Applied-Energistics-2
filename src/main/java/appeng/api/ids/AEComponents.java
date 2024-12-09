package appeng.api.ids;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.google.common.collect.Maps;
import com.mojang.serialization.Codec;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.registries.DeferredRegister;

import appeng.api.components.ExportedUpgrades;
import appeng.api.config.FuzzyMode;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.util.AEColor;
import appeng.block.crafting.PushDirection;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedCraftingPattern;
import appeng.crafting.pattern.EncodedProcessingPattern;
import appeng.crafting.pattern.EncodedSmithingTablePattern;
import appeng.crafting.pattern.EncodedStonecuttingPattern;
import appeng.items.storage.SpatialPlotInfo;

public final class AEComponents {
    @ApiStatus.Internal
    public static final DeferredRegister<DataComponentType<?>> DR = DeferredRegister
            .create(Registries.DATA_COMPONENT_TYPE, AppEng.MOD_ID);

    private AEComponents() {
    }

    /**
     * The name of the machine type the settings were exported from.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<Component> EXPORTED_SETTINGS_SOURCE = register("exported_settings_source",
            builder -> builder.persistent(ComponentSerialization.CODEC)
                    .networkSynchronized(ComponentSerialization.STREAM_CODEC));

    /**
     * An export custom machine name.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<Component> EXPORTED_CUSTOM_NAME = register("exported_custom_name",
            builder -> builder.persistent(ComponentSerialization.CODEC)
                    .networkSynchronized(ComponentSerialization.STREAM_CODEC));

    /**
     * Exported machine upgrades.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<ExportedUpgrades> EXPORTED_UPGRADES = register("exported_upgrades",
            builder -> builder.persistent(ExportedUpgrades.CODEC).networkSynchronized(ExportedUpgrades.STREAM_CODEC));

    /**
     * Exported machine configuration.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<Map<String, String>> EXPORTED_SETTINGS = register("exported_settings",
            builder -> builder.persistent(Codec.unboundedMap(Codec.STRING, Codec.STRING))
                    .networkSynchronized(ByteBufCodecs.map(
                            Maps::newHashMapWithExpectedSize,
                            ByteBufCodecs.STRING_UTF8,
                            ByteBufCodecs.STRING_UTF8)));

    /**
     * Exported machine priority.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<Integer> EXPORTED_PRIORITY = register("exported_priority",
            builder -> builder.persistent(Codec.INT).networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * Exported subtype of a {@link appeng.parts.p2p.P2PTunnelPart}.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<Item> EXPORTED_P2P_TYPE = register("exported_p2p_type",
            builder -> builder.persistent(BuiltInRegistries.ITEM.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.ITEM)));

    /**
     * Exported {@link appeng.parts.p2p.P2PTunnelPart} frequency.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<Short> EXPORTED_P2P_FREQUENCY = register("exported_p2p_frequency",
            builder -> builder.persistent(Codec.SHORT).networkSynchronized(ByteBufCodecs.SHORT));

    /**
     * Specifies a color code consisting of 8 colors to display on the memory card item.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<MemoryCardColors> MEMORY_CARD_COLORS = register("memory_card_colors",
            builder -> builder.persistent(MemoryCardColors.CODEC).networkSynchronized(MemoryCardColors.STREAM_CODEC));

    /**
     * Exported configuration inventory.
     *
     * @see appeng.items.tools.MemoryCardItem
     */
    public static final DataComponentType<List<GenericStack>> EXPORTED_CONFIG_INV = register("exported_config_inv",
            builder -> builder.persistent(GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC)
                    .networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));

    /**
     * Exported reporting value for level emitters.
     */
    public static final DataComponentType<Long> EXPORTED_LEVEL_EMITTER_VALUE = register("exported_level_emitter_value",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    /**
     * Exported patterns
     *
     * @see appeng.items.tools.MemoryCardItem
     * @see appeng.helpers.patternprovider.PatternProviderLogic
     */
    public static final DataComponentType<ItemContainerContents> EXPORTED_PATTERNS = register("exported_patterns",
            builder -> builder.persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    /**
     * Exported push direction of pattern providers or similar machines.
     */
    public static final DataComponentType<PushDirection> EXPORTED_PUSH_DIRECTION = register("exported_push_direction",
            builder -> builder.persistent(PushDirection.CODEC).networkSynchronized(PushDirection.STREAM_CODEC));

    /**
     * The name inscribed by a {@link appeng.items.materials.NamePressItem}
     */
    public static final DataComponentType<Component> NAME_PRESS_NAME = register("name_press_name",
            builder -> builder.persistent(ComponentSerialization.CODEC)
                    .networkSynchronized(ComponentSerialization.TRUSTED_STREAM_CODEC));

    /**
     * An upgrade inventory.
     */
    public static final DataComponentType<ItemContainerContents> UPGRADES = register("upgrades",
            builder -> builder.persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    /**
     * The unique ID of a pair of {@link appeng.core.definitions.AEItems#QUANTUM_ENTANGLED_SINGULARITY}.
     */
    public static final DataComponentType<Long> ENTANGLED_SINGULARITY_ID = register("entangled_singularity_id",
            builder -> builder.persistent(Codec.LONG).networkSynchronized(ByteBufCodecs.VAR_LONG));

    /**
     * Currently stored energy in AE in this item. Usually the capacity will be set by the item, but some items allow it
     * to be overridden by {@link AEComponents#ENERGY_CAPACITY}.
     */
    public static final DataComponentType<Double> STORED_ENERGY = register("stored_energy",
            builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    /**
     * The maximum amount of energy that can be stored in this item.
     */
    public static final DataComponentType<Double> ENERGY_CAPACITY = register("energy_capacity",
            builder -> builder.persistent(Codec.DOUBLE).networkSynchronized(ByteBufCodecs.DOUBLE));

    /**
     * An encoded crafting pattern.
     *
     * @see AEItems#CRAFTING_PATTERN
     */
    public static final DataComponentType<EncodedCraftingPattern> ENCODED_CRAFTING_PATTERN = register(
            "encoded_crafting_pattern",
            builder -> builder.persistent(EncodedCraftingPattern.CODEC)
                    .networkSynchronized(EncodedCraftingPattern.STREAM_CODEC));

    /**
     * An encoded processing pattern.
     *
     * @see AEItems#PROCESSING_PATTERN
     */
    public static final DataComponentType<EncodedProcessingPattern> ENCODED_PROCESSING_PATTERN = register(
            "encoded_processing_pattern",
            builder -> builder.persistent(EncodedProcessingPattern.CODEC)
                    .networkSynchronized(EncodedProcessingPattern.STREAM_CODEC));

    /**
     * An encoded stonecutting pattern.
     *
     * @see AEItems#STONECUTTING_PATTERN
     */
    public static final DataComponentType<EncodedStonecuttingPattern> ENCODED_STONECUTTING_PATTERN = register(
            "encoded_stonecutting_pattern",
            builder -> builder.persistent(EncodedStonecuttingPattern.CODEC)
                    .networkSynchronized(EncodedStonecuttingPattern.STREAM_CODEC));

    /**
     * An encoded smithing table pattern.
     *
     * @see AEItems#SMITHING_TABLE_PATTERN
     */
    public static final DataComponentType<EncodedSmithingTablePattern> ENCODED_SMITHING_TABLE_PATTERN = register(
            "encoded_smithing_table_pattern",
            builder -> builder.persistent(EncodedSmithingTablePattern.CODEC)
                    .networkSynchronized(EncodedSmithingTablePattern.STREAM_CODEC));

    /**
     * List of AE key types enabled in a terminal
     */
    public static final DataComponentType<List<AEKeyType>> ENABLED_KEY_TYPES = register("enabled_key_types",
            builder -> builder.persistent(AEKeyType.CODEC.listOf())
                    .networkSynchronized(AEKeyType.STREAM_CODEC.apply(ByteBufCodecs.list())));

    /**
     * Encodes the link between a wireless item and a wireless access point.
     */
    public static final DataComponentType<GlobalPos> WIRELESS_LINK_TARGET = register("wireless_link_target",
            builder -> builder.persistent(GlobalPos.CODEC).networkSynchronized(GlobalPos.STREAM_CODEC));

    /**
     * Which paint item is currently selected in a color applicator.
     */
    public static final DataComponentType<AEColor> SELECTED_COLOR = register("selected_color",
            builder -> builder.persistent(AEColor.CODEC).networkSynchronized(AEColor.STREAM_CODEC));

    /**
     * Defines the fuzzy mode for a storage cell.
     */
    public static final DataComponentType<FuzzyMode> STORAGE_CELL_FUZZY_MODE = register("storage_cell_fuzzy_mode",
            builder -> builder.persistent(FuzzyMode.CODEC).networkSynchronized(FuzzyMode.STREAM_CODEC));

    /**
     * Content of a storage cell.
     */
    public static final DataComponentType<List<GenericStack>> STORAGE_CELL_INV = register("storage_cell_inv",
            builder -> builder.persistent(GenericStack.FAULT_TOLERANT_LIST_CODEC)
                    .networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));

    /**
     * Defines partitioning for a storage cell.
     */
    public static final DataComponentType<List<GenericStack>> STORAGE_CELL_CONFIG_INV = register(
            "storage_cell_config_inv",
            builder -> builder.persistent(GenericStack.FAULT_TOLERANT_NULLABLE_LIST_CODEC)
                    .networkSynchronized(GenericStack.STREAM_CODEC.apply(ByteBufCodecs.list())));

    /**
     * The item a facade is masquerading as.
     */
    public static final DataComponentType<Holder<Item>> FACADE_ITEM = register("facade_item",
            builder -> builder.persistent(BuiltInRegistries.ITEM.holderByNameCodec())
                    .networkSynchronized(ByteBufCodecs.holderRegistry(Registries.ITEM)));

    /**
     * Which property of a facade blockstate the wrench is currently cycling through.
     */
    public static final DataComponentType<String> FACADE_CYCLE_PROPERTY = register("facade_cycle_property",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /**
     * The generic stack wrapped in a {@link AEItems#WRAPPED_GENERIC_STACK}
     */
    public static final DataComponentType<GenericStack> WRAPPED_STACK = register("wrapped_stack",
            builder -> builder.persistent(GenericStack.CODEC).networkSynchronized(GenericStack.STREAM_CODEC));

    /**
     * A crafting inventory.
     */
    public static final DataComponentType<ItemContainerContents> CRAFTING_INV = register("crafting_inv",
            builder -> builder.persistent(ItemContainerContents.CODEC)
                    .networkSynchronized(ItemContainerContents.STREAM_CODEC));

    public static final DataComponentType<SpatialPlotInfo> SPATIAL_PLOT_INFO = register("spatial_plot_info",
            builder -> builder.persistent(SpatialPlotInfo.CODEC).networkSynchronized(SpatialPlotInfo.STREAM_CODEC));

    public static final DataComponentType<CustomData> MISSING_CONTENT_ITEMSTACK_DATA = register(
            "missing_content_itemstack_data",
            builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));

    public static final DataComponentType<CustomData> MISSING_CONTENT_AEKEY_DATA = register(
            "missing_content_aekey_data",
            builder -> builder.persistent(CustomData.CODEC).networkSynchronized(CustomData.STREAM_CODEC));

    public static final DataComponentType<String> MISSING_CONTENT_ERROR = register("missing_content_error",
            builder -> builder.persistent(Codec.STRING).networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private static <T> DataComponentType<T> register(String name, Consumer<DataComponentType.Builder<T>> customizer) {
        var builder = DataComponentType.<T>builder();
        customizer.accept(builder);
        var componentType = builder.build();
        DR.register(name, () -> componentType);
        return componentType;
    }
}
