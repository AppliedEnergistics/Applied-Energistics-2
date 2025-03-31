package appeng.client.api.model.parts;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.api.util.AEColor;
import appeng.core.AppEng;

/**
 * <dl>
 * <dt>Tint Index 1</dt>
 * <dd>The {@link AEColor#blackVariant dark variant color} of the cable that this part is attached to.</dd>
 * <dt>Tint Index 2</dt>
 * <dd>The {@link AEColor#mediumVariant color} of the cable that this part is attached to.</dd>
 * <dt>Tint Index 3</dt>
 * <dd>The {@link AEColor#whiteVariant bright variant color} of the cable that this part is attached to.</dd>
 * <dt>Tint Index 4</dt>
 * <dd>A color variant that is between the cable's {@link AEColor#mediumVariant color} and its
 * {@link AEColor#whiteVariant bright variant}.</dd>
 * </dl>
 */
public class StaticPartModel implements PartModel {
    private final SimpleModelWrapper model;

    public StaticPartModel(SimpleModelWrapper model) {
        this.model = model;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random,
            List<BlockModelPart> parts) {
        parts.add(model);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return model.particleIcon();
    }

    public record Unbaked(ResourceLocation model) implements PartModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("model");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::model)).apply(builder, Unbaked::new));

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var bakedModel = SimpleModelWrapper.bake(baker, model, modelState);

            return new StaticPartModel(bakedModel);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
        }
    }
}
