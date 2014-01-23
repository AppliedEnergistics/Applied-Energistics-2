package appeng.block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IconRegister;
import net.minecraft.client.resources.Resource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Icon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeDirection;
import appeng.api.implementations.IMemoryCard;
import appeng.api.implementations.MemoryCardMessages;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.networking.BlockCableBus;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.WorldRender;
import appeng.client.texture.FlipableIcon;
import appeng.core.Configuration;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.util.LookDirection;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEBaseBlock extends BlockContainer implements IAEFeature
{

	private String FeatureFullname;
	private String FeatureSubname;
	private AEFeatureHandler feature;

	private Class<? extends TileEntity> tileEntityType = null;
	protected boolean isOpaque = true;
	protected boolean isFullSize = true;

	@SideOnly(Side.CLIENT)
	public Icon renderIcon;

	@SideOnly(Side.CLIENT)
	BlockRenderInfo renderInfo;

	@Override
	public String toString()
	{
		return FeatureFullname;
	}

	@SideOnly(Side.CLIENT)
	protected Class<? extends BaseBlockRender> getRenderer()
	{
		return BaseBlockRender.class;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderType()
	{
		return WorldRender.instance.getRenderId();
	}

	@SideOnly(Side.CLIENT)
	private FlipableIcon optionaIcon(IconRegister ir, String Name, Icon substitute)
	{
		// if the input is an flippable icon find the original.
		while (substitute instanceof FlipableIcon)
			substitute = ((FlipableIcon) substitute).getOriginal();

		if ( substitute != null )
		{
			try
			{
				ResourceLocation resLoc = new ResourceLocation( Name );
				resLoc = new ResourceLocation( resLoc.getResourceDomain(), String.format( "%s/%s%s", new Object[] { "textures/blocks",
						resLoc.getResourcePath(), ".png" } ) );

				Resource res = Minecraft.getMinecraft().getResourceManager().getResource( resLoc );
				if ( res != null )
					return new FlipableIcon( ir.registerIcon( Name ) );
			}
			catch (Throwable e)
			{
				return new FlipableIcon( substitute );
			}
		}

		return new FlipableIcon( ir.registerIcon( Name ) );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IconRegister iconRegistry)
	{
		BlockRenderInfo info = getRendererInstance();
		FlipableIcon topIcon;
		FlipableIcon bottomIcon;
		FlipableIcon sideIcon;
		FlipableIcon eastIcon;
		FlipableIcon westIcon;
		FlipableIcon southIcon;
		FlipableIcon northIcon;

		this.blockIcon = topIcon = optionaIcon( iconRegistry, this.getTextureName(), null );
		bottomIcon = optionaIcon( iconRegistry, this.getTextureName() + "Bottom", topIcon );
		sideIcon = optionaIcon( iconRegistry, this.getTextureName() + "Side", topIcon );
		eastIcon = optionaIcon( iconRegistry, this.getTextureName() + "East", sideIcon );
		westIcon = optionaIcon( iconRegistry, this.getTextureName() + "West", sideIcon );
		southIcon = optionaIcon( iconRegistry, this.getTextureName() + "Front", sideIcon );
		northIcon = optionaIcon( iconRegistry, this.getTextureName() + "Back", sideIcon );

		info.updateIcons( bottomIcon, topIcon, northIcon, southIcon, eastIcon, westIcon );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Icon getIcon(int direction, int metadata)
	{
		if ( renderIcon != null )
			return renderIcon;

		return getRendererInstance().getTexture( ForgeDirection.getOrientation( direction ) );
	}

	@Override
	public Icon getBlockTexture(IBlockAccess w, int x, int y, int z, int s)
	{
		return getIcon( mapRotation( w, x, y, z, s ), 0 );
	}

	protected void setTileEntiy(Class<? extends TileEntity> c)
	{
		AEBaseTile.registerTileItem( c, new ItemStack( this ) );
		GameRegistry.registerTileEntity( tileEntityType = c, FeatureFullname );
	}

	protected void setfeature(EnumSet<AEFeature> f)
	{
		feature = new AEFeatureHandler( f, this, FeatureSubname );
	}

	protected AEBaseBlock(Class<?> c, Material mat) {
		this( c, mat, null );
		setLightOpacity( 15 );
		setLightValue( 0 );
		setHardness( 1.2F );
	}

	protected AEBaseBlock(Class<?> c, Material mat, String subname) {
		super( Configuration.instance.getBlockID( c, subname ), mat );

		if ( mat == Material.glass )
			setStepSound( Block.soundGlassFootstep );
		else if ( mat == Material.rock )
			setStepSound( Block.soundStoneFootstep );
		else
			setStepSound( Block.soundMetalFootstep );

		FeatureFullname = AEFeatureHandler.getName( c, subname );
		FeatureSubname = subname;
	}

	@Override
	final public AEFeatureHandler feature()
	{
		return feature;
	}

	public boolean isOpaque()
	{
		return isOpaque;
	}

	@Override
	final public boolean isOpaqueCube()
	{
		return isOpaque;
	}

	@Override
	public boolean renderAsNormalBlock()
	{
		return isFullSize && isOpaque;
	}

	@Override
	final public boolean isBlockNormalCube(World world, int x, int y, int z)
	{
		return isFullSize;
	}

	public boolean hasBlockTileEntity()
	{
		return tileEntityType != null;
	}

	@Override
	final public boolean hasTileEntity(int metadata)
	{
		return hasBlockTileEntity();
	}

	public Class<? extends TileEntity> getTileEntityClass()
	{
		return tileEntityType;
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderInfo getRendererInstance()
	{
		if ( renderInfo != null )
			return renderInfo;

		try
		{
			return renderInfo = new BlockRenderInfo( getRenderer().newInstance() );
		}
		catch (Throwable t)
		{
			throw new RuntimeException( t );
		}
	}

	@Override
	final public TileEntity createTileEntity(World world, int metadata)
	{
		try
		{
			return tileEntityType.newInstance();
		}
		catch (Throwable e)
		{
			throw new RuntimeException( e );
		}
	}

	@Override
	final public TileEntity createNewTileEntity(World world)
	{
		return createTileEntity( world, 0 );
	}

	final public <T extends TileEntity> T getTileEntity(IBlockAccess w, int x, int y, int z)
	{
		if ( !hasBlockTileEntity() )
			return null;

		TileEntity te = w.getBlockTileEntity( x, y, z );
		if ( tileEntityType.isInstance( te ) )
			return (T) te;

		return null;
	}

	@Override
	final public boolean rotateBlock(World w, int x, int y, int z, ForgeDirection axis)
	{
		IOrientable rotateable = null;

		if ( hasBlockTileEntity() )
		{
			rotateable = (AEBaseTile) getTileEntity( w, x, y, z );
		}
		else if ( this instanceof IOrientableBlock )
		{
			rotateable = ((IOrientableBlock) this).getOrientable( w, x, y, z );
		}

		if ( rotateable != null && rotateable.canBeRotated() )
		{
			ForgeDirection forward = rotateable.getForward();
			ForgeDirection up = rotateable.getUp();

			for (int rs = 0; rs < 4; rs++)
			{
				forward = Platform.rotateAround( forward, axis );
				up = Platform.rotateAround( up, axis );

				if ( this.isValidOrientation( w, x, y, z, forward, up ) )
				{
					rotateable.setOrientation( forward, up );
					return true;
				}
			}
		}

		return false;
	}

	public ForgeDirection mapRotation(IOrientable ori, ForgeDirection dir)
	{
		// case DOWN: return bottomIcon;
		// case UP: return blockIcon;
		// case NORTH: return northIcon;
		// case SOUTH: return southIcon;
		// case WEST: return sideIcon;
		// case EAST: return sideIcon;

		ForgeDirection forward = ori.getForward();
		ForgeDirection up = ori.getUp();
		ForgeDirection west = ForgeDirection.UNKNOWN;

		if ( forward == null || up == null )
			return dir;

		int west_x = forward.offsetY * up.offsetZ - forward.offsetZ * up.offsetY;
		int west_y = forward.offsetZ * up.offsetX - forward.offsetX * up.offsetZ;
		int west_z = forward.offsetX * up.offsetY - forward.offsetY * up.offsetX;

		for (ForgeDirection dx : ForgeDirection.VALID_DIRECTIONS)
			if ( dx.offsetX == west_x && dx.offsetY == west_y && dx.offsetZ == west_z )
				west = dx;

		if ( dir.equals( forward ) )
			return ForgeDirection.SOUTH;
		if ( dir.equals( forward.getOpposite() ) )
			return ForgeDirection.NORTH;

		if ( dir.equals( up ) )
			return ForgeDirection.UP;
		if ( dir.equals( up.getOpposite() ) )
			return ForgeDirection.DOWN;

		if ( dir.equals( west ) )
			return ForgeDirection.WEST;
		if ( dir.equals( west.getOpposite() ) )
			return ForgeDirection.EAST;

		return ForgeDirection.UNKNOWN;
	}

	int mapRotation(IBlockAccess w, int x, int y, int z, int s)
	{
		IOrientable ori = null;

		if ( hasBlockTileEntity() )
		{
			ori = (AEBaseTile) getTileEntity( w, x, y, z );
		}
		else if ( this instanceof IOrientableBlock )
		{
			ori = ((IOrientableBlock) this).getOrientable( w, x, y, z );
		}

		if ( ori != null && ori.canBeRotated() )
		{
			return mapRotation( ori, ForgeDirection.getOrientation( s ) ).ordinal();
		}

		return s;
	}

	@Override
	final public ForgeDirection[] getValidRotations(World w, int x, int y, int z)
	{
		if ( hasBlockTileEntity() )
		{
			AEBaseTile obj = getTileEntity( w, x, y, z );
			if ( obj != null && obj.canBeRotated() )
			{
				return ForgeDirection.VALID_DIRECTIONS;
			}
		}

		return new ForgeDirection[0];
	}

	@Override
	public void breakBlock(World w, int x, int y, int z, int a, int b)
	{
		AEBaseTile te = getTileEntity( w, x, y, z );
		if ( te != null )
		{
			if ( te.dropItems )
			{
				ArrayList<ItemStack> drops = new ArrayList<ItemStack>();
				te.getDrops( w, x, y, z, drops );

				// Cry ;_; ...
				Platform.spawnDrops( w, x, y, z, drops );
			}
		}

		super.breakBlock( w, x, y, z, a, b );
		if ( te != null )
			w.setBlockTileEntity( x, y, z, null );
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World w, int x, int y, int z, Vec3 a, Vec3 b)
	{
		ICustomCollision collisionHandler = null;

		if ( this instanceof ICustomCollision )
			collisionHandler = (ICustomCollision) this;
		else
		{
			AEBaseTile te = getTileEntity( w, x, y, z );
			if ( te instanceof ICustomCollision )
				collisionHandler = (ICustomCollision) te;
		}

		if ( collisionHandler != null )
		{
			Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxsFromPool( w, x, y, z );
			MovingObjectPosition br = null;

			double lastDist = 0;

			for (AxisAlignedBB bb : bbs)
			{
				setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

				MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, a, b );

				setBlockBounds( 0, 0, 0, 1, 1, 1 );

				if ( r != null )
				{
					double xLen = (a.xCoord - r.hitVec.xCoord);
					double yLen = (a.yCoord - r.hitVec.yCoord);
					double zLen = (a.zCoord - r.hitVec.zCoord);

					double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
					if ( br == null || lastDist > thisDist )
					{
						lastDist = thisDist;
						br = r;
					}
				}
			}

			if ( br != null )
			{
				return br;
			}
			return null;
		}

		setBlockBounds( 0, 0, 0, 1, 1, 1 );
		return super.collisionRayTrace( w, x, y, z, a, b );
	}

	@Override
	final public AxisAlignedBB getSelectedBoundingBoxFromPool(World w, int x, int y, int z)
	{
		ICustomCollision collisionHandler = null;
		AxisAlignedBB b = null;

		if ( this instanceof ICustomCollision )
			collisionHandler = (ICustomCollision) this;
		else
		{
			AEBaseTile te = getTileEntity( w, x, y, z );
			if ( te instanceof ICustomCollision )
				collisionHandler = (ICustomCollision) te;
		}

		if ( collisionHandler != null )
		{
			if ( Platform.isClient() )
			{
				LookDirection ld = Platform.getPlayerRay( Minecraft.getMinecraft().thePlayer );

				Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxsFromPool( w, x, y, z );
				AxisAlignedBB br = null;

				double lastDist = 0;

				for (AxisAlignedBB bb : bbs)
				{
					setBlockBounds( (float) bb.minX, (float) bb.minY, (float) bb.minZ, (float) bb.maxX, (float) bb.maxY, (float) bb.maxZ );

					MovingObjectPosition r = super.collisionRayTrace( w, x, y, z, ld.a, ld.b );

					setBlockBounds( 0, 0, 0, 1, 1, 1 );

					if ( r != null )
					{
						double xLen = (ld.a.xCoord - r.hitVec.xCoord);
						double yLen = (ld.a.yCoord - r.hitVec.yCoord);
						double zLen = (ld.a.zCoord - r.hitVec.zCoord);

						double thisDist = xLen * xLen + yLen * yLen + zLen * zLen;
						if ( br == null || lastDist > thisDist )
						{
							lastDist = thisDist;
							br = bb;
						}
					}
				}

				if ( br != null )
				{
					br.setBounds( br.minX + x, br.minY + y, br.minZ + z, br.maxX + x, br.maxY + y, br.maxZ + z );
					return br;
				}
			}

			for (AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxsFromPool( w, x, y, z ))
			{
				if ( b == null )
					b = bx;
				else
				{
					double minX = Math.min( b.minX, bx.minX );
					double minY = Math.min( b.minY, bx.minY );
					double minZ = Math.min( b.minZ, bx.minZ );
					double maxX = Math.max( b.maxX, bx.maxX );
					double maxY = Math.max( b.maxY, bx.maxY );
					double maxZ = Math.max( b.maxZ, bx.maxZ );
					b.setBounds( minX, minY, minZ, maxX, maxY, maxZ );
				}
			}

			b.setBounds( b.minX + x, b.minY + y, b.minZ + z, b.maxX + x, b.maxY + y, b.maxZ + z );
		}
		else
			b = super.getSelectedBoundingBoxFromPool( w, x, y, z );

		return b;
	}

	@Override
	final public void addCollisionBoxesToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
	{
		ICustomCollision collisionHandler = null;

		if ( this instanceof ICustomCollision )
			collisionHandler = (ICustomCollision) this;
		else
		{
			AEBaseTile te = getTileEntity( w, x, y, z );
			if ( te instanceof ICustomCollision )
				collisionHandler = (ICustomCollision) te;
		}

		if ( collisionHandler != null && bb != null )
		{
			List<AxisAlignedBB> tmp = new ArrayList<AxisAlignedBB>();
			collisionHandler.addCollidingBlockToList( w, x, y, z, bb, tmp, e );
			for (AxisAlignedBB b : tmp)
			{
				b.minX += x;
				b.minY += y;
				b.minZ += z;
				b.maxX += x;
				b.maxY += y;
				b.maxZ += z;
				if ( bb.intersectsWith( b ) )
					out.add( b );
			}
		}
		else
			super.addCollisionBoxesToList( w, x, y, z, bb, out, e );
	}

	@Override
	public void onBlockDestroyedByPlayer(World par1World, int par2, int par3, int par4, int par5)
	{
		super.onBlockDestroyedByPlayer( par1World, par2, par3, par4, par5 );
	}

	public boolean onActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		return false;
	}

	@Override
	final public boolean onBlockActivated(World w, int x, int y, int z, EntityPlayer player, int side, float hitX, float hitY, float hitZ)
	{
		if ( player != null )
		{
			ItemStack is = player.inventory.getCurrentItem();
			if ( is != null )
			{
				if ( Platform.isWrench( player, is, x, y, z ) && player.isSneaking() )
				{
					int id = w.getBlockId( x, y, z );
					if ( id != 0 )
					{
						AEBaseTile tile = getTileEntity( w, x, y, z );
						ItemStack[] drops = Platform.getBlockDrops( w, x, y, z );

						if ( tile == null )
							return false;

						if ( tile instanceof TileCableBus )
							return false;

						ItemStack op = new ItemStack( this );
						for (ItemStack ol : drops)
						{
							if ( Platform.isSameItemType( ol, op ) )
							{
								NBTTagCompound tag = tile.downloadSettings( SettingsFrom.DISMANTLE_ITEM );
								if ( tag != null )
									ol.setTagCompound( tag );
							}
						}

						if ( Block.blocksList[id].removeBlockByPlayer( w, player, x, y, z ) )
						{
							List<ItemStack> l = new ArrayList<ItemStack>();
							for (ItemStack iss : drops)
								l.add( iss );
							Platform.spawnDrops( w, x, y, z, l );
							w.setBlockToAir( x, y, z );
						}
					}
					return false;
				}

				if ( is.getItem() instanceof IMemoryCard && !(this instanceof BlockCableBus) )
				{
					IMemoryCard memc = (IMemoryCard) is.getItem();
					if ( player.isSneaking() )
					{
						AEBaseTile t = getTileEntity( w, x, y, z );
						if ( t != null )
						{
							String name = getUnlocalizedName();
							NBTTagCompound data = t.downloadSettings( SettingsFrom.MEMORY_CARD );
							if ( data != null )
							{
								memc.setMemoryCardContents( is, name, data );
								memc.notifyUser( this, player, MemoryCardMessages.SETTINGS_SAVED );
								return false;
							}
						}
					}
					else
					{
						String name = memc.getSettingsName( is );
						NBTTagCompound data = memc.getData( is );
						if ( getUnlocalizedName().equals( name ) )
						{
							AEBaseTile t = getTileEntity( w, x, y, z );
							t.uploadSettings( SettingsFrom.MEMORY_CARD, data );
							memc.notifyUser( this, player, MemoryCardMessages.SETTINGS_LOADED );
						}
						else
							memc.notifyUser( this, player, MemoryCardMessages.INVALID_MACHINE );
						return false;
					}
				}
			}
		}

		return onActivated( w, x, y, z, player, side, hitX, hitY, hitZ );
	}

	public boolean isValidOrientation(World w, int x, int y, int z, ForgeDirection forward, ForgeDirection up)
	{
		return true;
	}

	public void addInformation(ItemStack is, EntityPlayer player, List<?> lines, boolean advancedItemTooltips)
	{

	}

	public Class<AEBaseItemBlock> getItemBlockClass()
	{
		return AEBaseItemBlock.class;
	}

}
