package appeng.items.misc;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import appeng.api.AEApi;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.items.AEBaseItem;

public class ItemCrystalSeed extends AEBaseItem implements IGrowableCrystal
{

	IIcon certus[] = new IIcon[3];
	IIcon fluix[] = new IIcon[3];
	IIcon nether[] = new IIcon[3];

	public ItemCrystalSeed() {
		super( ItemCrystalSeed.class );
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		int damage = is.getItemDamage();

		if ( damage <= 600 )
			return getUnlocalizedName() + ".Certus";

		if ( damage <= 1200 )
			return getUnlocalizedName() + ".Nether";

		if ( damage <= 1800 )
			return getUnlocalizedName() + ".Fluix";

		return getUnlocalizedName();
	}

	@Override
	public ItemStack triggerGrowth(ItemStack is)
	{
		is.setItemDamage( is.getItemDamage() + 1 );

		if ( is.getItemDamage() > 1800 ) // max!
			return is;

		if ( is.getItemDamage() == 600 )
			return AEApi.instance().materials().materialPureifiedCertusQuartzCrystal.stack( is.stackSize );
		if ( is.getItemDamage() == 1200 )
			return AEApi.instance().materials().materialPureifiedNetherQuartzCrystal.stack( is.stackSize );
		if ( is.getItemDamage() == 1800 )
			return AEApi.instance().materials().materialPureifiedFluixCrystal.stack( is.stackSize );

		return is;
	}

	@Override
	public IIcon getIconFromDamage(int damage)
	{
		IIcon list[] = null;

		if ( damage < 600 )
			list = certus;

		if ( damage < 1200 )
			list = nether;

		if ( damage < 1800 )
			list = fluix;

		if ( list == null )
			return Items.diamond.getIconFromDamage( 0 );

		if ( damage < 200 )
			return list[0];
		else if ( damage < 400 )
			return list[1];
		else
			return list[2];
	}

	@Override
	public float getMultiplier(Block blk, Material mat)
	{
		return 1.0f;
	}

	@Override
	public void registerIcons(IIconRegister ir)
	{
		String preFix = "appliedenergistics2:GrowthSeed.";

		certus[0] = ir.registerIcon( preFix + "Certus" );
		certus[1] = ir.registerIcon( preFix + "Certus1" );
		certus[2] = ir.registerIcon( preFix + "Certus2" );

		nether[0] = ir.registerIcon( preFix + "Nether" );
		nether[1] = ir.registerIcon( preFix + "Nether1" );
		nether[2] = ir.registerIcon( preFix + "Nether2" );

		fluix[0] = ir.registerIcon( preFix + "Fluix" );
		fluix[1] = ir.registerIcon( preFix + "Fluix1" );
		fluix[2] = ir.registerIcon( preFix + "Fluix2" );
	}

}
