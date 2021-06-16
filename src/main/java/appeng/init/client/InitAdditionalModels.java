package appeng.init.client;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoader;

import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.core.Api;
import appeng.core.features.registries.PartModels;

/**
 * Registers any JSON model files with Minecraft that are not referenced via blockstates or item IDs
 */
@OnlyIn(Dist.CLIENT)
public class InitAdditionalModels {

    public static void init() {
        ModelLoader.addSpecialModel(MolecularAssemblerRenderer.LIGHTS_MODEL);

        PartModels partModels = (PartModels) Api.INSTANCE.registries().partModels();
        partModels.getModels().forEach(ModelLoader::addSpecialModel);
        partModels.setInitialized(true);
    }

}
