package appeng.items;

import java.util.EnumSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class AEBaseItem extends Item implements IAEFeature
{

	final String featureFullName;
	final String featureSubName;
	AEFeatureHandler feature;

	@Override
	public String toString()
	{
		return featureFullName;
	}

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public void setFeature(EnumSet<AEFeature> f)
	{
		feature = new AEFeatureHandler( f, this, featureSubName );
	}

	public AEBaseItem(Class c) {
		this( c, null );
		canRepair = false;
	}

	public AEBaseItem(Class c, String subName) {
		featureSubName = subName;
		featureFullName = AEFeatureHandler.getName( c, subName );
	}

	@Override
	public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2)
	{
		return false;
	}

	@Override
	public void postInit()
	{
		// override!
	}
}
