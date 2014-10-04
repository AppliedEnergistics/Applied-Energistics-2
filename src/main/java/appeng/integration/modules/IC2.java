package appeng.integration.modules;

import ic2.api.energy.tile.IEnergyTile;
import ic2.api.recipe.RecipeInputItemStack;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IIC2;

public class IC2 extends BaseModule implements IIC2
{

	public static IC2 instance;

	public IC2() {
		TestClass( IEnergyTile.class );
	}

	@Override
	public void Init()
	{
	}

	@Override
	public void PostInit()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( getItem( "copperCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "insulatedCopperCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "goldCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "insulatedGoldCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "ironCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "insulatedIronCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "insulatedTinCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "glassFiberCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "tinCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "detectorCableItem" ), TunnelType.IC2_POWER );
		reg.addNewAttunement( getItem( "splitterCableItem" ), TunnelType.IC2_POWER );

		// this is gone?
		// AEApi.instance().registries().matterCannon().registerAmmo( getItem( "uraniumDrop" ), 238.0289 );
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
