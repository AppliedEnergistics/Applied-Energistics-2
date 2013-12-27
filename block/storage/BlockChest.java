package appeng.block.storage;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.storage.ICellHandler;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
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
			else if ( tg.isPowered() )
			{
				ItemStack cell = tg.getStackInSlot( 1 );
				if ( cell != null )
				{
					ICellHandler ch = AEApi.instance().registries().cell().getHander( cell );

					List<IMEInventoryHandler> ih = tg.getCellArray( StorageChannel.ITEMS );
					if ( ch != null && ih != null && ih.size() == 1 )
					{
						IMEInventoryHandler mine = ih.get( 0 );
						ch.openChestGui( p, tg, ch, mine, cell, StorageChannel.ITEMS );
						return true;
					}

					List<IMEInventoryHandler> fh = tg.getCellArray( StorageChannel.FLUIDS );
					if ( ch != null && fh != null && ih.size() == 1 )
					{
						IMEInventoryHandler mine = fh.get( 0 );
						ch.openChestGui( p, tg, ch, mine, cell, StorageChannel.FLUIDS );
						return true;
					}
				}

				p.sendChatToPlayer( PlayerMessages.ChestCannotReadStorageCell.get() );
			}
			else
				p.sendChatToPlayer( PlayerMessages.MachineNotPowered.get() );

			return true;
		}

		return false;
	}
}
