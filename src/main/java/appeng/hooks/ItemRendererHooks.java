package appeng.hooks;

import appeng.items.misc.EncodedPatternItem;
import appeng.mixins.ItemRendererAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.item.ItemStack;

public final class ItemRendererHooks {

    // Prevents recursion in the hook below
    private static final ThreadLocal<ItemStack> OVERRIDING_FOR = new ThreadLocal<>();

    private ItemRendererHooks() {
    }

    /**
     * This hook will exchange the rendered item model for encoded patterns to the item being crafted by them
     * if shift is held.
     */
    public static boolean onRenderGuiItemModel(ItemRenderer renderer, ItemStack stack, int x, int y, BakedModel model) {
        if (stack.getItem() instanceof EncodedPatternItem && OVERRIDING_FOR.get() != stack) {
            boolean shiftHeld = Screen.hasShiftDown();
            ClientWorld world = MinecraftClient.getInstance().world;
            if (shiftHeld && world != null) {
                EncodedPatternItem iep = (EncodedPatternItem) stack.getItem();
                ItemStack output = iep.getOutput(world, stack);
                if (!output.isEmpty()) {
                    BakedModel realModel = MinecraftClient.getInstance().getItemRenderer().getModels()
                            .getModel(output);
                    ItemRendererAccessor self = (ItemRendererAccessor) renderer;
                    OVERRIDING_FOR.set(stack);
                    try {
                        self.callRenderGuiItemModel(stack, x, y, realModel);
                    } finally {
                        OVERRIDING_FOR.remove();
                    }
                    return true;
                }
            }
        }
        return false;
    }

}
