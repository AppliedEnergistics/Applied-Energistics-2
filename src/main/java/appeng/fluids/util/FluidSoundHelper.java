package appeng.fluids.util;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

/**
 * Helps with playing fill/empty sounds for fluids to players.
 */
public final class FluidSoundHelper {

    private FluidSoundHelper() {
    }

    public static void playFillSound(PlayerEntity player, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return;
        }

        SoundEvent fillSound = fluidStack.getFluid().getAttributes().getFillSound(fluidStack);
        if (fillSound == null) {
            return;
        }

        playSound(player, fillSound);
    }

    public static void playEmptySound(PlayerEntity player, FluidStack fluidStack) {
        if (fluidStack.isEmpty()) {
            return;
        }

        SoundEvent fillSound = fluidStack.getFluid().getAttributes().getEmptySound(fluidStack);
        if (fillSound == null) {
            return;
        }

        playSound(player, fillSound);
    }

    /**
     * @see net.minecraftforge.fluids.FluidUtil#tryFillContainer(ItemStack, IFluidHandler, int, PlayerEntity, boolean)
     */
    private static void playSound(PlayerEntity player, SoundEvent fillSound) {
        // This should just play the sound for the player themselves
        player.playSound(fillSound, SoundCategory.PLAYERS, 1.0F, 1.0F);
    }

}
