
package appeng.client.model;

import java.util.List;
import java.util.Objects;

import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.client.api.model.parts.PartModel;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;

/**
 * Adds model parts based on the status of the level emitter.
 */
public record LevelEmitterPartModel(
        BlockModelPart onBaked,
        BlockModelPart offBaked,
        Transformation transformation) implements PartModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random,
            List<BlockModelPart> parts) {
        var on = Objects.requireNonNullElse(partModelData.get(PartModelData.LEVEL_EMITTER_ON), false);

        if (on) {
            parts.add(onBaked);
        } else {
            parts.add(offBaked);
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return onBaked.particleIcon();
    }

    public record Unbaked(Identifier on, Identifier off) implements PartModel.Unbaked {
        public static final Identifier ID = AppEng.makeId("level_emitter");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        Identifier.CODEC.fieldOf("on").forGetter(Unbaked::on),
                        Identifier.CODEC.fieldOf("off").forGetter(Unbaked::off)).apply(builder, Unbaked::new));

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var onBaked = SimpleModelWrapper.bake(baker, on, modelState);
            var offBaked = SimpleModelWrapper.bake(baker, off(), modelState);

            return new LevelEmitterPartModel(
                    onBaked,
                    offBaked,
                    modelState.transformation());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(on);
            resolver.markDependency(off);
        }
    }
}
