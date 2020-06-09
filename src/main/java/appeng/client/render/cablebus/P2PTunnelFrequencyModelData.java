package appeng.client.render.cablebus;

import appeng.client.render.model.AEInternalModelData;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelProperty;

import javax.annotation.Nullable;

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
