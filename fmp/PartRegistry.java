package appeng.fmp;

import net.minecraft.block.Block;
import appeng.block.AEBaseBlock;
import appeng.block.misc.BlockQuartzTorch;
import appeng.block.networking.BlockCableBus;
import appeng.core.Api;
import codechicken.multipart.TMultiPart;

public enum PartRegistry
{
	QuartzTorchPart("ae2_torch", BlockQuartzTorch.class, QuartzTorchPart.class), CableBusPart("ae2_cablebus", BlockCableBus.class, CableBusPart.class);

	final private String name;
	final private Class<? extends AEBaseBlock> blk;
	final private Class<? extends TMultiPart> part;

	public String getName()
	{
		return name;
	}

	private PartRegistry(String name, Class<? extends AEBaseBlock> blk, Class<? extends TMultiPart> part) {
		this.name = name;
		this.blk = blk;
		this.part = part;
	}

	public TMultiPart construct(int meta)
	{
		try
		{
			if ( this == CableBusPart )
				return (TMultiPart) Api.instance.partHelper.getCombinedInstance( part.getName() ).newInstance();
			else
				return part.getConstructor( int.class ).newInstance( meta );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	public static String getPartName(TMultiPart part)
	{
		Class c = part.getClass();
		for (PartRegistry pr : values())
		{
			if ( pr.equals( c ) )
				return pr.getName();
		}
		throw new RuntimeException( "Invalid PartName" );
	}

	public static TMultiPart getPartByBlock(Block block, int meta)
	{
		for (PartRegistry pr : values())
		{
			if ( pr.blk.isInstance( block ) )
			{
				return pr.construct( meta );
			}
		}
		return null;
	}

	public static boolean isPart(Block block)
	{
		for (PartRegistry pr : values())
		{
			if ( pr.blk.isInstance( block ) )
			{
				return true;
			}
		}
		return false;
	}
}
