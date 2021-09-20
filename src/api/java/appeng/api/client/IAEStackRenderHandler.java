package appeng.api.client;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.storage.data.IAEStack;

/**
 * Client-side rendering of AE stacks. Must be registered in {@link AEStackRendering} for each storage channel!
 */
@OnlyIn(Dist.CLIENT)
public interface IAEStackRenderHandler<T extends IAEStack> {
    void drawRepresentation(AbstractContainerScreen<?> screen, PoseStack poseStack, int x, int y, T stack); // TODO:
                                                                                                            // pass
                                                                                                            // Minecraft
                                                                                                            // instead
                                                                                                            // of screen

    Component getDisplayName(T stack);

    String formatAmount(long amount, AmountFormat format);

    String getModid(T stack);
}
