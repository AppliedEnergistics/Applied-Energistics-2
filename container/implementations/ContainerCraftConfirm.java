package appeng.container.implementations;

import java.util.concurrent.Future;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;
import net.minecraft.util.ChatComponentText;
import net.minecraft.world.World;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.IActionHost;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ITerminalHost;
import appeng.container.AEBaseContainer;
import appeng.core.AELog;
import appeng.crafting.CraftingJob;
import appeng.crafting.ICraftingHost;

public class ContainerCraftConfirm extends AEBaseContainer implements ICraftingHost
{

	ITerminalHost priHost;
	public Future<CraftingJob> job;
	public CraftingJob result;

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
				AELog.info( "Job info is ready!" );
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

	@Override
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
