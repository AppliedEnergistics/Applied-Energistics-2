package appeng.datagen.providers.models;

import appeng.core.AppEng;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelInstance;
import net.minecraft.data.PackOutput;

public class PartModelProvider extends AE2BlockStateProvider {
    public PartModelProvider(PackOutput packOutput) {
        super(packOutput, AppEng.MOD_ID);
    }

    @Override
    public String getName() {
        return "AE2 Part Models";
    }

    @Override
    protected void register(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
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
        blockModels.modelOutput.accept(AppEng.makeId(name), () -> new JsonObject());
    }
}
