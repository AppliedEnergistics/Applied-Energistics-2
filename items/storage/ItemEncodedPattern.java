package appeng.items.storage;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import appeng.api.crafting.ICraftingPatternDetails;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.core.features.AEFeature;
import appeng.items.AEBaseItem;

public class ItemEncodedPattern extends AEBaseItem implements ICraftingPatternItem
{

	public ItemEncodedPattern() {
		super( ItemEncodedPattern.class );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
		setMaxStackSize( 1 );
	}

	@Override
	public ICraftingPatternDetails getPatternForItem(ItemStack is)
	{
		return null;
	}

}
