package appeng.datagen.providers.models;

import net.minecraft.data.DataGenerator;
import net.minecraftforge.client.model.generators.BlockModelBuilder;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import appeng.core.AppEng;

public class PartModelProvider extends ModelProvider<BlockModelBuilder> {
    public PartModelProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, AppEng.MOD_ID, "part", BlockModelBuilder::new, existingFileHelper);
    }

    @Override
    public String getName() {
        return "Part Models: " + modid;
    }

    @Override
    protected void registerModels() {
        addBuiltInModel("part/annihilation_plane");
        addBuiltInModel("part/annihilation_plane_on");
        addBuiltInModel("part/identity_annihilation_plane");
        addBuiltInModel("part/identity_annihilation_plane_on");
        addBuiltInModel("part/formation_plane");
        addBuiltInModel("part/formation_plane_on");
        addBuiltInModel("part/p2p/p2p_tunnel_frequency");
    }

    /**
     * The files need to exist for Fabric's post-processor to pick them up. The content is ignored. For Forge, we still
     * set the loader name, since it'll be used there.
     */
    private void addBuiltInModel(String name) {
        getBuilder(name);
    }
}
