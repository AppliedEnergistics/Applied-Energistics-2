package appeng.me.cache;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import appeng.api.config.SecurityPermissions;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridCache;
import appeng.api.networking.IGridHost;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridStorage;
import appeng.api.networking.events.MENetworkEventSubscribe;
import appeng.api.networking.events.MENetworkSecurityChange;
import appeng.api.networking.security.ISecurityGrid;
import appeng.core.WorldSettings;
import appeng.me.GridNode;
import appeng.tile.misc.TileSecurity;

public class SecurityCache implements IGridCache, ISecurityGrid
{

	private List<TileSecurity> securityProvider = new ArrayList();
	private HashMap<Integer, EnumSet<SecurityPermissions>> playerPerms = new HashMap<Integer, EnumSet<SecurityPermissions>>();

	public SecurityCache(IGrid g) {
		myGrid = g;
	}

	private long securityKey = -1;
	public final IGrid myGrid;

	@MENetworkEventSubscribe
	public void updatePermissions(MENetworkSecurityChange ev)
	{
		playerPerms.clear();
		if ( securityProvider.isEmpty() )
			return;

		securityProvider.get( 0 ).readPermissions( playerPerms );
	}

	public long getSecurityKey()
	{
		return securityKey;
	}

	@Override
	public boolean isAvailable()
	{
		return securityProvider.size() == 1 && securityProvider.get( 0 ).isSecurityEnabled();
	}

	@Override
	public boolean hasPermission(EntityPlayer player, SecurityPermissions perm)
	{
		return hasPermission( player == null ? -1 : WorldSettings.getInstance().getPlayerID( player.getCommandSenderName() ), perm );
	}

	@Override
	public boolean hasPermission(int playerID, SecurityPermissions perm)
	{
		if ( isAvailable() )
		{
			EnumSet<SecurityPermissions> perms = playerPerms.get( playerID );

			if ( perms == null )
			{
				if ( playerID == -1 ) // no default?
					return false;
				else
					return hasPermission( -1, perm );
			}

			return perms.contains( perm );
		}
		return true;
	}

	private void updateSecurityKey()
	{
		long lastCode = securityKey;

		if ( securityProvider.size() == 1 )
			securityKey = securityProvider.get( 0 ).securityKey;
		else
			securityKey = -1;

		if ( lastCode != securityKey )
		{
			myGrid.postEvent( new MENetworkSecurityChange() );
			for (IGridNode n : myGrid.getNodes())
				((GridNode) n).lastSecurityKey = securityKey;
		}
	}

	@Override
	public void removeNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof TileSecurity )
		{
			securityProvider.remove( (TileSecurity) machine );
			updateSecurityKey();
		}
	}

	@Override
	public void addNode(IGridNode gridNode, IGridHost machine)
	{
		if ( machine instanceof TileSecurity )
		{
			securityProvider.add( (TileSecurity) machine );
			updateSecurityKey();
		}
		else
			((GridNode) gridNode).lastSecurityKey = securityKey;
	}

	@Override
	public void onUpdateTick()
	{

	}

	@Override
	public void onSplit(IGridStorage destinationStorage)
	{

	}

	@Override
	public void onJoin(IGridStorage sourceStorage)
	{

	}

	@Override
	public void populateGridStorage(IGridStorage destinationStorage)
	{

	}

	@Override
	public int getOwner()
	{
		if ( isAvailable() )
			return securityProvider.get( 0 ).getOwner();
		return -1;
	}

}
