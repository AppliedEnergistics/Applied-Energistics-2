package appeng.integration.modules.trenergy;

import net.minecraft.item.ItemStack;

import team.reborn.energy.Energy;
import team.reborn.energy.EnergySide;
import team.reborn.energy.EnergyStorage;
import team.reborn.energy.EnergyTier;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * Adapts an itemstack that implements {@link appeng.api.implementations.items.IAEItemPowerStorage} to the interface
 * used by TechReborn's energy API.
 */
public final class ItemPowerStorageAdapter implements EnergyStorage {

    private final IAEItemPowerStorage item;

    private final ItemStack stack;

    public ItemPowerStorageAdapter(IAEItemPowerStorage item, ItemStack stack) {
        this.item = item;
        this.stack = stack;
    }

    @Override
    public double getStored(EnergySide energySide) {
        return PowerUnits.AE.convertTo(PowerUnits.TR, this.item.getAECurrentPower(this.stack));
    }

    @Override
    public void setStored(double v) {
        double newPower = PowerUnits.TR.convertTo(PowerUnits.AE, v);
        double currentPower = this.item.getAECurrentPower(this.stack);
        double toInject = newPower - currentPower;
        if (toInject > 0.0000001) {
            this.item.injectAEPower(stack, toInject, Actionable.MODULATE);
        }
    }

    @Override
    public double getMaxStoredPower() {
        return PowerUnits.AE.convertTo(PowerUnits.TR, this.item.getAEMaxPower(this.stack));
    }

    @Override
    public EnergyTier getTier() {
        return EnergyTier.INFINITE;
    }

    public static void register() {
        // Register our charged items as TechReborn Energy compatible
        Energy.registerHolder(
                (Object obj) -> obj instanceof ItemStack && ((ItemStack) obj).getItem() instanceof IAEItemPowerStorage,
                (Object obj) -> {
                    ItemStack stack = (ItemStack) obj;
                    IAEItemPowerStorage item = (IAEItemPowerStorage) stack.getItem();
                    return new ItemPowerStorageAdapter(item, stack);
                });
    }

}
