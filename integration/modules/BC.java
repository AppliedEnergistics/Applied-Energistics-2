package appeng.integration.modules;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.TunnelType;
import appeng.api.definitions.Blocks;
import appeng.api.features.IP2PTunnelRegistry;
import appeng.api.parts.IFacadePart;
import appeng.facade.FacadePart;
import appeng.integration.BaseModule;
import appeng.integration.abstraction.IBC;
import appeng.integration.modules.helpers.BCPipeHandler;
import buildcraft.BuildCraftEnergy;
import buildcraft.BuildCraftTransport;
import buildcraft.api.tools.IToolWrench;
import buildcraft.api.transport.IPipeConnection;
import buildcraft.api.transport.IPipeTile;
import buildcraft.api.transport.IPipeTile.PipeType;
import buildcraft.transport.ItemFacade;
import buildcraft.transport.PipeIconProvider;
import buildcraft.transport.TileGenericPipe;
import cpw.mods.fml.common.event.FMLInterModComms;

public class BC extends BaseModule implements IBC
{

	public static BC instance;

	@Override
	public void addFacade(ItemStack item)
	{
		if ( item != null )
			FMLInterModComms.sendMessage( "BuildCraft|Transport", "add-facade", item );
	}

	@Override
	public boolean isWrench(Item eq)
	{
		return eq instanceof IToolWrench;
	}

	@Override
	public boolean isPipe(TileEntity te, ForgeDirection dir)
	{
		if ( te instanceof IPipeTile )
		{
			try
			{
				if ( te instanceof TileGenericPipe )
					if ( ((TileGenericPipe) te).hasPlug( dir.getOpposite() ) )
						return false;
			}
			catch (Exception err)
			{
			}

			return true;
		}

		return false;
	}

	@Override
	public boolean canWrench(Item i, EntityPlayer p, int x, int y, int z)
	{
		return ((IToolWrench) i).canWrench( p, x, y, z );
	}

	@Override
	public void wrenchUsed(Item i, EntityPlayer p, int x, int y, int z)
	{
		((IToolWrench) i).wrenchUsed( p, x, y, z );
	}

	@Override
	public boolean addItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir)
	{
		if ( is != null && te != null && te instanceof IPipeTile )
		{
			IPipeTile pt = (IPipeTile) te;
			if ( pt.getPipeType() == PipeType.ITEM )
			{
				int amt = pt.injectItem( is, false, dir );
				if ( amt == is.stackSize )
				{
					pt.injectItem( is, true, dir );
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean isFacade(ItemStack is)
	{
		if ( is == null )
			return false;
		return is.getItem() instanceof ItemFacade;
	}

	@Override
	public boolean canAddItemsToPipe(TileEntity te, ItemStack is, ForgeDirection dir)
	{

		if ( is != null && te != null && te instanceof IPipeTile )
		{
			IPipeTile pt = (IPipeTile) te;
			if ( pt.getPipeType() == PipeType.ITEM )
			{
				int amt = pt.injectItem( is, false, dir );
				if ( amt == is.stackSize )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void registerPowerP2P()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 0 ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 1 ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftEnergy.engineBlock, 1, 2 ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerCobblestone ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerDiamond ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerGold ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerQuartz ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerStone ), TunnelType.BC_POWER );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipePowerWood ), TunnelType.BC_POWER );
	}

	@Override
	public void registerItemP2P()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsWood ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsVoid ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsSandstone ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsQuartz ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsObsidian ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsIron ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsGold ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsEmerald ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsDiamond ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsStone ), TunnelType.ITEM );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeItemsCobblestone ), TunnelType.ITEM );
	}

	@Override
	public void registerLiquidsP2P()
	{
		IP2PTunnelRegistry reg = AEApi.instance().registries().p2pTunnel();
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsCobblestone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsEmerald ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsGold ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsIron ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsSandstone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsStone ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsVoid ), TunnelType.FLUID );
		reg.addNewAttunement( new ItemStack( BuildCraftTransport.pipeFluidsWood ), TunnelType.FLUID );
	}

	@Override
	public void Init()
	{
		TestClass( IPipeConnection.class );
		TestClass( ItemFacade.class );
		TestClass( IToolWrench.class );

		AEApi.instance().partHelper().registerNewLayer( "appeng.api.parts.layers.LayerIPipeConnection", "buildcraft.api.transport.IPipeConnection" );
		AEApi.instance().registries().externalStorage().addExternalStorageInterface( new BCPipeHandler() );

		Blocks b = AEApi.instance().blocks();
		addFacade( b.blockFluix.stack( 1 ) );
		addFacade( b.blockQuartz.stack( 1 ) );
		addFacade( b.blockQuartzChiseled.stack( 1 ) );
		addFacade( b.blockQuartzPiller.stack( 1 ) );
		
		Block skyStone = b.blockSkyStone.block();
		if ( skyStone != null )
		{
			addFacade( new ItemStack( skyStone, 1, 0 ) );
			addFacade( new ItemStack( skyStone, 1, 1 ) );
			addFacade( new ItemStack( skyStone, 1, 2 ) );
			addFacade( new ItemStack( skyStone, 1, 3 ) );
		}
	}

	@Override
	public void PostInit()
	{
		registerPowerP2P();
		registerItemP2P();
		registerLiquidsP2P();
	}

	@Override
	public IFacadePart createFacadePart(Block blk, int meta, ForgeDirection side)
	{
		ItemStack fs = ItemFacade.getStack( blk, meta );
		return new FacadePart( fs, side );
	}

	@Override
	public IFacadePart createFacadePart(ItemStack fs, ForgeDirection side)
	{
		return new FacadePart( fs, side );
	}

	@Override
	public ItemStack getTextureForFacade(ItemStack facade)
	{
		Block blk = ItemFacade.getBlock( facade );
		return new ItemStack( blk, 1, ItemFacade.getMetaData( facade ) );
	}

	@Override
	public IIcon getFacadeTexture()
	{
		try
		{
			return BuildCraftTransport.instance.pipeIconProvider.getIcon( PipeIconProvider.TYPE.PipeStructureCobblestone.ordinal() ); // Structure
		}
		catch (Throwable t)
		{
		}
		return null;
		// Pipe
	}

}
