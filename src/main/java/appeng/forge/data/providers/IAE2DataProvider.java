package appeng.forge.data.providers;

import net.minecraft.data.DataProvider;

import appeng.api.AEApi;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;

public interface IAE2DataProvider extends DataProvider {
    IBlocks BLOCKS = AEApi.instance().definitions().blocks();
    IItems ITEMS = AEApi.instance().definitions().items();
    IMaterials MATERIALS = AEApi.instance().definitions().materials();
}
