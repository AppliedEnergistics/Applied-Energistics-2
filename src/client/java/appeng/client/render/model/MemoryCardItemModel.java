package appeng.client.render.model;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.client.resources.model.ModelDebugName;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.client.render.CubeBuilder;
import appeng.client.render.ItemBaseModelWrapper;
import appeng.core.AppEng;
import appeng.items.tools.MemoryCardItem;
import org.joml.Matrix4fc;

public class MemoryCardItemModel implements ItemModel {
    private final ItemBaseModelWrapper baseModel;
    private final LoadingCache<MemoryCardColors, List<BakedQuad>> hashModelCache;
    private final Matrix4fc transform;

    public MemoryCardItemModel(ItemBaseModelWrapper baseModel, Material.Baked hashSprite, Matrix4fc transform) {
        this.baseModel = baseModel;
        this.hashModelCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public List<BakedQuad> load(MemoryCardColors colors) {
                        return buildColorQuads(hashSprite, colors);
                    }
                });
        this.transform = transform;
    }

    @Override
    public void update(ItemStackRenderState renderState,
            ItemStack stack,
            ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable ItemOwner owner,
            int seed) {

        if (!(stack.getItem() instanceof MemoryCardItem item)) {
            return;
        }

        renderState.appendModelIdentityElement(this);

        var baseLayer = renderState.newLayer();
        var tint = baseLayer.tintLayers();
        tint.add(-1);
        tint.add(ARGB.opaque(item.getColor(stack)));
        baseModel.applyToLayer(baseLayer, displayContext);
        renderState.appendModelIdentityElement(tint.getInt(1));

        var colors = stack.getOrDefault(AEComponents.MEMORY_CARD_COLORS, MemoryCardColors.DEFAULT);
        var colorLayer = renderState.newLayer();
        colorLayer.setLocalTransform(transform);
        colorLayer.setExtents(baseModel.extents());
        baseModel.renderProperties().applyToLayer(colorLayer, displayContext);
        colorLayer.prepareQuadList().addAll(hashModelCache.getUnchecked(colors));
        renderState.appendModelIdentityElement(colors);
    }

    private static List<BakedQuad> buildColorQuads(Material.Baked texture, MemoryCardColors colors) {
        var quads = new ArrayList<BakedQuad>(2 * 4 * 6);
        CubeBuilder builder = new CubeBuilder(quads::add);
        builder.setDrawFaces(EnumSet.of(Direction.NORTH, Direction.SOUTH));
        builder.setCustomUv(Direction.NORTH, 0, 0, 1, 1);
        builder.setCustomUv(Direction.SOUTH, 0, 0, 1, 1);

        builder.setTexture(texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                var color = colors.get(x, y);
                builder.setColorRGB(color.mediumVariant);
                builder.addCube(8 + x, 8 + 1 - y, 7.5f, 8 + x + 1, 8 + 1 - y + 1, 8.5f);
            }
        }

        return quads;
    }

    public record Unbaked(Identifier baseModel,
            Identifier colorOverlaySprite) implements ItemModel.Unbaked {
        public static final Identifier ID = AppEng.makeId("memory_card_identity");

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        Identifier.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
                        Identifier.CODEC.fieldOf("color_overlay_sprite").forGetter(Unbaked::colorOverlaySprite))
                        .apply(builder, Unbaked::new));

        @Override
        public MemoryCardItemModel bake(BakingContext context, Matrix4fc transform) {

            ModelDebugName debugName = getClass()::toString;
            var colorOverlayMaterial = new Material(colorOverlaySprite);
            var hashSprite = context.blockModelBaker().materials().get(colorOverlayMaterial, debugName);

            var baseModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), this.baseModel, transform);
            return new MemoryCardItemModel(baseModel, hashSprite, transform);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.markDependency(baseModel);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
