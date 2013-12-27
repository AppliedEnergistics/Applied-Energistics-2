package appeng.items.parts;

import java.util.List;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.parts.IPartItem;
import appeng.api.parts.IPart;
import appeng.items.AEBaseItem;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemPart extends AEBaseItem implements IPartItem
{

	final PartType part;

	public ItemPart(PartType type) {
		super( ItemPart.class, type.name() );
		setfeature( type.getFeature() );
		AEApi.instance().partHelper().setItemBusRenderer( this );
		part = type;
		if ( type == PartType.CableSmart || type == PartType.CableCovered || type == PartType.CableGlass )
		{
			setHasSubtypes( true );
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getSpriteNumber()
	{
		return 0;
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY,
			float hitZ)
	{
		return AEApi.instance().partHelper().placeBus( is, x, y, z, side, player, w );
	}

	@Override
	public IPart createPartFromItemStack(ItemStack is)
	{
		try
		{
			return part.getPart().getConstructor( ItemStack.class ).newInstance( is );
		}
		catch (Throwable e)
		{
			throw new RuntimeException( "Unable to construct IBusPart from IBusItem : " + part.getPart().getName()
					+ " ; Possibly didn't have correct constructor( ItemStack )", e );
		}
	}

	@Override
	public void getSubItems(int number, CreativeTabs tab, List list)
	{
		if ( part == PartType.CableSmart || part == PartType.CableCovered || part == PartType.CableGlass )
		{
			list.add( new ItemStack( this, 1, 16 ) );
			for (int x = 0; x < 16; x++)
				list.add( new ItemStack( this, 1, x ) );
			return;
		}
		super.getSubItems( number, tab, list );
	}

}
