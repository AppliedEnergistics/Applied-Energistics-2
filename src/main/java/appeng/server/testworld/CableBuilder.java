package appeng.server.testworld;

import java.util.function.Consumer;

import net.minecraft.core.Direction;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluid;

import appeng.api.config.Settings;
import appeng.api.config.YesNo;
import appeng.api.parts.IPart;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.core.definitions.ItemDefinition;
import appeng.items.parts.PartItem;

public class CableBuilder {
    private final PlotBuilder plotBuilder;
    private final String bb;

    public CableBuilder(PlotBuilder plotBuilder, String bb) {
        this.plotBuilder = plotBuilder;
        this.bb = bb;
    }

    public CableBuilder part(Direction side, ItemDefinition<? extends PartItem<?>> part) {
        plotBuilder.part(bb, side, part);
        return this;
    }

    public <T extends IPart> CableBuilder part(Direction side,
            ItemDefinition<? extends PartItem<T>> part,
            Consumer<T> partCustomizer) {
        plotBuilder.part(bb, side, part, partCustomizer);
        return this;
    }

    /**
     * Set up a level emitter configured to emit a given item for crafting.
     */
    public CableBuilder craftingEmitter(Direction side, AEKey what) {
        return part(side, AEParts.LEVEL_EMITTER, emitter -> {
            emitter.getUpgrades().addItems(AEItems.CRAFTING_CARD.stack());
            emitter.getConfigManager().putSetting(Settings.CRAFT_VIA_REDSTONE, YesNo.YES);
            emitter.getConfig().addFilter(what);
        });
    }

    public CableBuilder craftingEmitter(Direction side, ItemLike what) {
        return craftingEmitter(side, AEItemKey.of(what));
    }

    public CableBuilder craftingEmitter(Direction side, Fluid what) {
        return craftingEmitter(side, AEFluidKey.of(what));
    }
}
