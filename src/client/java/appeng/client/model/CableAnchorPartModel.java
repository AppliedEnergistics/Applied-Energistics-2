package appeng.client.model;

import appeng.client.api.model.parts.PartModel;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;
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

public class CableAnchorPartModel implements PartModel {
    private final SimpleModelWrapper model;
    private final SimpleModelWrapper shortModel;

    public CableAnchorPartModel(SimpleModelWrapper model, SimpleModelWrapper shortModel) {
        this.model = model;
        this.shortModel = shortModel;
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random, List<BlockModelPart> parts) {
        var shortVersion = Objects.requireNonNullElse(partModelData.get(PartModelData.CABLE_ANCHOR_SHORT), false);

        if (shortVersion) {
            parts.add(shortModel);
        } else {
            parts.add(model);
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return model.particleIcon();
    }

    public record Unbaked(ResourceLocation model, ResourceLocation shortModel) implements PartModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("cable_anchor");
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("model").forGetter(Unbaked::model),
                ResourceLocation.CODEC.fieldOf("short_model").forGetter(Unbaked::shortModel)
        ).apply(builder, Unbaked::new));

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            var bakedModel = SimpleModelWrapper.bake(baker, model, modelState);
            var shortBakedModel = SimpleModelWrapper.bake(baker, shortModel, modelState);

            return new CableAnchorPartModel(bakedModel, shortBakedModel);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(model);
            resolver.markDependency(shortModel);
        }
    }
}
