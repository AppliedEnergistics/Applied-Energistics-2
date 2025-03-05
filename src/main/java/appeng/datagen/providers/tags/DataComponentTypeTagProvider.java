package appeng.datagen.providers.tags;

import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;


import appeng.api.ids.AEComponents;
import appeng.core.AppEng;
import appeng.datagen.providers.localization.LocalizationProvider;
import appeng.items.tools.MemoryCardItem;

public class DataComponentTypeTagProvider extends TagsProvider<DataComponentType<?>> {
    private final LocalizationProvider localization;

    public DataComponentTypeTagProvider(PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries,
            LocalizationProvider localization) {
        super(output, Registries.DATA_COMPONENT_TYPE, registries, AppEng.MOD_ID);
        this.localization = localization;
    }

    private final HashSet<DataComponentType<?>> translated = new HashSet<>();

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        Map<DataComponentType<?>, ResourceKey<DataComponentType<?>>> componentKeys = new IdentityHashMap<>();
        for (var entry : AEComponents.DR.getEntries()) {
            componentKeys.put(entry.get(), entry.getKey());
        }

        addExportedComponentCategory("Filter", AEComponents.EXPORTED_CONFIG_INV);
        addExportedComponentCategory("Patterns", AEComponents.EXPORTED_PATTERNS);
        addExportedComponentCategory("Custom Name", AEComponents.EXPORTED_CUSTOM_NAME);
        addExportedComponentCategory("Level Emitter Value", AEComponents.EXPORTED_LEVEL_EMITTER_VALUE);
        addExportedComponentCategory("P2P Frequency", AEComponents.EXPORTED_P2P_FREQUENCY);
        addExportedComponentCategory("P2P Type", AEComponents.EXPORTED_P2P_TYPE);
        addExportedComponentCategory("Priority", AEComponents.EXPORTED_PRIORITY);
        addExportedComponentCategory("Push Direction", AEComponents.EXPORTED_PUSH_DIRECTION);
        addExportedComponentCategory("Settings", AEComponents.EXPORTED_SETTINGS);
        addExportedComponentCategory("Upgrades", AEComponents.EXPORTED_UPGRADES);

        tag(ConventionTags.EXPORTED_SETTINGS).add(
                BuiltInRegistries.DATA_COMPONENT_TYPE.wrapAsHolder(AEComponents.EXPORTED_SETTINGS_SOURCE).getKey());
    }

    private void addExportedComponentCategory(String englishCategoryName, DataComponentType<?>... types) {
        for (var type : types) {
            translated.add(type);
            var key = BuiltInRegistries.DATA_COMPONENT_TYPE.getResourceKey(type).get();
            tag(ConventionTags.EXPORTED_SETTINGS).add(key);

            localization.add(MemoryCardItem.getSettingTranslationKey(type), englishCategoryName);
        }
    }
}
