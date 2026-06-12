package appeng.client.item;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4fc;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.ItemOwner;
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
            @Nullable ItemOwner owner,
            int seed) {
        if (!(stack.getItem() instanceof ColorApplicatorItem colorApplicator)) {
            return;
        }

        var layer = renderState.newLayer();

        var currentColor = colorApplicator.getColor(stack);
        if (currentColor == null) {
            uncoloredModel.applyToLayer(layer, displayContext);
        } else {
            renderState.appendModelIdentityElement(currentColor);
            var tint = layer.tintLayers();
            tint.add(ARGB.opaque(currentColor.blackVariant));
            tint.add(ARGB.opaque(currentColor.mediumVariant));
            tint.add(ARGB.opaque(currentColor.whiteVariant));

            coloredModel.applyToLayer(layer, displayContext);
        }
    }

    public record Unbaked(Identifier uncoloredModel, Identifier coloredModel) implements ItemModel.Unbaked {
        public static final Identifier ID = AppEng.makeId("color_applicator");

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                        Identifier.CODEC.fieldOf("uncolored_model").forGetter(Unbaked::uncoloredModel),
                        Identifier.CODEC.fieldOf("colored_model").forGetter(Unbaked::coloredModel))
                        .apply(builder, Unbaked::new));

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.markDependency(uncoloredModel);
            resolver.markDependency(coloredModel);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context, Matrix4fc transform) {
            var uncoloredBakedModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), this.uncoloredModel,
                    transform);
            var coloredBakedModel = ItemBaseModelWrapper.bake(context.blockModelBaker(), this.coloredModel, transform);
            return new ColorApplicatorItemModel(uncoloredBakedModel, coloredBakedModel);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
