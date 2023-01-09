package com.enderio.core.common.interfaces;

import net.minecraft.item.ItemStack;

import javax.annotation.Nonnull;

public interface IOverlayRenderAware {

    public void renderItemOverlayIntoGUI(@Nonnull ItemStack stack, int xPosition, int yPosition);

}
