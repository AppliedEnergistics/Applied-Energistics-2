package appeng.client.api.model.parts;

import appeng.api.parts.IPart;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

public interface PartModel {
    /**
     * Collects model parts of this part model.
     *
     * @param level         The level the part host is in.
     * @param pos           The position the part host is located at.
     * @param partModelData Any part model state previously collected from {@link IPart#getModelData()}.
     */
    void collectParts(BlockAndTintGetter level,
                      BlockPos pos,
                      ModelData partModelData,
                      RandomSource random,
                      List<BlockModelPart> parts);

    TextureAtlasSprite particleIcon();

    /**
     * An unbaked {@link PartModel} which is what is deserialized from the JSON file and ultimately used
     * to produce a {@link PartModel} and its dependencies.
     */
    interface Unbaked extends ResolvableModel {
        MapCodec<? extends Unbaked> codec();

        PartModel bake(ModelBaker baker, ModelState modelState);
    }
}
