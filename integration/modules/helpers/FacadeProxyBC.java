package appeng.integration.modules.helpers;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.ForgeDirection;
import buildcraft.transport.ItemFacade;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class FacadeProxyBC implements IFacadeProxy
{

	float facadeThickness = 1F / 16F;

	int facadeBlocks[];
	int facadeMeta[];

	public FacadeProxyBC() {
		facadeBlocks = new int[6];
		facadeMeta = new int[6];
	}

	/**
	 * Mirrors the array on the Y axis by calculating offsets from 0.5F
	 * 
	 * @param targetArray
	 */
	private void mirrorY(float[][] targetArray)
	{
		float temp = targetArray[1][0];
		targetArray[1][0] = (targetArray[1][1] - 0.5F) * -1F + 0.5F; // 1 ->
																		// 0.5F
																		// ->
																		// -0.5F
																		// -> 0F
		targetArray[1][1] = (temp - 0.5F) * -1F + 0.5F; // 0 -> -0.5F -> 0.5F ->
														// 1F
	}

	/**
	 * Shifts the coordinates around effectivly rotating something. Zero state
	 * is DOWN then -> NORTH -> WEST Note - To obtain Pos, do a mirrorY() before
	 * rotating
	 * 
	 * @param targetArray
	 *            the array that should be rotated
	 */
	private void rotate(float[][] targetArray)
	{
		for (int i = 0; i < 2; i++)
		{
			float temp = targetArray[2][i];
			targetArray[2][i] = targetArray[1][i];
			targetArray[1][i] = targetArray[0][i];
			targetArray[0][i] = temp;
		}
	}

	/**
	 * @param targetArray
	 *            the array that should be transformed
	 * @param direction
	 */
	private void transform(float[][] targetArray, ForgeDirection direction)
	{
		if ( (direction.ordinal() & 0x1) == 1 )
		{
			mirrorY( targetArray );
		}

		for (int i = 0; i < (direction.ordinal() >> 1); i++)
		{
			rotate( targetArray );
		}
	}

	/**
	 * Clones both dimensions of a float[][]
	 * 
	 * @param source
	 *            the float[][] to deepClone
	 * @return
	 */
	private float[][] deepClone(float[][] source)
	{
		float[][] target = source.clone();
		for (int i = 0; i < target.length; i++)
		{
			target[i] = source[i].clone();
		}
		return target;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void facadeRender(RenderBlocks renderblocks, Block block, IFacadeTile ft, int x, int y, int z, float tubeThickness)
	{
		float zFightOffset = 1F / 4096F;

		float[][] zeroState = new float[3][2];
		// X START - END
		zeroState[0][0] = 0.0F - zFightOffset / 2;
		zeroState[0][1] = 1.0F + zFightOffset / 2;
		// Y START - END
		zeroState[1][0] = 0.0F - zFightOffset;
		zeroState[1][1] = facadeThickness;
		// Z START - END
		zeroState[2][0] = 0.0F;
		zeroState[2][1] = 1.0F;

		renderblocks.renderAllFaces = true;

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			if ( hasFacade( ft, direction ) )
			{
				Block blk = Block.blocksList[facadeBlocks[direction.ordinal()]];
				renderblocks.overrideBlockTexture = blk.getIcon( direction.ordinal(), facadeMeta[direction.ordinal()] );

				try
				{
					// AppEngBlockRenderer.instance.overrideRenderColor =
					// Item.itemsList[ facadeBlocks[ direction.ordinal() ]
					// ].getColorFromItemStack(new ItemStack( facadeBlocks[
					// direction.ordinal() ], 1, facadeMeta[ direction.ordinal()
					// ] ), 0);
				} catch (Throwable error)
				{
					// explosion!
				}

				if ( blk.getRenderType() == 31 )
				{
					if ( (facadeMeta[direction.ordinal()] & 12) == 4 )
					{
						renderblocks.uvRotateEast = 1;
						renderblocks.uvRotateWest = 1;
						renderblocks.uvRotateTop = 1;
						renderblocks.uvRotateBottom = 1;
					} else if ( (facadeMeta[direction.ordinal()] & 12) == 8 )
					{
						renderblocks.uvRotateSouth = 1;
						renderblocks.uvRotateNorth = 1;
					}
				}

				float holeThickness = ft.getHoleThickness( direction );

				// Hollow facade
				if ( ft.isConnected( direction ) )
				{
					float[][] rotated = deepClone( zeroState );
					rotated[2][0] = 0.0F;
					rotated[2][1] = 0.0f + holeThickness;
					rotated[1][0] -= zFightOffset / 2;
					transform( rotated, direction );
					block.setBlockBounds( rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
							rotated[2][1] );
					renderblocks.setRenderBoundsFromBlock( block );
					renderblocks.renderStandardBlock( block, x, y, z );

					rotated = deepClone( zeroState );
					rotated[2][0] = 1.0f - holeThickness;
					rotated[1][0] -= zFightOffset / 2;
					transform( rotated, direction );
					block.setBlockBounds( rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
							rotated[2][1] );
					renderblocks.setRenderBoundsFromBlock( block );
					renderblocks.renderStandardBlock( block, x, y, z );

					rotated = deepClone( zeroState );
					rotated[0][0] = 0.0F;
					rotated[0][1] = 0.0f + holeThickness;
					rotated[1][1] -= zFightOffset;
					transform( rotated, direction );
					block.setBlockBounds( rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
							rotated[2][1] );
					renderblocks.setRenderBoundsFromBlock( block );
					renderblocks.renderStandardBlock( block, x, y, z );

					rotated = deepClone( zeroState );
					rotated[0][0] = 1.0f - holeThickness;
					rotated[0][1] = 1F;
					rotated[1][1] -= zFightOffset;
					transform( rotated, direction );
					block.setBlockBounds( rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
							rotated[2][1] );
					renderblocks.setRenderBoundsFromBlock( block );
					renderblocks.renderStandardBlock( block, x, y, z );
				} else
				{ // Solid facade
					float[][] rotated = deepClone( zeroState );
					transform( rotated, direction );
					block.setBlockBounds( rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
							rotated[2][1] );
					renderblocks.setRenderBoundsFromBlock( block );
					renderblocks.renderStandardBlock( block, x, y, z );
				}

				// AppEngBlockRenderer.instance.overrideRenderColor = -1;
			}

			renderblocks.uvRotateEast = 0;
			renderblocks.uvRotateWest = 0;
			renderblocks.uvRotateTop = 0;
			renderblocks.uvRotateBottom = 0;
			renderblocks.uvRotateSouth = 0;
			renderblocks.uvRotateNorth = 0;
		}

		// X START - END
		zeroState[0][0] = 0.0f + tubeThickness; // Utils.pipeMinPos;
		zeroState[0][1] = 1.0f - tubeThickness; // Utils.pipeMaxPos;
		// Y START - END
		zeroState[1][0] = facadeThickness;
		zeroState[1][1] = 0.0f + tubeThickness; // Utils.pipeMinPos;
		// Z START - END
		zeroState[2][0] = 0.0f + tubeThickness; // Utils.pipeMinPos;
		zeroState[2][1] = 1.0f - tubeThickness; // Utils.pipeMaxPos;

		if ( tubeThickness > 0.001 )
		{
			// renderblocks.overrideBlockTexture =
			// BuildCraftTransport.instance.pipeIconProvider.getIcon(PipeIconProvider.PipeStructureCobblestone);
			// // Structure Pipe

			for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
			{
				if ( hasFacade( ft, direction ) && !ft.isConnected( direction ) )
				{
					float[][] rotated = deepClone( zeroState );
					transform( rotated, direction );

					block.setBlockBounds( rotated[0][0], rotated[1][0], rotated[2][0], rotated[0][1], rotated[1][1],
							rotated[2][1] );
					renderblocks.setRenderBoundsFromBlock( block );
					renderblocks.renderStandardBlock( block, x, y, z );
				}
			}
		}

		renderblocks.renderAllFaces = false;
		renderblocks.overrideBlockTexture = null;
	}

	ItemStack createFacade(int itemid, int meta)
	{
		try
		{
			return ItemFacade.getStack( itemid, meta );
		} catch (Throwable e)
		{
			try
			{
				// return new ItemStack( BuildCraftTransport.facadeItem, 1,
				// ItemFacade.encode( itemid, meta ) );
			} catch (Throwable e2)
			{
				FMLLog.severe( "Unable to create facade item, please make sure your using the newest Version of BC and AE, and if you are inform AlgorithmX2 of this message." );
			}
		}
		return null;
	}

	@Override
	public List<ItemStack> getDrops(IFacadeTile ft)
	{
		List<ItemStack> out = new ArrayList();

		for (ForgeDirection direction : ForgeDirection.VALID_DIRECTIONS)
		{
			if ( this.facadeBlocks[direction.ordinal()] != 0 )
			{
				ItemStack facade = createFacade( this.facadeBlocks[direction.ordinal()], this.facadeMeta[direction.ordinal()] );

				this.facadeMeta[direction.ordinal()] = 0;

				out.add( facade );
			}
		}

		return out;
	}

	@Override
	public boolean addFacade(IFacadeTile ft, ForgeDirection direction, int blockid, int meta)
	{
		if ( hasFacade( ft, direction ) )
			dropFacade( ft, direction );

		// if ( ft.isConnected( direction ) )
		// return false;

		this.facadeBlocks[direction.ordinal()] = blockid;
		this.facadeMeta[direction.ordinal()] = meta;

		ft.markForUpdate();

		return true;
	}

	@Override
	public boolean hasFacade(IFacadeTile ft, ForgeDirection direction)
	{
		return this.facadeBlocks[direction.ordinal()] != 0;
	}

	@Override
	public void dropFacade(IFacadeTile ft, ForgeDirection direction)
	{
		if ( this.facadeBlocks[direction.ordinal()] != 0 )
		{
			ItemStack facade = createFacade( this.facadeBlocks[direction.ordinal()], this.facadeMeta[direction.ordinal()] );

			this.facadeBlocks[direction.ordinal()] = 0;
			this.facadeMeta[direction.ordinal()] = 0;

			ft.dropFacadeItem( facade );

			ft.markForUpdate();
		}
	}

	@Override
	public boolean readFromStream(DataInputStream out) throws IOException
	{
		boolean diffrent = false;
		for (int x = 0; x < 6; x++)
		{
			int fb = facadeBlocks[x];
			int fm = facadeMeta[x];
			facadeBlocks[x] = out.readInt();
			facadeMeta[x] = out.readInt();
			diffrent = diffrent || fb != facadeBlocks[x] || fm != facadeMeta[x];
		}
		return diffrent;
	}

	@Override
	public void writeToStream(DataOutputStream out) throws IOException
	{
		for (int x = 0; x < 6; x++)
		{
			out.writeInt( facadeBlocks[x] );
			out.writeInt( facadeMeta[x] );
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tc)
	{
		tc.setIntArray( "facadeBlocks", facadeBlocks );
		tc.setIntArray( "facadeMeta", facadeMeta );
	}

	@Override
	public void readFromNBT(NBTTagCompound tc)
	{
		facadeBlocks = tc.getIntArray( "facadeBlocks" );
		facadeMeta = tc.getIntArray( "facadeMeta" );

		if ( facadeBlocks == null )
			facadeBlocks = new int[6];
		if ( facadeMeta == null )
			facadeMeta = new int[6];
		if ( facadeBlocks.length != 6 )
			facadeBlocks = new int[6];
		if ( facadeMeta.length != 6 )
			facadeMeta = new int[6];
	}

	@Override
	public boolean addFacade(TileEntity tileEntity, int side, ItemStack hand)
	{
		if ( tileEntity instanceof IFacadeTile )
		{

			try
			{
				return addFacade( (IFacadeTile) tileEntity, ForgeDirection.getOrientation( side ), ItemFacade.getBlockId( hand ),
						ItemFacade.getMetaData( hand ) );
			} catch (Throwable e)
			{
				FMLLog.severe( "Unable to place facade item, please make sure your using the newest Version of BC and AE, and if you are inform AlgorithmX2 of this message." );
			}
		}
		return false;
	}

}
