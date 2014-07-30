package appeng.block.crafting;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import appeng.api.AEApi;
import appeng.block.AEBaseItemBlock;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;

public class ItemCraftingStorage extends AEBaseItemBlock
{

	public ItemCraftingStorage(Block id) {
		super( id );
	}

	@Override
	public boolean hasContainerItem()
	{
		return AEConfig.instance.isFeatureEnabled( AEFeature.enableDisassemblyCrafting );
	}

	@Override
	public ItemStack getContainerItem(ItemStack itemStack)
	{
		return AEApi.instance().blocks().blockCraftingUnit.stack( 1 );
	}

}
