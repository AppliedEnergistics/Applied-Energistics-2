package appeng.items.materials;

import java.util.Collections;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.ItemStackSrc;
import appeng.items.AEBaseItem;
import appeng.util.Platform;

public class ItemMaterial extends AEBaseItem implements IStorageComponent, IUpgradeModule
{

	HashMap<Integer, MaterialType> dmgToMaterial = new HashMap();

	public static ItemMaterial instance;

	public ItemMaterial() {
		super( ItemMaterial.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
		setHasSubtypes( true );
		instance = this;
	}

	class SlightlyBetterSort implements Comparator<String>
	{

		Pattern p;

		public SlightlyBetterSort(Pattern p) {
			this.p = p;
		}

		@Override
		public int compare(String o1, String o2)
		{
			try
			{
				Matcher a = p.matcher( o1 );
				Matcher b = p.matcher( o2 );
				if ( a.find() && b.find() )
				{
					int ia = Integer.parseInt( a.group( 1 ) );
					int ib = Integer.parseInt( b.group( 1 ) );
					return Integer.compare( ia, ib );
				}
			}
			catch (Throwable t)
			{
				// ek!
			}
			return o1.compareTo( o2 );
		}
	}

	@Override
	public void addInformation(ItemStack par1ItemStack, EntityPlayer par2EntityPlayer, List par3List, boolean par4)
	{
		super.addInformation( par1ItemStack, par2EntityPlayer, par3List, par4 );

		Upgrades u = getType( par1ItemStack );
		if ( u != null )
		{
			List<String> textList = new LinkedList();
			for (Entry<ItemStack, Integer> j : u.getSupported().entrySet())
			{
				String name = null;

				int limit = j.getValue();

				if ( j.getKey().getItem() instanceof IItemGroup )
				{
					IItemGroup ig = (IItemGroup) j.getKey().getItem();
					String str = ig.getUnlocalizedGroupName( j.getKey() );
					if ( str != null )
						name = Platform.gui_localize( str ) + (limit > 1 ? " (" + limit + ")" : "");
				}

				if ( name == null )
					name = j.getKey().getDisplayName() + (limit > 1 ? " (" + limit + ")" : "");

				if ( !textList.contains( name ) )
					textList.add( name );
			}

			Pattern p = Pattern.compile( "(\\d+)[^\\d]" );
			SlightlyBetterSort s = new SlightlyBetterSort( p );
			Collections.sort( textList, s );
			par3List.addAll( textList );
		}
	}

	public ItemStackSrc createMaterial(MaterialType mat)
	{
		String name = mat.name();

		if ( mat.damageValue == -1 )
		{
			boolean enabled = true;
			for (AEFeature f : mat.getFeature())
				enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

			if ( enabled )
			{
				int newMaterialNum = AEConfig.instance.get( "materials", name, AEConfig.instance.getFreeMaterial() ).getInt();
				mat.damageValue = newMaterialNum;
				ItemStackSrc output = new ItemStackSrc( this, newMaterialNum );

				dmgToMaterial.put( newMaterialNum, mat );

				if ( mat.getOreName() != null )
					OreDictionary.registerOre( mat.getOreName(), output.stack( 1 ) );

				return output;
			}

			return null;
		}
		else
			throw new RuntimeException( "Cannot create the same material twice..." );
	}

	public MaterialType getTypeByStack(ItemStack is)
	{
		return dmgToMaterial.get( is.getItemDamage() );
	}

	@Override
	public IIcon getIconFromDamage(int dmg)
	{
		return dmgToMaterial.get( dmg ).IIcon;
	}

	private String nameOf(ItemStack is)
	{
		return AEFeatureHandler.getName( ItemMaterial.class, getTypeByStack( is ).name() );
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		return "item.appliedenergistics2." + nameOf( is );
	}

	@Override
	public void registerIcons(IIconRegister icoRegister)
	{
		for (MaterialType mat : MaterialType.values())
		{
			if ( mat.damageValue != -1 )
			{
				String tex = "appliedenergistics2:" + nameOf( new ItemStack( this, 1, mat.damageValue ) );
				mat.IIcon = icoRegister.registerIcon( tex );
			}
		}
	}

	@Override
	public boolean hasCustomEntity(ItemStack is)
	{
		return getTypeByStack( is ).hasCustomEntity();
	}

	@Override
	public Entity createEntity(World w, Entity location, ItemStack itemstack)
	{
		Class<? extends Entity> droppedEntity = getTypeByStack( itemstack ).getCustomEntityClass();
		Entity eqi;

		try
		{
			eqi = droppedEntity.getConstructor( World.class, double.class, double.class, double.class, ItemStack.class ).newInstance( w, location.posX,
					location.posY, location.posZ, itemstack );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}

		eqi.motionX = location.motionX;
		eqi.motionY = location.motionY;
		eqi.motionZ = location.motionZ;

		if ( location instanceof EntityItem && eqi instanceof EntityItem )
			((EntityItem) eqi).delayBeforeCanPickup = ((EntityItem) location).delayBeforeCanPickup;

		return eqi;
	}

	@Override
	public int getBytes(ItemStack is)
	{
		switch (getTypeByStack( is ))
		{
		case Cell1kPart:
			return 1024;
		case Cell4kPart:
			return 1024 * 4;
		case Cell16kPart:
			return 1024 * 16;
		case Cell64kPart:
			return 1024 * 64;
		default:
		}
		return 0;
	}

	@Override
	public boolean isStorageComponent(ItemStack is)
	{
		switch (getTypeByStack( is ))
		{
		case Cell1kPart:
		case Cell4kPart:
		case Cell16kPart:
		case Cell64kPart:
			return true;
		default:
		}
		return false;
	}

	@Override
	public Upgrades getType(ItemStack itemstack)
	{
		switch (getTypeByStack( itemstack ))
		{
		case CardCapacity:
			return Upgrades.CAPACITY;
		case CardFuzzy:
			return Upgrades.FUZZY;
		case CardRedstone:
			return Upgrades.REDSTONE;
		case CardSpeed:
			return Upgrades.SPEED;
		case CardInverter:
			return Upgrades.INVERTER;
		default:
			return null;
		}
	}

	@Override
	public void getSubItems(Item par1, CreativeTabs par2CreativeTabs, List cList)
	{
		for (MaterialType mat : MaterialType.values())
		{
			if ( mat.damageValue >= 0 )
				cList.add( new ItemStack( this, 1, mat.damageValue ) );
		}
	}
}
