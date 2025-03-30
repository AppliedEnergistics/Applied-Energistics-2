package appeng.client.model;

import appeng.client.api.model.parts.PartModel;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;
import com.mojang.math.Transformation;
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

import java.util.List;
import java.util.Objects;

/**
 * Adds model parts based on the status of the parts main grid node.
 */
public record StatusIndicatorPartModel(
        SimpleModelWrapper activeBaked,
        SimpleModelWrapper poweredBaked,
        SimpleModelWrapper unpoweredBaked,
        Transformation transformation
) implements PartModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random, List<BlockModelPart> parts) {
        var state = Objects.requireNonNullElse(partModelData.get(PartModelData.STATUS_INDICATOR), PartModelData.StatusIndicatorState.UNPOWERED);

        var model = switch (state) {
            case ACTIVE -> activeBaked;
            case POWERED -> poweredBaked;
            case UNPOWERED -> unpoweredBaked;
        };
        parts.add(model);
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return activeBaked.particleIcon();
    }

    public record Unbaked(ResourceLocation active, ResourceLocation powered,
                          ResourceLocation unpowered) implements PartModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("status_indicator");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        ResourceLocation.CODEC.fieldOf("active").forGetter(Unbaked::active),
                        ResourceLocation.CODEC.fieldOf("powered").forGetter(Unbaked::powered),
                        ResourceLocation.CODEC.fieldOf("unpowered").forGetter(Unbaked::unpowered)
                ).apply(builder, Unbaked::new)
        );

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var activeBaked = SimpleModelWrapper.bake(baker, active, modelState);
            var poweredBaked = SimpleModelWrapper.bake(baker, powered(), modelState);
            var unpoweredBaked = SimpleModelWrapper.bake(baker, unpowered(), modelState);

            return new StatusIndicatorPartModel(
                    activeBaked,
                    poweredBaked,
                    unpoweredBaked,
                    modelState.transformation()
            );
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(active);
            resolver.markDependency(powered);
            resolver.markDependency(unpowered);
        }
    }
}
