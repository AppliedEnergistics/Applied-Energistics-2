package appeng.client.render.cablebus;

import net.minecraftforge.client.model.data.ModelProperty;

import appeng.client.render.model.AEInternalModelData;

public final class P2PTunnelFrequencyModelData extends AEInternalModelData {

    public static final ModelProperty<Long> FREQUENCY = new ModelProperty<>();

    private final long frequency;

    public P2PTunnelFrequencyModelData(long frequency) {
        this.frequency = frequency;
    }

    public long getFrequency() {
        return frequency;
    }

}
