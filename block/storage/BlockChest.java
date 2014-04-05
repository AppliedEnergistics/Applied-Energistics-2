package appeng.block.storage;

import java.util.EnumSet;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.blocks.RenderMEChest;
import appeng.core.features.AEFeature;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.GuiBridge;
import appeng.tile.storage.TileChest;
import appeng.util.Platform;

public class BlockChest extends AEBaseBlock
{

	public BlockChest() {
		super( BlockChest.class, Material.iron );
		setfeature( EnumSet.of( AEFeature.StorageCells, AEFeature.MEChest ) );
		setTileEntiy( TileChest.class );
	}

	@Override
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return RenderMEChest.class;
	}

	@Override
	public boolean onActivated(World w, int x, int y, int z, EntityPlayer p, int side, float hitX, float hitY, float hitZ)
	{
		TileChest tg = getTileEntity( w, x, y, z );
		if ( tg != null && !p.isSneaking() )
		{
			if ( Platform.isClient() )
				return true;

			if ( side != tg.getUp().ordinal() )
			{
				Platform.openGUI( p, tg, ForgeDirection.getOrientation( side ), GuiBridge.GUI_CHEST );
			}
			else
			{
				ItemStack cell = tg.getStackInSlot( 1 );
				if ( cell != null )
				{
					ICellHandler ch = AEApi.instance().registries().cell().getHandler( cell );

					tg.openGui( p, ch, cell, side );
				}
				else
					p.addChatMessage( PlayerMessages.ChestCannotReadStorageCell.get() );
			}

			return true;
		}

		return false;
	}
}
