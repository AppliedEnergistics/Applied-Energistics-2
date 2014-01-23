package appeng.items.parts;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Icon;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.implementations.items.IItemGroup;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.localization.GuiText;
import appeng.items.AEBaseItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPart extends AEBaseItem implements IPartItem, IItemGroup
{

	class PartTypeIst
	{

		PartType part;
		int varient;

		@SideOnly(Side.CLIENT)
		Icon ico;

	};

	HashMap<Integer, PartTypeIst> dmgToPart = new HashMap();

	public ItemPart() {
		super( ItemPart.class );
		setfeature( EnumSet.of( AEFeature.Core ) );
		AEApi.instance().partHelper().setItemBusRenderer( this );
		setHasSubtypes( true );
	}

	public ItemStack createPart(PartType mat, Enum varient)
	{
		String name = varient == null ? mat.name() : mat.name() + "." + varient.name();
		int varID = varient == null ? 0 : varient.ordinal();

		// verify
		for (PartTypeIst p : dmgToPart.values())
		{
			if ( p.part == mat && p.varient == varID )
				throw new RuntimeException( "Cannot create the same material twice..." );
		}

		boolean enabled = true;
		for (AEFeature f : mat.getFeature())
			enabled = enabled && Configuration.instance.isFeatureEnabled( f );

		if ( enabled )
		{
			int newPartNum = Configuration.instance.get( "parts", name, Configuration.instance.getFreePart() ).getInt();
			ItemStack output = new ItemStack( this );
			output.setItemDamage( newPartNum );

			PartTypeIst pti = new PartTypeIst();
			pti.part = mat;
			pti.varient = varID;

			dmgToPart.put( newPartNum, pti );
			return output;
		}

		return null;
	}

	public PartType getTypeByStack(ItemStack is)
	{
		return dmgToPart.get( is.getItemDamage() ).part;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	public Icon getIconFromDamage(int dmg)
	{
		Icon ico = dmgToPart.get( dmg ).ico;
		return ico;
	}

	@Override
	public String getUnlocalizedName(ItemStack is)
	{
		return "item.appliedenergistics2." + getname( is );
	}

	public String getname(ItemStack is)
	{
		return AEFeatureHandler.getName( ItemPart.class, getTypeByStack( is ).name() );
	}

	@Override
	public String getItemDisplayName(ItemStack is)
	{
		Enum[] varients = getTypeByStack( is ).getVarients();

		if ( varients != null )
			return super.getItemDisplayName( is ) + " - " + varients[dmgToPart.get( is.getItemDamage() ).varient].toString();

		return super.getItemDisplayName( is );
	}

	@Override
	public void registerIcons(IconRegister par1IconRegister)
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
			return t.getPart().getConstructor( ItemStack.class ).newInstance( is );
		}
		catch (Throwable e)
		{
			throw new RuntimeException( "Unable to construct IBusPart from IBusItem : " + getTypeByStack( is ).getPart().getName()
					+ " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
	}

	@Override
	public void getSubItems(int number, CreativeTabs tab, List cList)
	{
		for (Entry<Integer, PartTypeIst> part : dmgToPart.entrySet())
			cList.add( new ItemStack( this, 1, part.getKey() ) );
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

}
