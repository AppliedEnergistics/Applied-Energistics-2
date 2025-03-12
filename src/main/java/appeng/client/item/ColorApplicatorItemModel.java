package appeng.client.item;

import appeng.api.util.AEColor;
import appeng.core.AppEng;
import appeng.items.tools.powered.ColorApplicatorItem;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.ItemModel;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ResolvableModel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;

public class ColorApplicatorItemModel implements ItemModel {
    private final BakedModel uncoloredBakedModel;
    private final BakedModel coloredBakedModel;

    public ColorApplicatorItemModel(BakedModel uncoloredBakedModel, BakedModel coloredBakedModel) {
        this.uncoloredBakedModel = uncoloredBakedModel;
        this.coloredBakedModel = coloredBakedModel;
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
            layer.setupBlockModel(uncoloredBakedModel, uncoloredBakedModel.getRenderType(stack));
        } else {
            var tint = layer.prepareTintLayers(3);
            tint[0] = ARGB.opaque(currentColor.blackVariant);
            tint[1] = ARGB.opaque(currentColor.mediumVariant);
            tint[2] = ARGB.opaque(currentColor.whiteVariant);

            layer.setupBlockModel(coloredBakedModel, coloredBakedModel.getRenderType(stack));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public record Unbaked(ResourceLocation uncoloredModel, ResourceLocation coloredModel) implements ItemModel.Unbaked {
        public static final ResourceLocation ID = AppEng.makeId("color_applicator");

        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(
                builder -> builder.group(
                                ResourceLocation.CODEC.fieldOf("uncolored_model").forGetter(Unbaked::uncoloredModel),
                                ResourceLocation.CODEC.fieldOf("colored_model").forGetter(Unbaked::coloredModel)
                        )
                        .apply(builder, Unbaked::new)
        );

        @Override
        public void resolveDependencies(ResolvableModel.Resolver resolver) {
            resolver.resolve(uncoloredModel);
            resolver.resolve(coloredModel);
        }

        @Override
        public ItemModel bake(ItemModel.BakingContext context) {
            var uncoloredBakedModel = context.bake(this.uncoloredModel);
            var coloredBakedModel = context.bake(this.coloredModel);
            return new ColorApplicatorItemModel(uncoloredBakedModel, coloredBakedModel);
        }

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }
    }
}
