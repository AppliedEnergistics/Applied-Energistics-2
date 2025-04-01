package appeng.client.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
import net.neoforged.neoforge.client.model.IQuadTransformer;
import net.neoforged.neoforge.client.model.QuadTransformers;
import net.neoforged.neoforge.model.data.ModelData;

import appeng.client.api.model.parts.PartModel;
import appeng.client.render.CubeBuilder;
import appeng.core.AppEng;
import appeng.parts.automation.PartModelData;
import appeng.parts.automation.PlaneConnections;

public class PlanePartModel implements PartModel {

    private static final PlaneConnections DEFAULT_PERMUTATION = PlaneConnections.of(false, false, false, false);

    private final Map<PlaneConnections, SimpleModelWrapper> onParts;
    private final Map<PlaneConnections, SimpleModelWrapper> offParts;

    private final TextureAtlasSprite frontOnSprite;
    private final TextureAtlasSprite frontOffSprite;

    public PlanePartModel(TextureAtlasSprite frontOnSprite,
            TextureAtlasSprite frontOffSprite,
            TextureAtlasSprite sidesSprite,
            TextureAtlasSprite backSprite,
            Transformation transformation) {
        this.frontOnSprite = frontOnSprite;
        this.frontOffSprite = frontOffSprite;

        var quadTransformer = QuadTransformers.applying(BlockMath.blockCenterToCorner(transformation));

        onParts = new HashMap<>(PlaneConnections.PERMUTATIONS.size());
        offParts = new HashMap<>(PlaneConnections.PERMUTATIONS.size());
        // Create all possible permutations (16)
        for (var permutation : PlaneConnections.PERMUTATIONS) {
            this.onParts.put(permutation, new SimpleModelWrapper(
                    buildQuads(frontOnSprite, sidesSprite, backSprite, permutation, quadTransformer),
                    true,
                    frontOnSprite,
                    RenderType.solid()));
            this.offParts.put(permutation, new SimpleModelWrapper(
                    buildQuads(frontOffSprite, sidesSprite, backSprite, permutation, quadTransformer),
                    true,
                    frontOffSprite,
                    RenderType.solid()));
        }
    }

    private static QuadCollection buildQuads(TextureAtlasSprite frontSprite,
            TextureAtlasSprite sidesSprite,
            TextureAtlasSprite backSprite,
            PlaneConnections permutation,
            IQuadTransformer quadTransformer) {
        var quads = new QuadCollection.Builder();

        var builder = new CubeBuilder(quad -> {
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
        return quads.build();
    }

    @Override
    public void collectParts(BlockAndTintGetter level, BlockPos pos, ModelData partModelData, RandomSource random,
            List<BlockModelPart> parts) {
        var connections = partModelData.get(PartModelData.CONNECTIONS);
        if (connections == null) {
            connections = DEFAULT_PERMUTATION;
        }
        var indicatorState = Objects.requireNonNullElse(partModelData.get(PartModelData.STATUS_INDICATOR),
                PartModelData.StatusIndicatorState.UNPOWERED);
        if (indicatorState == PartModelData.StatusIndicatorState.ACTIVE) {
            parts.add(this.onParts.get(connections));
        } else {
            parts.add(this.offParts.get(connections));
        }
    }

    @Override
    public TextureAtlasSprite particleIcon() {
        return frontOffSprite;
    }

    public record Unbaked(
            ResourceLocation frontOnTexture,
            ResourceLocation frontOffTexture,
            ResourceLocation sidesTexture,
            ResourceLocation backTexture) implements PartModel.Unbaked {

        public static final ResourceLocation ID = AppEng.makeId("plane");

        public static MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("front_on").forGetter(Unbaked::frontOnTexture),
                ResourceLocation.CODEC.fieldOf("front_off").forGetter(Unbaked::frontOffTexture),
                ResourceLocation.CODEC.fieldOf("sides").forGetter(Unbaked::sidesTexture),
                ResourceLocation.CODEC.fieldOf("back").forGetter(Unbaked::backTexture)).apply(builder, Unbaked::new));

        @Override
        public MapCodec<? extends PartModel.Unbaked> codec() {
            return MAP_CODEC;
        }

        @Override
        public PartModel bake(ModelBaker baker, ModelState modelState) {
            ModelDebugName debugName = getClass()::toString;

            var frontOnSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, frontOnTexture),
                    debugName);
            var frontOffSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, frontOffTexture),
                    debugName);
            var sidesSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, sidesTexture), debugName);
            var backSprite = baker.sprites().get(new Material(TextureAtlas.LOCATION_BLOCKS, backTexture), debugName);

            return new PlanePartModel(
                    frontOnSprite,
                    frontOffSprite,
                    sidesSprite,
                    backSprite,
                    modelState.transformation());
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
        }
    }
}
