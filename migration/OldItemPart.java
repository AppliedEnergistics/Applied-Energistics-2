package appeng.migration;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.AEConfig;
import appeng.core.features.AEFeature;
import appeng.core.features.ItemStackSrc;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import appeng.items.parts.ItemMultiPart;
import appeng.items.parts.PartType;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class OldItemPart extends AEBaseItem implements IPartItem, IItemGroup, IItemMigrate
{

	class OldPartTypeIst
	{

		OldPartType part;
		int varient;

		@SideOnly(Side.CLIENT)
		IIcon ico;

	};

	HashMap<Integer, OldPartTypeIst> dmgToPart = new HashMap();

	public static OldItemPart instance;

	public OldItemPart() {
		super( OldItemPart.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
		AEApi.instance().partHelper().setItemBusRenderer( this );
		setHasSubtypes( true );
		instance = this;

		for (OldPartType omt : OldPartType.values())
		{
			if ( omt.getVarients() == null )
				createPart( omt, null );
			else
			{
				for (Enum e : omt.getVarients())
					createPart( omt, e );
			}
		}
	}

	public ItemStackSrc createPart(OldPartType mat, Enum varient)
	{
		try
		{
			// I think this still works?
			ItemStack is = new ItemStack( this );
			mat.getPart().getConstructor( ItemStack.class ).newInstance( is );
		}
		catch (Throwable e)
		{
			e.printStackTrace();
			return null; // part not supported..
		}

		String name = varient == null ? mat.name() : mat.name() + "." + varient.name();
		int varID = varient == null ? 0 : varient.ordinal();

		// verify
		for (OldPartTypeIst p : dmgToPart.values())
		{
			if ( p.part == mat && p.varient == varID )
				throw new RuntimeException( "Cannot create the same material twice..." );
		}

		boolean enabled = true;
		for (AEFeature f : mat.getFeature())
			enabled = enabled && AEConfig.instance.isFeatureEnabled( f );

		if ( enabled )
		{
			int newPartNum = AEConfig.instance.get( "parts", name, AEConfig.instance.getFreePart( mat.ordinal() * 32 + varID ) ).getInt();
			ItemStackSrc output = new ItemStackSrc( this, newPartNum );

			OldPartTypeIst pti = new OldPartTypeIst();
			pti.part = mat;
			pti.varient = varID;

			dmgToPart.put( newPartNum, pti );
			return output;
		}

		return null;
	}

	public int getDamageByType(OldPartType t)
	{
		for (Entry<Integer, OldPartTypeIst> pt : dmgToPart.entrySet())
		{
			if ( pt.getValue().part == t )
				return pt.getKey();
		}
		return -1;
	}

	public OldPartType getTypeByStack(ItemStack is)
	{
		if ( is == null )
			return null;

		OldPartTypeIst pt = dmgToPart.get( is.getItemDamage() );
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
		return "PART";
	}

	@Override
	public String getItemStackDisplayName(ItemStack par1ItemStack)
	{
		return "AE2-OLD-PART";
	}

	@Override
	public void registerIcons(IIconRegister par1IconRegister)
	{

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
			OldPartType t = getTypeByStack( is );
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

	}

	public int varientOf(int itemDamage)
	{
		return dmgToPart.get( itemDamage ).varient;
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

	@Override
	public void modifyItemStack(ItemStack is)
	{
		if ( is.getItem() == this )
		{
			OldPartTypeIst omt = dmgToPart.get( is.getItemDamage() );
			for (PartType mt : PartType.values())
			{
				if ( omt != null && omt.part != null && mt.toString().equals( omt.part.toString() ) )
				{
					ItemStack newStack = ItemMultiPart.instance.getStackFromTypeAndVarient( mt, omt.varient );
					if ( newStack != null )
					{
						NBTTagCompound tmp = new NBTTagCompound();
						newStack.stackSize = is.stackSize;
						newStack.setTagCompound( is.getTagCompound() );

						// write then read...
						newStack.writeToNBT( tmp );
						is.readFromNBT( tmp );
						return;
					}
				}
			}
		}
	}

}
