package appeng.core.features.registries;

import appeng.api.features.IGrinderRegistry;
import appeng.api.features.ILocateableRegistry;
import appeng.api.features.IMatterCannonAmmoRegistry;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.features.IPlayerRegistry;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.ISpecialComparisonRegistry;
import appeng.api.features.IWirelessTermRegistery;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IExternalStorageRegistry;

public class RegistryContainer implements IRegistryContainer
{

	private GrinderRecipeManager GrinderRecipes = new GrinderRecipeManager();
	private ExternalStorageRegistry ExternalStorageHandlers = new ExternalStorageRegistry();
	private CellRegistry CellRegistry = new CellRegistry();
	private LocateableRegistry LocateableRegistry = new LocateableRegistry();
	private SpecialComparisonRegistry SpecialComparsonRegistry = new SpecialComparisonRegistry();
	private WirelessRegistry WirelessRegistery = new WirelessRegistry();
	private GridCacheRegistry GridCacheRegistry = new GridCacheRegistry();
	private P2PTunnelRegistry P2PRegistry = new P2PTunnelRegistry();
	private MovableTileRegistry MoveableReg = new MovableTileRegistry();
	private MatterCannonAmmoRegistry matterCannonReg = new MatterCannonAmmoRegistry();
	private PlayerRegistry playerreg = new PlayerRegistry();

	@Override
	public IWirelessTermRegistery wireless()
	{
		return WirelessRegistery;
	}

	@Override
	public ICellRegistry cell()
	{
		return CellRegistry;
	}

	@Override
	public IGrinderRegistry grinder()
	{
		return GrinderRecipes;
	}

	@Override
	public ISpecialComparisonRegistry specialComparson()
	{
		return SpecialComparsonRegistry;
	}

	@Override
	public IExternalStorageRegistry externalStorage()
	{
		return ExternalStorageHandlers;
	}

	@Override
	public ILocateableRegistry locateable()
	{
		return LocateableRegistry;
	}

	@Override
	public IGridCacheRegistry gridCache()
	{
		return GridCacheRegistry;
	}

	@Override
	public IMovableRegistry moveable()
	{
		return MoveableReg;
	}

	@Override
	public IP2PTunnelRegistry p2pTunnel()
	{
		return P2PRegistry;
	}

	@Override
	public IMatterCannonAmmoRegistry matterCannon()
	{
		return matterCannonReg;
	}

	@Override
	public IPlayerRegistry players()
	{
		return playerreg;
	}

}
