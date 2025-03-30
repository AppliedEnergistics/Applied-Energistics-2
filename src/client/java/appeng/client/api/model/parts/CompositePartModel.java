package appeng.client.api.model.parts;

import appeng.client.model.PartModels;
import appeng.core.AppEng;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.List;

/**
 * Combines multiple part models into one.
 */
public record CompositePartModel(List<PartModel> models) implements PartModel {
    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random, List<BlockModelPart> parts) {
        for (var model : models) {
            model.collectParts(level, pos, partModelData, random, parts);
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return models.getFirst().particleIcon();
    }

    public record Unbaked(List<PartModel.Unbaked> models) implements PartModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("composite");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                PartModels.CODEC.listOf().fieldOf("models").forGetter(Unbaked::models)
        ).apply(builder, Unbaked::new));

        public Unbaked {
            if (models.isEmpty()) {
                throw new IllegalStateException("Cannot construct an empty composite part model.");
            }
        }

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var baked = models.stream()
                    .map(unbaked -> unbaked.bake(baker, modelState))
                    .toList();

            return new CompositePartModel(baked);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            for (var model : models) {
                model.resolveDependencies(resolver);
            }
        }
    }
}
