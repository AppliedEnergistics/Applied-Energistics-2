package appeng.core.features.registries;

import appeng.api.features.IGrinderRegistry;
import appeng.api.features.ILocatableRegistry;
import appeng.api.features.IMatterCannonAmmoRegistry;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.features.IPlayerRegistry;
import appeng.api.features.IRecipeHandlerRegistry;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.ISpecialComparisonRegistry;
import appeng.api.features.IWirelessTermRegistry;
import appeng.api.features.IWorldGen;
import appeng.api.movable.IMovableRegistry;
import appeng.api.networking.IGridCacheRegistry;
import appeng.api.storage.ICellRegistry;
import appeng.api.storage.IExternalStorageRegistry;

public class RegistryContainer implements IRegistryContainer
{

	private GrinderRecipeManager GrinderRecipes = new GrinderRecipeManager();
	private ExternalStorageRegistry ExternalStorageHandlers = new ExternalStorageRegistry();
	private CellRegistry CellRegistry = new CellRegistry();
	private LocatableRegistry LocatableRegistry = new LocatableRegistry();
	private SpecialComparisonRegistry SpecialComparsonRegistry = new SpecialComparisonRegistry();
	private WirelessRegistry WirelessRegistry = new WirelessRegistry();
	private GridCacheRegistry GridCacheRegistry = new GridCacheRegistry();
	private P2PTunnelRegistry P2PRegistry = new P2PTunnelRegistry();
	private MovableTileRegistry MoveableReg = new MovableTileRegistry();
	private MatterCannonAmmoRegistry matterCannonReg = new MatterCannonAmmoRegistry();
	private PlayerRegistry playerreg = new PlayerRegistry();
	private IRecipeHandlerRegistry recipeReg = new RecipeHandlerRegistry();

	@Override
	public IWirelessTermRegistry wireless()
	{
		return WirelessRegistry;
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
	public ISpecialComparisonRegistry specialComparison()
	{
		return SpecialComparsonRegistry;
	}

	@Override
	public IExternalStorageRegistry externalStorage()
	{
		return ExternalStorageHandlers;
	}

	@Override
	public ILocatableRegistry locatable()
	{
		return LocatableRegistry;
	}

	@Override
	public IGridCacheRegistry gridCache()
	{
		return GridCacheRegistry;
	}

	@Override
	public IMovableRegistry movable()
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

	@Override
	public IRecipeHandlerRegistry recipes()
	{
		return recipeReg;
	}

	@Override
	public IWorldGen worldgen()
	{
		return WorldGenRegistry.instance;
	}

}
