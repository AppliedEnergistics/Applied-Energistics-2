package appeng.client.render.model;

import appeng.api.implementations.items.IMemoryCard;
import appeng.api.util.AEColor;
import appeng.client.render.cablebus.CubeBuilder;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.item.ItemStack;

import java.util.Random;
import java.util.function.Supplier;

class MemoryCardBakedModel extends ForwardingBakedModel implements FabricBakedModel {
    private static final AEColor[] DEFAULT_COLOR_CODE = new AEColor[] { AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT, AEColor.TRANSPARENT,
            AEColor.TRANSPARENT, };

    private final Sprite texture;

    public MemoryCardBakedModel(BakedModel baseModel, Sprite texture) {
        this.wrapped = baseModel;
        this.texture = texture;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    @Override
    public void emitItemQuads(ItemStack stack, Supplier<Random> randomSupplier, RenderContext context) {

        context.fallbackConsumer().accept(wrapped);

        AEColor[] colorCode = getColorCode(stack);

        CubeBuilder builder = new CubeBuilder(context.getEmitter());

        builder.setTexture(this.texture);

        for (int x = 0; x < 4; x++) {
            for (int y = 0; y < 2; y++) {
                final AEColor color = colorCode[x + y * 4];

                builder.setColorRGB(color.mediumVariant);
                builder.addCube(7 + x, 8 + (1 - y), 7.5f, 7 + x + 1, 8 + (1 - y) + 1, 8.5f);
            }
        }
    }

    private static AEColor[] getColorCode(ItemStack stack) {
        if (stack.getItem() instanceof IMemoryCard) {
            final IMemoryCard memoryCard = (IMemoryCard) stack.getItem();
            return memoryCard.getColorCode(stack);
        }

        return DEFAULT_COLOR_CODE;
    }

}
