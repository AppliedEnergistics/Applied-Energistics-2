package appeng.core.sync.packets;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IEnergyGrid;
import appeng.api.networking.security.ISecurityGrid;
import appeng.api.networking.storage.IStorageGrid;
import appeng.api.storage.IMEMonitor;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.ContainerNull;
import appeng.core.sync.AppEngPacket;
import appeng.core.sync.network.INetworkInfo;
import appeng.helpers.IContainerCraftingPacket;
import appeng.util.Platform;
import appeng.util.item.AEItemStack;

public class PacketNEIRecipe extends AppEngPacket
{

	ItemStack[] recipe;

	// automatic.
	public PacketNEIRecipe(ByteBuf stream) throws IOException {
		ByteArrayInputStream bytes = new ByteArrayInputStream( stream.array() );
		bytes.skip( stream.readerIndex() );
		NBTTagCompound comp = CompressedStreamTools.readCompressed( bytes );
		if ( comp != null )
		{
			recipe = new ItemStack[9];
			for (int x = 0; x < recipe.length; x++)
			{
				recipe[x] = ItemStack.loadItemStackFromNBT( comp.getCompoundTag( "#" + x ) );
			}
		}
	}

	@Override
	public void serverPacketData(INetworkInfo manager, AppEngPacket packet, EntityPlayer player)
	{
		EntityPlayerMP pmp = (EntityPlayerMP) player;
		Container con = pmp.openContainer;

		if ( con != null && con instanceof IContainerCraftingPacket )
		{
			IContainerCraftingPacket cct = (IContainerCraftingPacket) con;
			IGridNode node = cct.getNetworkNode();
			if ( node != null )
			{
				IGrid grid = node.getGrid();
				if ( grid == null )
					return;

				IStorageGrid inv = grid.getCache( IStorageGrid.class );
				IEnergyGrid energy = grid.getCache( IEnergyGrid.class );
				ISecurityGrid security = grid.getCache( ISecurityGrid.class );
				IInventory craftMatrix = cct.getInventoryByName( "crafting" );

				Actionable realForFake = cct.useRealItems() ? Actionable.MODULATE : Actionable.SIMULATE;

				if ( inv != null && recipe != null && security != null )
				{
					InventoryCrafting ic = new InventoryCrafting( new ContainerNull(), 3, 3 );
					for (int x = 0; x < 9; x++)
						ic.setInventorySlotContents( x, recipe[x] );

					IRecipe r = Platform.findMatchingRecipe( ic, pmp.worldObj );

					if ( r != null && security.hasPermission( player, SecurityPermissions.EXTRACT ) )
					{
						ItemStack is = r.getCraftingResult( ic );

						if ( is != null )
						{
							IMEMonitor<IAEItemStack> stor = inv.getItemInventory();
							IItemList all = stor.getStorageList();

							for (int x = 0; x < craftMatrix.getSizeInventory(); x++)
							{
								ItemStack PatternItem = ic.getStackInSlot( x );

								ItemStack currentItem = craftMatrix.getStackInSlot( x );
								if ( currentItem != null )
								{
									ic.setInventorySlotContents( x, currentItem );
									ItemStack newis = r.matches( ic, pmp.worldObj ) ? r.getCraftingResult( ic ) : null;
									ic.setInventorySlotContents( x, PatternItem );

									if ( newis == null || !Platform.isSameItemPrecise( newis, is ) )
									{
										IAEItemStack in = AEItemStack.create( currentItem );
										if ( in != null )
										{
											IAEItemStack out = realForFake == Actionable.SIMULATE ? null : Platform.poweredInsert( energy, stor, in,
													cct.getSource() );
											if ( out != null )
												craftMatrix.setInventorySlotContents( x, out.getItemStack() );
											else
												craftMatrix.setInventorySlotContents( x, null );

											currentItem = craftMatrix.getStackInSlot( x );
										}
									}
								}

								if ( PatternItem != null && currentItem == null )
								{
									craftMatrix.setInventorySlotContents( x, Platform.extractItemsByRecipe( energy, cct.getSource(), stor, player.worldObj, r,
											is, ic, PatternItem, x, all, realForFake ) );
								}
							}
							con.onCraftMatrixChanged( craftMatrix );
						}
					}
				}
			}
		}
	}

	// api
	public PacketNEIRecipe(NBTTagCompound recipe) throws IOException {
		ByteBuf data = Unpooled.buffer();

		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		DataOutputStream datao = new DataOutputStream( bytes );

		data.writeInt( getPacketID() );

		CompressedStreamTools.writeCompressed( recipe, datao );
		data.writeBytes( bytes.toByteArray() );

		configureWrite( data );
	}
}
