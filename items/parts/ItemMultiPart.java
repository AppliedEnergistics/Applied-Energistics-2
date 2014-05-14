package appeng.items.parts;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.ItemStackSrc;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemMultiPart extends AEBaseItem implements IPartItem, IItemGroup
{

	class PartTypeIst
	{

		PartType part;
		int varient;

		@SideOnly(Side.CLIENT)
		IIcon ico;
	};

	HashMap<Integer, PartTypeIst> dmgToPart = new HashMap();

	public static ItemMultiPart instance;

	public ItemMultiPart() {
		super( ItemMultiPart.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
		AEApi.instance().partHelper().setItemBusRenderer( this );
		setHasSubtypes( true );
		instance = this;
	}

	public ItemStackSrc createPart(PartType mat, Enum varient)
	{
		try
		{
			// I think this still works?
			ItemStack is = new ItemStack( this );
			mat.getPart().getConstructor( ItemStack.class ).newInstance( is );
		}
		catch (Throwable e)
		{
			AELog.integration(e);
			return null; // part not supported..
		}

		int varID = varient == null ? 0 : varient.ordinal();

		// verify
		for (PartTypeIst p : dmgToPart.values())
		{
			if ( p.part == mat && p.varient == varID )
				throw new RuntimeException( "Cannot create the same material twice..." );
		}

		boolean enabled = true;
		for (AEFeature f : mat.getFeature())
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

		if ( enabled )
		{
			int newPartNum = mat.baseDamage + varID;
			ItemStackSrc output = new ItemStackSrc( this, newPartNum );

			PartTypeIst pti = new PartTypeIst();
			pti.part = mat;
			pti.varient = varID;

			if ( dmgToPart.get( newPartNum ) == null )
			{
				dmgToPart.put( newPartNum, pti );
				return output;
			}
			else
				throw new RuntimeException( "Meta Overlap detected." );
		}

		return null;
	}

	public int getDamageByType(PartType t)
	{
		for (Entry<Integer, PartTypeIst> pt : dmgToPart.entrySet())
		{
			if ( pt.getValue().part == t )
				return pt.getKey();
		}
		return -1;
	}

	public PartType getTypeByStack(ItemStack is)
	{
		if ( is == null )
			return null;

		PartTypeIst pt = dmgToPart.get( is.getItemDamage() );
		if ( pt != null )
			return pt.part;

		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	public IIcon getIconFromDamage(int dmg)
	{
		IIcon ico = dmgToPart.get( dmg ).ico;
		return ico;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		return "item.appliedenergistics2." + getname( is );
	}

	public String getname(ItemStack is)
	{
		return AEFeatureHandler.getName( ItemMultiPart.class, getTypeByStack( is ).name() );
	}

	@Override
	public String getItemStackDisplayName(ItemStack is)
	{
		PartType pt = getTypeByStack( is );
		if ( pt == null )
			return "Unnamed";

		Enum[] varients = pt.getVarients();

		if ( varients != null )
			return super.getItemStackDisplayName( is ) + " - " + varients[dmgToPart.get( is.getItemDamage() ).varient].toString();

		if ( pt.getExtraName() != null )
			return super.getItemStackDisplayName( is ) + " - " + pt.getExtraName().getLocal();

		return super.getItemStackDisplayName( is );
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{
		for (Entry<Integer, PartTypeIst> part : dmgToPart.entrySet())
		{
			String tex = "appliedenergistics2:" + getname( new ItemStack( this, 1, part.getKey() ) );
			part.getValue().ico = par1IconRegister.registerIcon( tex );
		}
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		return AEApi.instance().partHelper().placeBus( is, x, y, z, side, player, w );
	}

	@Override
	public IPart createPartFromItemStack(ItemStack is)
	{
		try
		{
			PartType t = getTypeByStack( is );
			if ( t != null )
				return t.getPart().getConstructor( ItemStack.class ).newInstance( is );
		}
		catch (Throwable e)
		{
			throw new RuntimeException( "Unable to construct IBusPart from IBusItem : " + getTypeByStack( is ).getPart().getName()
					+ " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
		return null;
	}

	@Override
	public void getSubItems(Item number, CreativeTabs tab, List cList)
	{
		for (Entry<Integer, PartTypeIst> part : dmgToPart.entrySet())
			cList.add( new ItemStack( this, 1, part.getKey() ) );
	}

	public int varientOf(int itemDamage)
	{
		if ( dmgToPart.containsKey( itemDamage ) )
			return dmgToPart.get( itemDamage ).varient;
		return 0;
	}

	@Override
	public String getUnlocalizedGroupName(ItemStack is)
	{
		switch (getTypeByStack( is ))
		{
		case ImportBus:
		case ExportBus:
			return GuiText.IOBuses.getUnlocalized();
		default:
		}
		return null;
	}

	public ItemStack getStackFromTypeAndVarient(PartType mt, int varient)
	{
		return new ItemStack( this, 1, mt.baseDamage + varient );
	}
}
