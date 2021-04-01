package appeng.data.providers;

import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.core.Api;
import net.minecraft.data.IDataProvider;

public interface IAE2DataProvider extends IDataProvider {
    IBlocks BLOCKS = Api.instance().definitions().blocks();
    IItems ITEMS = Api.instance().definitions().items();
    IMaterials MATERIALS = Api.instance().definitions().materials();
}
