package appeng.integration.modules.trenergy;

import net.minecraft.item.ItemStack;

import dev.technici4n.fasttransferlib.api.Simulation;
import dev.technici4n.fasttransferlib.api.energy.EnergyApi;
import dev.technici4n.fasttransferlib.api.energy.EnergyIo;

import appeng.api.config.Actionable;
import appeng.api.config.PowerUnits;
import appeng.api.implementations.items.IAEItemPowerStorage;

/**
 * Adapts an itemstack that implements {@link appeng.api.implementations.items.IAEItemPowerStorage} to the interface
 * used by FTL energy.
 */
public final class ItemPowerStorageAdapter implements EnergyIo {

    private final IAEItemPowerStorage item;

    private final ItemStack stack;

    public ItemPowerStorageAdapter(IAEItemPowerStorage item, ItemStack stack) {
        this.item = item;
        this.stack = stack;
    }

    public static void register() {
        // Register our charged items as TechReborn Energy compatible
        EnergyApi.ITEM.registerFallback((stack, ignored) -> {
            if (stack.getItem() instanceof IAEItemPowerStorage) {
                return new ItemPowerStorageAdapter((IAEItemPowerStorage) stack.getItem(), stack);
            }

            return null;
        });
    }

    @Override
    public double getEnergy() {
        return PowerUnits.AE.convertTo(PowerUnits.TR, item.getAECurrentPower(stack));
    }

    @Override
    public double getEnergyCapacity() {
        return PowerUnits.AE.convertTo(PowerUnits.TR, this.item.getAEMaxPower(this.stack));
    }

    @Override
    public double insert(double maxAmount, Simulation simulation) {
        double convertedOffer = PowerUnits.TR.convertTo(PowerUnits.AE, maxAmount);
        double overflow = item.injectAEPower(stack, convertedOffer,
                simulation.isSimulating() ? Actionable.SIMULATE : Actionable.MODULATE);
        return PowerUnits.AE.convertTo(PowerUnits.TR, overflow);
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }
}
