package appeng.init.client;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.eventbus.api.IEventBus;

import appeng.block.AEBaseBlock;
import appeng.client.render.crafting.MonitorBakedModel;
import appeng.client.render.model.AutoRotatingBakedModel;
import appeng.core.AppEng;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.features.BlockDefinition;

public final class InitAutoRotatingModel {

    /**
     * Blocks that should not use the auto rotation model.
     */
    private static final Set<BlockDefinition> NO_AUTO_ROTATION = ImmutableSet.of(
            ApiBlocks.MULTI_PART,
            ApiBlocks.CONTROLLER,
            ApiBlocks.PAINT,
            ApiBlocks.QUANTUM_LINK,
            ApiBlocks.QUANTUM_RING,
            ApiBlocks.CRAFTING_UNIT,
            ApiBlocks.CRAFTING_ACCELERATOR,
            ApiBlocks.CRAFTING_MONITOR,
            ApiBlocks.CRAFTING_STORAGE_1K,
            ApiBlocks.CRAFTING_STORAGE_4K,
            ApiBlocks.CRAFTING_STORAGE_16K,
            ApiBlocks.CRAFTING_STORAGE_64K);

    // Maps from resource path to customizer
    private static final Map<String, Function<IBakedModel, IBakedModel>> CUSTOMIZERS = new HashMap<>();

    private InitAutoRotatingModel() {
    }

    public static void init(IEventBus modEventBus) {
        register(ApiBlocks.CRAFTING_MONITOR, InitAutoRotatingModel::customizeCraftingMonitorModel);

        for (BlockDefinition block : ApiBlocks.getBlocks()) {
            if (NO_AUTO_ROTATION.contains(block)) {
                continue;
            }

            if (block.block() instanceof AEBaseBlock) {
                // This is a default rotating model if the base-block uses an AE tile entity
                // which exposes UP/FRONT as extended props
                register(block, AutoRotatingBakedModel::new);
            }
        }

        modEventBus.addListener(InitAutoRotatingModel::onModelBake);
    }

    private static void register(BlockDefinition block, Function<IBakedModel, IBakedModel> customizer) {
        String path = block.block().getRegistryName().getPath();
        CUSTOMIZERS.put(path, customizer);
    }

    private static IBakedModel customizeCraftingMonitorModel(IBakedModel model) {
        // The formed model handles rotations itself, the unformed one does not
        if (model instanceof MonitorBakedModel) {
            return model;
        }
        return new AutoRotatingBakedModel(model);
    }

    private static void onModelBake(ModelBakeEvent event) {
        Map<ResourceLocation, IBakedModel> modelRegistry = event.getModelRegistry();
        Set<ResourceLocation> keys = Sets.newHashSet(modelRegistry.keySet());
        IBakedModel missingModel = modelRegistry.get(ModelBakery.MODEL_MISSING);

        for (ResourceLocation location : keys) {
            if (!location.getNamespace().equals(AppEng.MOD_ID)) {
                continue;
            }

            IBakedModel orgModel = modelRegistry.get(location);

            // Don't customize the missing model. This causes Forge to swallow exceptions
            if (orgModel == missingModel) {
                continue;
            }

            Function<IBakedModel, IBakedModel> customizer = CUSTOMIZERS.get(location.getPath());
            if (customizer != null) {
                IBakedModel newModel = customizer.apply(orgModel);

                if (newModel != orgModel) {
                    modelRegistry.put(location, newModel);
                }
            }
        }
    }

}
