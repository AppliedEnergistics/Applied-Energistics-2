package appeng.items.materials;

import javax.annotation.Nonnegative;

public class EnergyCardItem extends UpgradeCardItem {

    private final int energyMultiplier;

    public EnergyCardItem(Properties properties, @Nonnegative int energyMultiplier) {
        super(properties);
        this.energyMultiplier = energyMultiplier;
    }

    public int getEnergyMultiplier() {
        return this.energyMultiplier;
    }
}
