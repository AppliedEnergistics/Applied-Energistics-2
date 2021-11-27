package appeng.parts.automation;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

import appeng.api.networking.IGrid;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.core.settings.TickRates;
import appeng.menu.implementations.IOBusMenu;

public class ImportBusPart extends IOBusPart {
    private StackImportStrategy importStrategy;

    public ImportBusPart(ItemStack is) {
        super(TickRates.ImportBus, is);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        bch.addBox(6, 6, 11, 10, 10, 13);
        bch.addBox(5, 5, 13, 11, 11, 14);
        bch.addBox(4, 4, 14, 12, 12, 16);
    }

    @Override
    protected TickRateModulation doBusWork(IGrid grid) {
        if (!this.canDoBusWork()) {
            return TickRateModulation.IDLE;
        }

        if (importStrategy == null) {
            var self = this.getHost().getBlockEntity();
            var fromPos = self.getBlockPos().relative(this.getSide());
            var fromSide = getSide().getOpposite();
            importStrategy = StackWorldBehaviors.createImportFacade((ServerLevel) getLevel(), fromPos, fromSide);
        }

        var context = new StackTransferContext(
                grid.getStorageService().getInventory(),
                grid.getEnergyService(),
                this.source,
                getOperationsPerTick(),
                getFilter());

        importStrategy.transfer(context);

        return context.hasDoneWork() ? TickRateModulation.FASTER : TickRateModulation.SLOWER;
    }

    @Override
    protected MenuType<?> getMenuType() {
        return IOBusMenu.IMPORT_TYPE;
    }

    @Override
    public IPartModel getStaticModels() {
        if (this.isActive() && this.isPowered()) {
            return MODELS_HAS_CHANNEL;
        } else if (this.isPowered()) {
            return MODELS_ON;
        } else {
            return MODELS_OFF;
        }
    }
}
