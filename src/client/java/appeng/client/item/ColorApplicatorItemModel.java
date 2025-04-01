package appeng.client.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import appeng.client.render.ItemBaseModelWrapper;
import appeng.core.AppEng;
import appeng.items.tools.powered.ColorApplicatorItem;

public class ColorApplicatorItemModel implements ItemModel {
    private final ItemBaseModelWrapper uncoloredModel;
    private final ItemBaseModelWrapper coloredModel;

    public ColorApplicatorItemModel(ItemBaseModelWrapper uncoloredModel, ItemBaseModelWrapper coloredModel) {
        this.uncoloredModel = uncoloredModel;
        this.coloredModel = coloredModel;
    }

    @Override
    public void update(ItemStackRenderState renderState,
            ItemStack stack,
            ItemModelResolver itemModelResolver,
            ItemDisplayContext displayContext,
            @Nullable ClientLevel level,
            @Nullable LivingEntity entity,
            int seed) {
        if (!(stack.getItem() instanceof ColorApplicatorItem colorApplicator)) {
            return;
        }

        var layer = renderState.newLayer();

        var currentColor = colorApplicator.getColor(stack);
        if (currentColor == null) {
            uncoloredModel.applyToLayer(layer, displayContext);
        } else {
            var tint = layer.prepareTintLayers(3);
            tint[0] = ARGB.opaque(currentColor.blackVariant);
            tint[1] = ARGB.opaque(currentColor.mediumVariant);
            tint[2] = ARGB.opaque(currentColor.whiteVariant);

            coloredModel.applyToLayer(layer, displayContext);
        }
    }

    public record Unbaked(ResourceLocation uncoloredModel, ResourceLocation coloredModel) implements ItemModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("color_applicator");

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        ResourceLocation.CODEC.fieldOf("uncolored_model").forGetter(Unbaked::uncoloredModel),
                        ResourceLocation.CODEC.fieldOf("colored_model").forGetter(Unbaked::coloredModel))
                        .apply(builder, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(uncoloredModel);
            resolver.markDependency(coloredModel);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            var uncoloredBakedModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), this.uncoloredModel);
            var coloredBakedModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), this.coloredModel);
            return new ColorApplicatorItemModel(uncoloredBakedModel, coloredBakedModel);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
