package appeng.client.model;

import appeng.client.api.model.parts.PartModel;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;
import appeng.parts.automation.PlaneConnections;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BlockModelPart;
import net.minecraft.client.renderer.block.model.SimpleModelWrapper;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.client.resources.model.QuadCollection;
import net.minecraft.core.BlockMath;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.model.data.ModelData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlanePartModel implements PartModel {

    private static final PlaneConnections DEFAULT_PERMUTATION = PlaneConnections.of(false, false, false, false);

    private final Map<PlaneConnections, SimpleModelWrapper> parts;

    private final TextureAtlasSprite frontSprite;

    public PlanePartModel(TextureAtlasSprite frontSprite,
                          TextureAtlasSprite sidesSprite,
                          TextureAtlasSprite backSprite,
                          Transformation transformation) {
        this.frontSprite = frontSprite;

        var quadTransformer = QuadTransformers.applying(BlockMath.blockCenterToCorner(transformation));

        parts = new HashMap<>(PlaneConnections.PERMUTATIONS.size());
        // Create all possible permutations (16)
        for (var permutation : PlaneConnections.PERMUTATIONS) {
            var quads = new QuadCollection.Builder();

            CubeBuilder builder = new CubeBuilder(quad -> {
                quadTransformer.processInPlace(quad);
                quads.addUnculledFace(quad);
            });

            builder.setTextures(sidesSprite, sidesSprite, frontSprite, backSprite, sidesSprite, sidesSprite);

            // Keep the orientation of the X axis in mind here. When looking at a quad
            // facing north from the front,
            // The X-axis points left
            int minX = permutation.isRight() ? 0 : 1;
            int maxX = permutation.isLeft() ? 16 : 15;
            int minY = permutation.isDown() ? 0 : 1;
            int maxY = permutation.isUp() ? 16 : 15;

            builder.addCube(minX, minY, 0, maxX, maxY, 1);

            this.parts.put(permutation, new SimpleModelWrapper(
                    quads.build(),
                    true,
                    frontSprite,
                    RenderType.solid()
            ));
        }
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random, List<BlockModelPart> parts) {
        var modelData = level.getModelData(pos);
        var connections = DEFAULT_PERMUTATION;
        if (modelData.has(PartModelData.CONNECTIONS)) {
            connections = modelData.get(PartModelData.CONNECTIONS);
        }
        parts.add(this.parts.get(connections));
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return frontSprite;
    }

    public record Unbaked(ResourceLocation frontTexture, ResourceLocation sidesTexture,
                          ResourceLocation backTexture) implements PartModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("plane");

        public static MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("front").forGetter(Unbaked::frontTexture),
                ResourceLocation.CODEC.fieldOf("sides").forGetter(Unbaked::frontTexture),
                ResourceLocation.CODEC.fieldOf("back").forGetter(Unbaked::frontTexture)
        ).apply(builder, Unbaked::new));

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            ModelDebugName debugName = getClass()::toString;

            var frontSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, frontTexture), debugName);
            var sidesSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, sidesTexture), debugName);
            var backSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, backTexture), debugName);

            return new PlanePartModel(frontSprite, sidesSprite, backSprite, modelState.transformation());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }
    }
}
