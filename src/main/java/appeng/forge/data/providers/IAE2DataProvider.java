package appeng.forge.data.providers;

import net.minecraft.data.IDataProvider;

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;

public interface IAE2DataProvider extends IDataProvider {
    IBlocks BLOCKS = Api.instance().definitions().blocks();
    IItems ITEMS = Api.instance().definitions().items();
    IMaterials MATERIALS = Api.instance().definitions().materials();
}
