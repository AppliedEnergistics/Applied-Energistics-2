package appeng.client.model;

import java.util.List;
import java.util.Objects;

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

import appeng.client.api.model.parts.PartModel;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;

public record LockableMonitorPartModel(SimpleModelWrapper unpoweredUnlockedModel,
        SimpleModelWrapper poweredUnlockedModel,
        SimpleModelWrapper unpoweredLockedModel,
        SimpleModelWrapper poweredLockedModel) implements PartModel {

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random,
            List<BlockModelPart> parts) {
        var locked = Objects.requireNonNullElse(partModelData.get(PartModelData.MONITOR_LOCKED), false);
        var statusIndicator = Objects.requireNonNullElse(partModelData.get(PartModelData.STATUS_INDICATOR),
                PartModelData.StatusIndicatorState.UNPOWERED);

        if (statusIndicator == PartModelData.StatusIndicatorState.UNPOWERED) {
            if (locked) {
                parts.add(unpoweredLockedModel);
            } else {
                parts.add(unpoweredUnlockedModel);
            }
        } else {
            if (locked) {
                parts.add(poweredLockedModel);
            } else {
                parts.add(poweredUnlockedModel);
            }
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return unpoweredLockedModel.particleIcon();
    }

    public record Unbaked(ResourceLocation unpoweredUnlockedModel,
            ResourceLocation poweredUnlockedModel,
            ResourceLocation unpoweredLockedModel,
            ResourceLocation poweredLockedModel) implements PartModel.Unbaked {

        public static final ResourceLocation ID = AppEng.makeId("lockable_monitor");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("unpowered_unlocked_model").forGetter(Unbaked::unpoweredUnlockedModel),
                ResourceLocation.CODEC.fieldOf("powered_unlocked_model").forGetter(Unbaked::poweredUnlockedModel),
                ResourceLocation.CODEC.fieldOf("unpowered_locked_model").forGetter(Unbaked::unpoweredLockedModel),
                ResourceLocation.CODEC.fieldOf("powered_locked_model").forGetter(Unbaked::poweredLockedModel))
                .apply(builder, Unbaked::new));

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var unpoweredUnlockedModel = SimpleModelWrapper.bake(baker, this.unpoweredUnlockedModel, modelState);
            var poweredUnlockedModel = SimpleModelWrapper.bake(baker, this.poweredUnlockedModel, modelState);
            var unpoweredLockedModel = SimpleModelWrapper.bake(baker, this.unpoweredLockedModel, modelState);
            var poweredLockedModel = SimpleModelWrapper.bake(baker, this.poweredLockedModel, modelState);

            return new LockableMonitorPartModel(unpoweredUnlockedModel, poweredUnlockedModel, unpoweredLockedModel,
                    poweredLockedModel);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(unpoweredUnlockedModel);
            resolver.markDependency(poweredUnlockedModel);
            resolver.markDependency(unpoweredLockedModel);
            resolver.markDependency(poweredLockedModel);
        }
    }
}
