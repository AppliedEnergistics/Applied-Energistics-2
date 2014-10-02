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

	private final GrinderRecipeManager GrinderRecipes = new GrinderRecipeManager();
	private final ExternalStorageRegistry ExternalStorageHandlers = new ExternalStorageRegistry();
	private final CellRegistry CellRegistry = new CellRegistry();
	private final LocatableRegistry LocatableRegistry = new LocatableRegistry();
	private final SpecialComparisonRegistry SpecialComparisonRegistry = new SpecialComparisonRegistry();
	private final WirelessRegistry WirelessRegistry = new WirelessRegistry();
	private final GridCacheRegistry GridCacheRegistry = new GridCacheRegistry();
	private final P2PTunnelRegistry P2PRegistry = new P2PTunnelRegistry();
	private final MovableTileRegistry MovableReg = new MovableTileRegistry();
	private final MatterCannonAmmoRegistry matterCannonReg = new MatterCannonAmmoRegistry();
	private final PlayerRegistry playerRegistry = new PlayerRegistry();
	private final IRecipeHandlerRegistry recipeReg = new RecipeHandlerRegistry();

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
		return SpecialComparisonRegistry;
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
		return MovableReg;
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
		return playerRegistry;
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
