package appeng.items;

import java.util.EnumSet;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;

public class AEBaseItem extends Item implements IAEFeature
{

	String FeatureFullname;
	String FeatureSubname;
	AEFeatureHandler feature;

	@Override
	public String toString()
	{
		return FeatureFullname;
	}

	@Override
	public AEFeatureHandler feature()
	{
		return feature;
	}

	public void setfeature(EnumSet<AEFeature> f)
	{
		feature = new AEFeatureHandler( f, this, FeatureSubname );
	}

	public AEBaseItem(Class c) {
		this( c, null );
		canRepair = false;
	}

	public AEBaseItem(Class c, String subname) {
		super( Configuration.instance.getItemID( c, subname ) );
		FeatureSubname = subname;
		FeatureFullname = AEFeatureHandler.getName( c, subname );
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
