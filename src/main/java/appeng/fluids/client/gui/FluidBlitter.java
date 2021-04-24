package appeng.fluids.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.fluid.Fluid;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.FluidStack;

import appeng.client.gui.style.Blitter;

/**
 * Creates a {@link Blitter} to draw fluids into the user interface.
 */
public final class FluidBlitter {

    private FluidBlitter() {
    }

    public static Blitter create(FluidStack stack) {
        Fluid fluid = stack.getFluid();
        FluidAttributes attributes = fluid.getAttributes();
        TextureAtlasSprite sprite = Minecraft.getInstance()
                .getAtlasSpriteGetter(PlayerContainer.LOCATION_BLOCKS_TEXTURE)
                .apply(attributes.getStillTexture(stack));

        return Blitter.sprite(sprite)
                .colorRgb(attributes.getColor())
                // Most fluid texture have transparency, but we want an opaque slot
                .blending(false);
    }

}
