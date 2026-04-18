package appeng.client.api.model.parts;

import java.util.List;

import com.mojang.serialization.MapCodec;

import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.client.renderer.block.dispatch.BlockStateModelPart;
import net.minecraft.client.renderer.block.dispatch.ModelState;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.api.parts.IPart;

public interface PartModel {
    /**
     * Collects model parts of this part model.
     *
     * @param level         The level the part host is in.
     * @param pos           The position the part host is located at.
     * @param partModelData Any part model state previously collected from
     *                      {@link IPart#collectModelData(ModelData.Builder)}.
     */
    void collectParts(BlockAndTintGetter level,
            BlockPos pos,
            ModelData partModelData,
            RandomSource random,
            List<BlockStateModelPart> parts);

    Material.Baked particleMaterial();

    /**
     * An unbaked {@link PartModel} which is what is deserialized from the JSON file and ultimately used to produce a
     * {@link PartModel} and its dependencies.
     */
    interface Unbaked extends ResolvableModel {
        MapCodec<? extends Unbaked> codec();

        PartModel bake(ModelBaker baker, ModelState modelState);
    }
}
