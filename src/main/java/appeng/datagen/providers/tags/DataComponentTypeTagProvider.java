package appeng.datagen.providers.tags;

import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.resources.ResourceKey;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import appeng.api.ids.AEComponents;
import appeng.core.AppEng;

public class DataComponentTypeTagProvider extends TagsProvider<DataComponentType<?>> {
    public DataComponentTypeTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries,
            @Nullable ExistingFileHelper existingFileHelper) {
        super(output, Registries.DATA_COMPONENT_TYPE, registries, AppEng.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {

        Map<DataComponentType<?>, ResourceKey<DataComponentType<?>>> componentKeys = new IdentityHashMap<>();
        for (var entry : AEComponents.DR.getEntries()) {
            componentKeys.put(entry.get(), entry.getKey());
        }

        var exportedComponents = List.of(
                AEComponents.EXPORTED_CONFIG_INV,
                AEComponents.EXPORTED_CUSTOM_NAME,
                AEComponents.EXPORTED_LEVEL_EMITTER_VALUE,
                AEComponents.EXPORTED_P2P_FREQUENCY,
                AEComponents.EXPORTED_P2P_TYPE,
                AEComponents.EXPORTED_PATTERNS,
                AEComponents.EXPORTED_PRIORITY,
                AEComponents.EXPORTED_PUSH_DIRECTION,
                AEComponents.EXPORTED_SETTINGS,
                AEComponents.EXPORTED_SETTINGS_SOURCE,
                AEComponents.EXPORTED_UPGRADES);
        for (var exportedComponent : exportedComponents) {
            var key = Objects.requireNonNull(componentKeys.get(exportedComponent));
            tag(ConventionTags.EXPORTED_SETTINGS).add(key);
        }
    }
}
