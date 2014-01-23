package appeng.items.tools;

import java.util.EnumSet;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.items.IAEWrench;
import appeng.api.networking.IGridHost;
import appeng.core.features.AEFeature;
import appeng.core.sync.GuiBridge;
import appeng.helpers.NetworkToolViewer;
import appeng.items.AEBaseItem;
import appeng.util.Platform;
import buildcraft.api.tools.IToolWrench;
import cpw.mods.fml.common.Optional.Interface;

@Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraftAPI|core")
public class ToolNetworkTool extends AEBaseItem implements IGuiItem, IAEWrench, IToolWrench
{

	public ToolNetworkTool() {
		super( ToolNetworkTool.class, null );
		setfeature( EnumSet.of( AEFeature.NetworkTool ) );
	}

	@Override
	public Object getGuiObject(ItemStack is, World world, int x, int y, int z)
	{
		TileEntity te = world.getBlockTileEntity( x, y, z );
		return new NetworkToolViewer( is, (IGridHost) (te instanceof IGridHost ? te : null) );
	}

	@Override
	public boolean onItemUseFirst(ItemStack is, EntityPlayer player, World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ)
	{
		int id = world.getBlockId( x, y, z );
		if ( id > 0 )
		{
			Block b = Block.blocksList[id];
			TileEntity te = world.getBlockTileEntity( x, y, z );
			if ( b != null && !(te instanceof IGridHost) )
			{
				if ( b.rotateBlock( world, x, y, z, ForgeDirection.getOrientation( side ) ) )
				{
					player.swingItem();
					return !world.isRemote;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onItemUse(ItemStack is, EntityPlayer p, World w, int x, int y, int z, int side, float hitx, float hity, float hitz)
	{
		if ( Platform.isClient() )
			return false;

		if ( !p.isSneaking() )
		{
			TileEntity te = w.getBlockTileEntity( x, y, z );
			if ( te instanceof IGridHost )
				Platform.openGUI( p, te, ForgeDirection.getOrientation( side ), GuiBridge.GUI_NETWORK_STATUS );
			else
				Platform.openGUI( p, null, ForgeDirection.UNKNOWN, GuiBridge.GUI_NETWORK_TOOL );
		}

		return false;
	}

	@Override
	public boolean shouldPassSneakingClickToBlock(World w, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean canWrench(ItemStack is, EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public boolean canWrench(EntityPlayer player, int x, int y, int z)
	{
		return true;
	}

	@Override
	public void wrenchUsed(EntityPlayer player, int x, int y, int z)
	{
		player.swingItem();
	}

}
