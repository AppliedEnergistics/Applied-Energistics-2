package appeng.hooks;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import alexiil.mc.lib.attributes.fluid.FluidAttributes;
import alexiil.mc.lib.attributes.fluid.volume.FluidVolume;

import appeng.client.render.DummyFluidBakedModel;
import appeng.fluids.items.FluidDummyItem;
import appeng.items.misc.EncodedPatternItem;
import appeng.mixins.ItemRendererAccessor;

public final class ItemRendererHooks {

    // Prevents recursion in the hook below
    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    private ItemRendererHooks() {
    }

    /**
     * This hook will exchange the rendered item model for encoded patterns to the
     * item being crafted by them if shift is held.
     */
    public static boolean onRenderGuiItemModel(ItemRenderer renderer, ItemStack stack, int x, int y, BakedModel model) {
        if (OVERRIDING_FOR.get() == stack) {
            return false; // Don't allow recursive model replacements
        }

        if (stack.getItem() instanceof EncodedPatternItem) {
            boolean shiftHeld = Screen.hasShiftDown();
            ClientWorld world = MinecraftClient.getInstance().world;
            if (shiftHeld && world != null) {
                EncodedPatternItem iep = (EncodedPatternItem) stack.getItem();
                ItemStack output = iep.getOutput(world, stack);
                if (!output.isEmpty()) {
                    BakedModel realModel = MinecraftClient.getInstance().getItemRenderer().getModels().getModel(output);
                    renderInstead(renderer, stack, x, y, realModel);
                    return true;
                }
            }
        } else if (stack.getItem() instanceof FluidDummyItem) {
            FluidDummyItem itemFacade = (FluidDummyItem) stack.getItem();
            FluidVolume fluidStack = itemFacade.getFluidStack(stack);
            renderInstead(renderer, stack, x, y, new DummyFluidBakedModel(fluidStack));
            return true;
        }

        return false;
    }

    private static void renderInstead(ItemRenderer renderer, ItemStack stack, int x, int y, BakedModel realModel) {
        ItemRendererAccessor self = (ItemRendererAccessor) renderer;
        OVERRIDING_FOR.set(stack);
        try {
            self.callRenderGuiItemModel(stack, x, y, realModel);
        } finally {
            OVERRIDING_FOR.remove();
        }
    }

}
