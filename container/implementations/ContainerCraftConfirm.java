package appeng.container.implementations;

import java.io.IOException;
import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.container.AEBaseContainer;
import appeng.container.guisync.GuiSync;
import appeng.core.AELog;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMEInventoryUpdate;
import appeng.crafting.CraftingJob;
import appeng.crafting.ICraftingHost;

public class ContainerCraftConfirm extends AEBaseContainer implements ICraftingHost
{

	ITerminalHost priHost;
	public Future<CraftingJob> job;
	public CraftingJob result;

	@GuiSync(0)
	public long bytesUsed;

	@GuiSync(1)
	public long cpuBytesAvail;

	@GuiSync(2)
	public int cpuCoProcessors;

	public ContainerCraftConfirm(InventoryPlayer ip, ITerminalHost te) {
		super( ip, te );
		priHost = te;
	}

	@Override
	public void detectAndSendChanges()
	{
		super.detectAndSendChanges();
		if ( job != null && job.isDone() )
		{
			try
			{
				result = job.get();
				if ( !result.isSimulation() )
				{
					try
					{
						PacketMEInventoryUpdate a = new PacketMEInventoryUpdate( (byte) 0 );
						PacketMEInventoryUpdate b = new PacketMEInventoryUpdate( (byte) 1 );

						IItemList<IAEItemStack> plan = AEApi.instance().storage().createItemList();
						result.tree.getPlan( plan );

						bytesUsed = result.getByteTotal();

						for (IAEItemStack out : plan)
						{
							IAEItemStack o = out.copy();
							o.reset();
							o.setStackSize( out.getStackSize() );

							IAEItemStack p = out.copy();
							p.reset();
							p.setStackSize( out.getCountRequestable() );

							if ( o.getStackSize() > 0 )
								a.appendItem( o );

							if ( p.getStackSize() > 0 )
								b.appendItem( p );
						}

						for (Object g : this.crafters)
						{
							if ( g instanceof EntityPlayer )
							{
								NetworkHandler.instance.sendTo( a, (EntityPlayerMP) g );
								NetworkHandler.instance.sendTo( b, (EntityPlayerMP) g );
							}
						}
					}
					catch (IOException e)
					{
						// :P
					}

					// CraftingCache cc = getGrid().getCache( CraftingCache.class );
					// cc.submitJob( result, null, getActionSrc() );
					// AELog.info( "Job info is ready!" );
					// this.isContainerValid = false;
				}
			}
			catch (Throwable e)
			{
				getPlayerInv().player.addChatMessage( new ChatComponentText( "Error: " + e.toString() ) );
				AELog.error( e );
				this.isContainerValid = false;
				result = null;
			}

			job = null;
		}
		verifyPermissions( SecurityPermissions.CRAFT, false );
	}

	@Override
	public void onContainerClosed(EntityPlayer par1EntityPlayer)
	{
		super.onContainerClosed( par1EntityPlayer );
		if ( job != null )
		{
			job.cancel( true );
			job = null;
		}
	}

	@Override
	public void removeCraftingFromCrafters(ICrafting c)
	{
		super.removeCraftingFromCrafters( c );
		if ( job != null )
		{
			job.cancel( true );
			job = null;
		}
	}

	@Override
	public IGrid getGrid()
	{
		IActionHost h = ((IActionHost) this.getTarget());
		return h.getActionableNode().getGrid();
	}

	public World getWorld()
	{
		return getPlayerInv().player.worldObj;
	}

	@Override
	public BaseActionSource getActionSrc()
	{
		return new PlayerSource( getPlayerInv().player, (IActionHost) getTarget() );
	}

}
