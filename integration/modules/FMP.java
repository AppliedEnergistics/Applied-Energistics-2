package appeng.integration.modules;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import appeng.api.AEApi;
import appeng.api.definitions.Blocks;
import appeng.api.parts.IPartHost;
import appeng.core.AELog;
import appeng.fmp.CableBusPart;
import appeng.fmp.FMPEvent;
import appeng.fmp.FMPPlacementHelper;
import appeng.fmp.PartRegistry;
import appeng.integration.IIntegrationModule;
import appeng.integration.abstraction.IFMP;
import appeng.integration.modules.helpers.FMPPacketEvent;
import appeng.parts.CableBusContainer;
import codechicken.lib.vec.BlockCoord;
import codechicken.microblock.BlockMicroMaterial;
import codechicken.multipart.MultiPartRegistry;
import codechicken.multipart.MultiPartRegistry.IPartConverter;
import codechicken.multipart.MultiPartRegistry.IPartFactory;
import codechicken.multipart.MultipartGenerator;
import codechicken.multipart.TMultiPart;
import codechicken.multipart.TileMultipart;
import cpw.mods.fml.common.eventhandler.Event;

public class FMP implements IIntegrationModule, IPartFactory, IPartConverter, IFMP
{

	public static FMP instance;

	@Override
	public TMultiPart createPart(String name, boolean client)
	{
		for (PartRegistry pr : PartRegistry.values())
		{
			if ( pr.getName() == name )
				return pr.construct( 0 );
		}

		return null;
	}

	@Override
	public TMultiPart convert(World world, BlockCoord pos)
	{
		Block blk = world.getBlock( pos.x, pos.y, pos.z );
		int meta = world.getBlockMetadata( pos.x, pos.y, pos.z );

		TMultiPart part = PartRegistry.getPartByBlock( blk, meta );
		if ( part instanceof CableBusPart )
		{
			CableBusPart cbp = (CableBusPart) part;
			cbp.convertFromTile( world.getTileEntity( pos.x, pos.y, pos.z ) );
		}

		return part;
	}

	@Override
	public void Init() throws Throwable
	{
		createAndRegister( AEApi.instance().blocks().blockQuartz.block(), 0 );
		createAndRegister( AEApi.instance().blocks().blockQuartzPiller.block(), 0 );
		createAndRegister( AEApi.instance().blocks().blockQuartzChiseled.block(), 0 );
		createAndRegister( AEApi.instance().blocks().blockSkyStone.block(), 0 );
		createAndRegister( AEApi.instance().blocks().blockSkyStone.block(), 1 );
		createAndRegister( AEApi.instance().blocks().blockSkyStone.block(), 2 );
		createAndRegister( AEApi.instance().blocks().blockSkyStone.block(), 3 );

		PartRegistry reg[] = PartRegistry.values();

		String data[] = new String[reg.length];
		for (int x = 0; x < data.length; x++)
			data[x] = reg[x].getName();

		MultiPartRegistry.registerConverter( this );
		MultiPartRegistry.registerParts( this, data );

		MultipartGenerator.registerPassThroughInterface( "appeng.helpers.AEMultiTile" );
	}

	private void createAndRegister(Block block, int i)
	{
		if ( block != null )
			BlockMicroMaterial.createAndRegister( block, i );
	}

	@Override
	public void PostInit() throws Throwable
	{
		MinecraftForge.EVENT_BUS.register( new FMPEvent() );
	}

	@Override
	public IPartHost getOrCreateHost(TileEntity tile)
	{
		try
		{
			BlockCoord loc = new BlockCoord( tile.xCoord, tile.yCoord, tile.zCoord );

			TileMultipart mp = TileMultipart.getOrConvertTile( tile.getWorldObj(), loc );
			if ( mp != null )
			{
				scala.collection.Iterator<TMultiPart> i = mp.partList().iterator();
				while (i.hasNext())
				{
					TMultiPart p = i.next();
					if ( p instanceof CableBusPart )
						return (IPartHost) p;
				}

				return new FMPPlacementHelper( mp );
			}
		}
		catch (Throwable t)
		{
			AELog.error( t );
		}
		return null;
	}

	@Override
	public CableBusContainer getCableContainer(TileEntity te)
	{
		if ( te instanceof TileMultipart )
		{
			TileMultipart mp = (TileMultipart) te;
			scala.collection.Iterator<TMultiPart> i = mp.partList().iterator();
			while (i.hasNext())
			{
				TMultiPart p = i.next();
				if ( p instanceof CableBusPart )
					return ((CableBusPart) p).cb;
			}
		}
		return null;
	}

	@Override
	public void registerPassThru(Class<?> layerInterface)
	{
		try
		{
			MultipartGenerator.registerPassThroughInterface( layerInterface.getName() );
		}
		catch (Throwable t)
		{
			AELog.severe( "Failed to register " + layerInterface.getName() + " with FMP, some features may not work with MultiParts." );
			AELog.error( t );
		}
	}

	@Override
	public Event newFMPPacketEvent(EntityPlayerMP sender)
	{
		return new FMPPacketEvent( sender );
	}

	@Override
	public Iterable<Block> blockTypes()
	{
		Blocks def = AEApi.instance().blocks();
		return Arrays.asList( def.blockMultiPart.block(), def.blockQuartzTorch.block() );
	}

}
