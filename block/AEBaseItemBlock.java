package appeng.block;

import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.client.render.ItemRenderer;
import appeng.me.helpers.IGridProxyable;
import appeng.tile.AEBaseTile;
import appeng.util.Platform;

public class AEBaseItemBlock extends ItemBlock
{

	final AEBaseBlock blockType;

	public AEBaseItemBlock(int id) {
		super( id );
		blockType = (AEBaseBlock) Block.blocksList[id + 256];
		if ( Platform.isClient() )
			MinecraftForgeClient.registerItemRenderer( blockType.blockID, ItemRenderer.instance );
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List lines, boolean advancedItemTooltips)
	{
		Block blk = Block.blocksList[getBlockID()];
		if ( blk instanceof AEBaseBlock )
			((AEBaseBlock) blk).addInformation( is, player, lines, advancedItemTooltips );
	}

	@Override
	public boolean placeBlockAt(ItemStack stack, EntityPlayer player, World w, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int metadata)
	{
		ForgeDirection up = ForgeDirection.UNKNOWN;
		ForgeDirection forward = ForgeDirection.UNKNOWN;

		IOrientable ori = null;

		if ( blockType.hasBlockTileEntity() )
		{
			up = ForgeDirection.UP;

			byte rotation = (byte) (MathHelper.floor_double( (double) ((player.rotationYaw * 4F) / 360F) + 2.5D ) & 3);

			switch (rotation)
			{
			default:
			case 0:
				forward = ForgeDirection.SOUTH;
				break;
			case 1:
				forward = ForgeDirection.WEST;
				break;
			case 2:
				forward = ForgeDirection.NORTH;
				break;
			case 3:
				forward = ForgeDirection.EAST;
				break;
			}

			if ( player.rotationPitch > 65 )
			{
				up = forward.getOpposite();
				forward = ForgeDirection.UP;
			}
			else if ( player.rotationPitch < -65 )
			{
				up = forward.getOpposite();
				forward = ForgeDirection.DOWN;
			}
		}

		if ( blockType instanceof IOrientableBlock )
		{
			ori = ((IOrientableBlock) blockType).getOrientable( w, x, y, z );
			up = ForgeDirection.getOrientation( side );
			forward = ForgeDirection.SOUTH;
			if ( up.offsetY == 0 )
				forward = ForgeDirection.UP;

			ori.setOrientation( forward, up );
		}

		if ( !blockType.isValidOrientation( w, x, y, z, forward, up ) )
			return false;

		if ( super.placeBlockAt( stack, player, w, x, y, z, side, hitX, hitY, hitZ, metadata ) )
		{
			if ( blockType.hasBlockTileEntity() )
			{
				AEBaseTile tile = blockType.getTileEntity( w, x, y, z );
				ori = tile;

				if ( tile == null )
					return true;

				if ( ori.canBeRotated() )
				{
					if ( ori.getForward() == null || ori.getUp() == null || // null
							tile.getForward() == ForgeDirection.UNKNOWN || ori.getUp() == ForgeDirection.UNKNOWN )
						ori.setOrientation( forward, up );
				}

				if ( tile instanceof IGridProxyable )
				{
					((IGridProxyable) tile).getProxy().setOwner( player );
				}

				tile.onPlacement( stack, player, side );
			}
			else if ( blockType instanceof IOrientableBlock )
			{
				ori.setOrientation( forward, up );
			}

			return true;
		}
		return false;
	}

	@Override
	public boolean isBookEnchantable(ItemStack itemstack1, ItemStack itemstack2)
	{
		return false;
	}

}
