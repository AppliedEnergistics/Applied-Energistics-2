package appeng.core;

import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.IAppEngApi;
import appeng.api.definitions.Blocks;
import appeng.api.definitions.Items;
import appeng.api.definitions.Materials;
import appeng.api.definitions.Parts;
import appeng.api.exceptions.FailedConnection;
import appeng.api.features.IRegistryContainer;
import appeng.api.networking.IGridBlock;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.parts.IPartHelper;
import appeng.api.storage.IStorageHelper;
import appeng.core.api.ApiPart;
import appeng.core.api.ApiStorage;
import appeng.core.features.registries.RegistryContainer;
import appeng.me.GridConnection;
import appeng.me.GridNode;
import appeng.util.Platform;

public class Api implements IAppEngApi
{

	public static final Api instance = new Api();

	private Api() {

	}

	// private MovableTileRegistry MovableRegistry = new MovableTileRegistry();
	private final RegistryContainer rc = new RegistryContainer();
	private final ApiStorage storageHelper = new ApiStorage();
	
	public final ApiPart partHelper = new ApiPart();

	private final Materials materials = new Materials();
	private final Items items = new Items();
	private final Blocks blocks = new Blocks();
	private final Parts parts = new Parts();

	@Override
	public IRegistryContainer registries()
	{
		return rc;
	}

	@Override
	public Items items()
	{
		return items;
	}

	@Override
	public Materials materials()
	{
		return materials;
	}

	@Override
	public Blocks blocks()
	{
		return blocks;
	}

	@Override
	public Parts parts()
	{
		return parts;
	}

	@Override
	public IStorageHelper storage()
	{
		return storageHelper;
	}

	@Override
	public IPartHelper partHelper()
	{
		return partHelper;
	}

	@Override
	public IGridNode createGridNode(IGridBlock blk)
	{
		if ( Platform.isClient() )
			throw new RuntimeException( "Grid Features are Server Side Only." );
		return new GridNode( blk );
	}

	@Override
	public IGridConnection createGridConnection(IGridNode a, IGridNode b) throws FailedConnection
	{
		return new GridConnection( a, b, ForgeDirection.UNKNOWN );
	}

}
