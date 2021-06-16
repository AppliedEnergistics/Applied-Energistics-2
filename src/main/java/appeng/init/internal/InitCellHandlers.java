package appeng.init.internal;

import appeng.api.features.IRegistryContainer;
import appeng.core.Api;
import appeng.core.features.registries.cell.BasicCellHandler;
import appeng.core.features.registries.cell.BasicItemCellGuiHandler;
import appeng.core.features.registries.cell.CreativeCellHandler;
import appeng.fluids.registries.BasicFluidCellGuiHandler;

public final class InitCellHandlers {

    private InitCellHandlers() {
    }

    public static void init() {
        final IRegistryContainer registries = Api.INSTANCE.registries();
        registries.cell().addCellHandler(new BasicCellHandler());
        registries.cell().addCellHandler(new CreativeCellHandler());
        registries.cell().addCellGuiHandler(new BasicItemCellGuiHandler());
        registries.cell().addCellGuiHandler(new BasicFluidCellGuiHandler());
    }

}
