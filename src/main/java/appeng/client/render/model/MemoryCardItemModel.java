package appeng.client.render.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import appeng.api.ids.AEComponents;
import appeng.api.implementations.items.MemoryCardColors;
import appeng.core.AppEng;
import appeng.items.tools.MemoryCardItem;

public class MemoryCardItemModel implements ItemModel {
    private final BakedModel baseModel;
    private final LoadingCache<MemoryCardColors, MemoryCardHashBakedModel> hashModelCache;

    public MemoryCardItemModel(BakedModel baseModel, TextureAtlasSprite hashSprite) {
        this.baseModel = baseModel;
        this.hashModelCache = CacheBuilder.newBuilder()
                .maximumSize(100)
                .build(new CacheLoader<>() {
                    @Override
                    public MemoryCardHashBakedModel load(MemoryCardColors colors) {
                        return new MemoryCardHashBakedModel(baseModel, hashSprite, colors);
                    }
                });
    }

    @Override
    public void update(ItemStackRenderState renderState,
            ItemStack stack,
            ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable LivingEntity entity,
            int seed) {

        if (!(stack.getItem() instanceof MemoryCardItem item)) {
            return;
        }

        var baseLayer = renderState.newLayer();
        var tint = baseLayer.prepareTintLayers(2);
        tint[0] = -1;
        tint[1] = ARGB.opaque(item.getColor(stack));
        baseLayer.setupBlockModel(baseModel, baseModel.getRenderType(stack));

        var colors = stack.getOrDefault(AEComponents.MEMORY_CARD_COLORS, MemoryCardColors.DEFAULT);
        renderState.newLayer().setupBlockModel(hashModelCache.getUnchecked(colors), baseModel.getRenderType(stack));
    }

    public record Unbaked(ResourceLocation baseModel,
            ResourceLocation colorOverlaySprite) implements ItemModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("memory_card_identity");

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        ResourceLocation.CODEC.fieldOf("base_model").forGetter(Unbaked::baseModel),
                        ResourceLocation.CODEC.fieldOf("color_overlay_sprite").forGetter(Unbaked::colorOverlaySprite))
                        .apply(builder, Unbaked::new));

        @Override
        public MemoryCardItemModel bake(BakingContext context) {

            var colorOverlayMaterial = new Material(TextureAtlas.LOCATION_BLOCKS, colorOverlaySprite);
            var hashSprite = context.blockModelBaker().sprites().get(colorOverlayMaterial);

            var baseModel = context.bake(this.baseModel);
            return new MemoryCardItemModel(baseModel, hashSprite);
        }

        @Override
        public void resolveDependencies(Resolver resolver) {
            resolver.resolve(baseModel);
        }

        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
