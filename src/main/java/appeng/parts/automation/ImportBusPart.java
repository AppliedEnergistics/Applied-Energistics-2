package appeng.parts.automation;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import appeng.api.parts.IPartCollisionHelper;
import appeng.api.storage.AEKeyFilter;
import appeng.api.storage.data.AEKey;
import appeng.core.settings.TickRates;
import appeng.parts.PartAdjacentApi;

public abstract class ImportBusPart<T extends AEKey, A> extends IOBusPart {
    protected final PartAdjacentApi<A> adjacentExternalApi;

    public ImportBusPart(TickRates tickRates, ItemStack is, BlockApiLookup<A, Direction> apiLookup,
            AEKeyFilter configFilter) {
        super(tickRates, is, configFilter);
        this.adjacentExternalApi = new PartAdjacentApi<>(this, apiLookup);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }
}
