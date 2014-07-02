package appeng.spatial;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import appeng.api.AEApi;
import appeng.core.AEConfig;

public class StorageChunkProvider extends ChunkProviderGenerate implements IChunkProvider
{

	final static Block[] ablock;
	
	static {
		
		ablock = new Block[255 * 256];
		
		Block matrixFrame = AEApi.instance().blocks().blockMatrixFrame.block();
		for (int x = 0; x < ablock.length; x++)
			ablock[x] = matrixFrame;
		
	}
	
	World w;

	public StorageChunkProvider(World wrd, long i) {
		super( wrd, i, false );
		this.w = wrd;
	}

	@Override
	public boolean unloadQueuedChunks()
	{
		return true;
	}

	@Override
	public Chunk provideChunk(int x, int z)
	{
		Chunk chunk = new Chunk( w, ablock, x, z );

		byte[] abyte = chunk.getBiomeArray();
		AEConfig config = AEConfig.instance;

		for (int k = 0; k < abyte.length; ++k)
			abyte[k] = (byte) config.storageBiomeID;

		if ( !chunk.isTerrainPopulated )
		{
			chunk.isTerrainPopulated = true;
			chunk.resetRelightChecks();
		}

		return chunk;
	}

	@Override
	public void populate(IChunkProvider par1iChunkProvider, int par2, int par3)
	{

	}

	@Override
	public List getPossibleCreatures(EnumCreatureType a, int b, int c, int d)
	{
		return new ArrayList();
	}

}
