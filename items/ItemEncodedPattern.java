package appeng.items;

import java.util.EnumSet;

import net.minecraft.item.ItemStack;
import appeng.api.crafting.ICraftingPatternMAC;
import appeng.api.implementations.ICraftingPatternItem;
import appeng.core.features.AEFeature;

public class ItemEncodedPattern extends AEBaseItem implements ICraftingPatternItem
{

	public ItemEncodedPattern() {
		super( ItemEncodedPattern.class );
		setfeature( EnumSet.of( AEFeature.Crafting ) );
	}

	@Override
	public ICraftingPatternMAC getPatternForItem(ItemStack is)
	{
		// TODO Auto-generated method stub
		return null;
	}

}
