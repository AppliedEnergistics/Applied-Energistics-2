package appeng.tile.crafting;

import net.minecraft.item.ItemStack;

/**
 * Stores client-side only state about the ongoing animation for a molecular
 * assembler.
 */
public class AssemblerAnimationStatus {

    private final ItemStack is;

    private final byte speed;

    private final int ticksRequired;

    private float accumulatedTicks;

    private float ticksUntilParticles;

    public AssemblerAnimationStatus(byte speed, ItemStack is) {
        this.speed = speed;
        this.is = is;
        this.ticksRequired = (int) Math.ceil(Math.max(1, 100.0f / speed)) + 2;
    }

    public ItemStack getIs() {
        return is;
    }

    public byte getSpeed() {
        return speed;
    }

    public float getAccumulatedTicks() {
        return accumulatedTicks;
    }

    public void setAccumulatedTicks(float accumulatedTicks) {
        this.accumulatedTicks = accumulatedTicks;
    }

    public float getTicksUntilParticles() {
        return ticksUntilParticles;
    }

    public void setTicksUntilParticles(float ticksUntilParticles) {
        this.ticksUntilParticles = ticksUntilParticles;
    }

    public boolean isExpired() {
        return accumulatedTicks > ticksRequired;
    }
}
