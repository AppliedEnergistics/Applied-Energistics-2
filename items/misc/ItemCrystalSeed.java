package appeng.items.misc;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.core.AppEng;
import appeng.core.features.AEFeature;
import appeng.entity.EntityGrowingCrystal;
import appeng.entity.EntityIds;
import appeng.items.AEBaseItem;
import cpw.mods.fml.common.registry.EntityRegistry;

public class ItemCrystalSeed extends AEBaseItem implements IGrowableCrystal
{

	IIcon certus[] = new IIcon[3];
	IIcon fluix[] = new IIcon[3];
	IIcon nether[] = new IIcon[3];

	public ItemCrystalSeed() {
		super( ItemCrystalSeed.class );
		setHasSubtypes( true );
		setMaxStackSize( 8 );
		setfeature( EnumSet.of( AEFeature.Core ) );

		EntityRegistry.registerModEntity( EntityGrowingCrystal.class, EntityGrowingCrystal.class.getSimpleName(), EntityIds.get( EntityGrowingCrystal.class ),
				AppEng.instance, 16, 4, true );
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		int damage = is.getItemDamage();

		if ( damage < 600 )
			return getUnlocalizedName() + ".Certus";

		if ( damage < 1200 )
			return getUnlocalizedName() + ".Nether";

		if ( damage < 1800 )
			return getUnlocalizedName() + ".Fluix";

		return getUnlocalizedName();
	}

	@Override
	public ItemStack triggerGrowth(ItemStack is)
	{
		int newDamage = is.getItemDamage() + 1;

		if ( newDamage == 600 )
			return AEApi.instance().materials().materialPureifiedCertusQuartzCrystal.stack( is.stackSize );
		if ( newDamage == 1200 )
			return AEApi.instance().materials().materialPureifiedNetherQuartzCrystal.stack( is.stackSize );
		if ( newDamage == 1800 )
			return AEApi.instance().materials().materialPureifiedFluixCrystal.stack( is.stackSize );
		if ( newDamage > 1800 )
			return null;

		is.setItemDamage( newDamage );
		return is;
	}

	@Override
	public boolean isDamageable()
	{
		return true;
	}

	@Override
	public boolean isDamaged(ItemStack stack)
	{
		if ( stack.getItemDamage() % 200 == 0 )
			return false;
		return true;
	}

	@Override
	public int getDisplayDamage(ItemStack stack)
	{
		return stack.getItemDamage() % 200;
	}

	@Override
	public int getMaxDamage(ItemStack stack)
	{
		return 200;
	}

	@Override
	public IIcon getIconFromDamage(int damage)
	{
		IIcon list[] = null;

		if ( damage < 600 )
			list = certus;

		else if ( damage < 1200 )
		{
			damage -= 600;
			list = nether;
		}

		else if ( damage < 1800 )
		{
			damage -= 1200;
			list = fluix;
		}

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
		return 0.5f;
	}

	@Override
	public void registerIcons(IIconRegister ir)
	{
		String preFix = "appliedenergistics2:GrowthSeed.";

		certus[0] = ir.registerIcon( preFix + "Certus" );
		certus[1] = ir.registerIcon( preFix + "Certus2" );
		certus[2] = ir.registerIcon( preFix + "Certus3" );

		nether[0] = ir.registerIcon( preFix + "Nether" );
		nether[1] = ir.registerIcon( preFix + "Nether2" );
		nether[2] = ir.registerIcon( preFix + "Nether3" );

		fluix[0] = ir.registerIcon( preFix + "Fluix" );
		fluix[1] = ir.registerIcon( preFix + "Fluix2" );
		fluix[2] = ir.registerIcon( preFix + "Fluix3" );
	}

	@Override
	public boolean hasCustomEntity(ItemStack stack)
	{
		return true;
	}

	@Override
	public Entity createEntity(World world, Entity location, ItemStack itemstack)
	{
		EntityGrowingCrystal egc = new EntityGrowingCrystal( world, location.posX, location.posY, location.posZ, itemstack );

		egc.motionX = location.motionX;
		egc.motionY = location.motionY;
		egc.motionZ = location.motionZ;

		if ( location instanceof EntityItem && egc instanceof EntityItem )
			((EntityItem) egc).delayBeforeCanPickup = ((EntityItem) location).delayBeforeCanPickup;

		return egc;
	}

	@Override
	public void getSubItems(Item i, CreativeTabs t, List l)
	{
		// lvl 0
		l.add( new ItemStack( this, 1, 0 ) );
		l.add( new ItemStack( this, 1, 600 ) );
		l.add( new ItemStack( this, 1, 1200 ) );

		// lvl 1
		l.add( new ItemStack( this, 1, 200 + 0 ) );
		l.add( new ItemStack( this, 1, 200 + 600 ) );
		l.add( new ItemStack( this, 1, 200 + 1200 ) );

		// lvl 2
		l.add( new ItemStack( this, 1, 400 + 0 ) );
		l.add( new ItemStack( this, 1, 400 + 600 ) );
		l.add( new ItemStack( this, 1, 400 + 1200 ) );
	}

}
