package appeng.me.storage;

import appeng.api.AEApi;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.SecurityPermissions;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.networking.security.BaseActionSource;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.IMEInventoryHandler;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IItemList;
import appeng.me.GridAccessException;
import appeng.tile.misc.TileSecurity;
import appeng.util.item.ItemList;

public class SecurityInventory implements IMEInventoryHandler<IAEItemStack>
{

	final TileSecurity securityTile;
	final public IItemList<IAEItemStack> storedItems = new ItemList();

	public SecurityInventory(TileSecurity ts) {
		securityTile = ts;
	}

	private boolean hasPermission(BaseActionSource src)
	{
		if ( src.isPlayer() )
		{
			try
			{
				return securityTile.getProxy().getSecurity().hasPermission( ((PlayerSource) src).player, SecurityPermissions.SECURITY );
			}
			catch (GridAccessException e)
			{
				// :P
			}
		}
		return false;
	}

	@Override
	public IAEItemStack injectItems(IAEItemStack input, Actionable type, BaseActionSource src)
	{
		if ( hasPermission( src ) && AEApi.instance().items().itemBiometricCard.sameAs( input.getItemStack() ) )
		{
			if ( canAccept( input ) )
			{
				if ( type == Actionable.SIMULATE )
					return null;

				storedItems.add( input );
				securityTile.inventoryChanged();
				return null;
			}
		}
		return input;
	}

	@Override
	public IAEItemStack extractItems(IAEItemStack request, Actionable mode, BaseActionSource src)
	{
		if ( hasPermission( src ) )
		{
			IAEItemStack target = storedItems.findPrecise( request );
			if ( target != null )
			{
				IAEItemStack output = target.copy();

				if ( mode == Actionable.SIMULATE )
					return output;

				target.setStackSize( 0 );
				securityTile.inventoryChanged();
				return output;
			}
		}
		return null;
	}

	@Override
	public IItemList<IAEItemStack> getAvailableItems(IItemList out)
	{
		for (IAEItemStack ais : storedItems)
			out.add( ais );

		return out;
	}

	@Override
	public StorageChannel getChannel()
	{
		return StorageChannel.ITEMS;
	}

	@Override
	public AccessRestriction getAccess()
	{
		return AccessRestriction.READ_WRITE;
	}

	@Override
	public boolean isPrioritized(IAEItemStack input)
	{
		return false;
	}

	@Override
	public boolean canAccept(IAEItemStack input)
	{
		if ( input.getItem() instanceof IBiometricCard )
		{
			IBiometricCard tbc = (IBiometricCard) input.getItem();
			String newUser = tbc.getUsername( input.getItemStack() );

			for (IAEItemStack ais : storedItems)
			{
				if ( ais.isMeaninful() )
				{
					String thisUser = tbc.getUsername( ais.getItemStack() );
					if ( thisUser.equals( newUser ) )
						return false;
				}
			}

			return true;
		}
		return false;
	}

	@Override
	public int getPriority()
	{
		return 0;
	}

	@Override
	public int getSlot()
	{
		return 0;
	}

}
