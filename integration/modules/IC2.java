package appeng.integration.modules;

import ic2.api.energy.tile.IEnergyTile;
import ic2.api.recipe.RecipeInputItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.AEApi;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IIC2;

public class IC2 implements IIntegrationModule, IIC2
{

	public static IC2 instance;

	@Override
	public void Init()
	{

	}

	@Override
	public void PostInit()
	{
		AEApi.instance().registries().matterCannon().registerAmmo( getItem( "uraniumDrop" ), 238.0289 );
	}

	@Override
	public void maceratorRecipe(ItemStack in, ItemStack out)
	{
		ic2.api.recipe.Recipes.macerator.addRecipe( new RecipeInputItemStack( in, in.stackSize ), null, out );
	}

	@Override
	public void addToEnergyNet(TileEntity appEngTile)
	{
		MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileLoadEvent( (IEnergyTile) appEngTile ) );
	}

	@Override
	public void removeFromEnergyNet(TileEntity appEngTile)
	{
		MinecraftForge.EVENT_BUS.post( new ic2.api.energy.event.EnergyTileUnloadEvent( (IEnergyTile) appEngTile ) );
	}

	@Override
	public ItemStack getItem(String name)
	{
		return ic2.api.item.IC2Items.getItem( name );
	}

}
