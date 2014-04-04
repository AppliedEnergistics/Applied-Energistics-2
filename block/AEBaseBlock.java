package appeng.block;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.implementations.items.IMemoryCard;
import appeng.api.implementations.items.MemoryCardMessages;
import appeng.api.util.IOrientable;
import appeng.api.util.IOrientableBlock;
import appeng.block.networking.BlockCableBus;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.WorldRender;
import appeng.client.texture.FlipableIcon;
import appeng.client.texture.MissingIcon;
import appeng.core.features.AEFeature;
import appeng.core.features.AEFeatureHandler;
import appeng.core.features.IAEFeature;
import appeng.core.features.ItemStackSrc;
import appeng.helpers.ICustomCollision;
import appeng.tile.AEBaseTile;
import appeng.tile.networking.TileCableBus;
import appeng.tile.storage.TileSkyChest;
import appeng.util.LookDirection;
import appeng.util.Platform;
import appeng.util.SettingsFrom;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.ReflectionHelper;
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
	protected boolean hasSubtypes = false;
	protected boolean isInventory = false;

	@SideOnly(Side.CLIENT)
	public IIcon renderIcon;

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
	private FlipableIcon optionaIcon(IIconRegister ir, String Name, IIcon substitute)
	{
		// if the input is an flippable IIcon find the original.
		while (substitute instanceof FlipableIcon)
			substitute = ((FlipableIcon) substitute).getOriginal();

		if ( substitute != null )
		{
			try
			{
				ResourceLocation resLoc = new ResourceLocation( Name );
				resLoc = new ResourceLocation( resLoc.getResourceDomain(), String.format( "%s/%s%s", new Object[] { "textures/blocks",
						resLoc.getResourcePath(), ".png" } ) );

				IResource res = Minecraft.getMinecraft().getResourceManager().getResource( resLoc );
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
	public void registerBlockIcons(IIconRegister iconRegistry)
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

	public void registerNoIcons()
	{
		BlockRenderInfo info = getRendererInstance();
		FlipableIcon i = new FlipableIcon( new MissingIcon( this ) );
		info.updateIcons( i, i, i, i, i, i );
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int direction, int metadata)
	{
		if ( renderIcon != null )
			return renderIcon;

		return getRendererInstance().getTexture( ForgeDirection.getOrientation( direction ) );
	}

	public IIcon unmappedGetIcon(IBlockAccess w, int x, int y, int z, int s)
	{
		return super.getIcon( w, x, y, z, s );
	}

	@Override
	public IIcon getIcon(IBlockAccess w, int x, int y, int z, int s)
	{
		return getIcon( mapRotation( w, x, y, z, s ), w.getBlockMetadata( x, y, z ) );
	}

	protected void setTileEntiy(Class<? extends TileEntity> c)
	{
		AEBaseTile.registerTileItem( c, new ItemStackSrc( this, 0 ) );
		GameRegistry.registerTileEntity( tileEntityType = c, FeatureFullname );
		isInventory = IInventory.class.isAssignableFrom( c );
		setTileProvider( hasBlockTileEntity() );
	}

	protected void setfeature(EnumSet<AEFeature> f)
	{
		feature = new AEFeatureHandler( f, this, FeatureSubname );
	}

	protected AEBaseBlock(Class<?> c, Material mat) {
		this( c, mat, null );
		setLightOpacity( 15 );
		setLightLevel( 0 );
		setHardness( 1.2F );
		setTileProvider( false );
	}

	// update Block value.
	private void setTileProvider(boolean b)
	{
		ReflectionHelper.setPrivateValue( Block.class, this, b, "isTileProvider" );
	}

	protected AEBaseBlock(Class<?> c, Material mat, String subname) {
		super( mat );

		if ( mat == Material.glass )
			setStepSound( Block.soundTypeGlass );
		else if ( mat == Material.rock )
			setStepSound( Block.soundTypeStone );
		else
			setStepSound( Block.soundTypeMetal );

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
	final public boolean isNormalCube(IBlockAccess world, int x, int y, int z)
	{
		return isFullSize;
	}

	public boolean hasBlockTileEntity()
	{
		return tileEntityType != null;
	}

	public Class<? extends TileEntity> getTileEntityClass()
	{
		return tileEntityType;
	}

	@SideOnly(Side.CLIENT)
	public void setRenderStateByMeta(int itemDamage)
	{

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
	final public TileEntity createNewTileEntity(World var1, int var2)
	{
		if ( hasBlockTileEntity() )
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
		return null;
	}

	final public <T extends TileEntity> T getTileEntity(IBlockAccess w, int x, int y, int z)
	{
		if ( !hasBlockTileEntity() )
			return null;

		TileEntity te = w.getTileEntity( x, y, z );
		if ( tileEntityType.isInstance( te ) )
			return (T) te;

		return null;
	}

	protected boolean hasCustomRotation()
	{
		return false;
	}

	protected void customRotateBlock(IOrientable rotatable, ForgeDirection axis)
	{

	}

	@Override
	final public boolean rotateBlock(World w, int x, int y, int z, ForgeDirection axis)
	{
		IOrientable rotatable = null;

		if ( hasBlockTileEntity() )
		{
			rotatable = (AEBaseTile) getTileEntity( w, x, y, z );
		}
		else if ( this instanceof IOrientableBlock )
		{
			rotatable = ((IOrientableBlock) this).getOrientable( w, x, y, z );
		}

		if ( rotatable != null && rotatable.canBeRotated() )
		{
			if ( hasCustomRotation() )
			{
				customRotateBlock( rotatable, axis );
				return true;
			}
			else
			{
				ForgeDirection forward = rotatable.getForward();
				ForgeDirection up = rotatable.getUp();

				for (int rs = 0; rs < 4; rs++)
				{
					forward = Platform.rotateAround( forward, axis );
					up = Platform.rotateAround( up, axis );

					if ( this.isValidOrientation( w, x, y, z, forward, up ) )
					{
						rotatable.setOrientation( forward, up );
						return true;
					}
				}
			}
		}

		return super.rotateBlock( w, x, y, z, axis );
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
	public void breakBlock(World w, int x, int y, int z, Block a, int b)
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
			w.setTileEntity( x, y, z, null );
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
			Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxsFromPool( w, x, y, z, null, true );
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
	@SideOnly(Side.CLIENT)
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

				Iterable<AxisAlignedBB> bbs = collisionHandler.getSelectedBoundingBoxsFromPool( w, x, y, z, Minecraft.getMinecraft().thePlayer, true );
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

			for (AxisAlignedBB bx : collisionHandler.getSelectedBoundingBoxsFromPool( w, x, y, z, null, false ))
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
	// NOTE: WAS FINAL, changed for Immibis
	public void addCollisionBoxesToList(World w, int x, int y, int z, AxisAlignedBB bb, List out, Entity e)
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
					Block id = w.getBlock( x, y, z );
					if ( id != null )
					{
						AEBaseTile tile = getTileEntity( w, x, y, z );
						ItemStack[] drops = Platform.getBlockDrops( w, x, y, z );

						if ( tile == null )
							return false;

						if ( tile instanceof TileCableBus || tile instanceof TileSkyChest )
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

						if ( id.removedByPlayer( w, player, x, y, z ) )
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
								memc.notifyUser( player, MemoryCardMessages.SETTINGS_SAVED );
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
							memc.notifyUser( player, MemoryCardMessages.SETTINGS_LOADED );
						}
						else
							memc.notifyUser( player, MemoryCardMessages.INVALID_MACHINE );
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

	public String getUnlocalizedName(ItemStack is)
	{
		return getUnlocalizedName();
	}

	public void addInformation(ItemStack is, EntityPlayer player, List<?> lines, boolean advancedItemTooltips)
	{

	}

	public Class<AEBaseItemBlock> getItemBlockClass()
	{
		return AEBaseItemBlock.class;
	}

	@Override
	public void postInit()
	{
		// override!
	}

	public boolean hasSubtypes()
	{
		return hasSubtypes;
	}

	public boolean hasComparatorInputOverride()
	{
		return isInventory;
	}

	public int getComparatorInputOverride(World w, int x, int y, int z, int s)
	{
		TileEntity te = getTileEntity( w, x, y, z );
		if ( te instanceof IInventory )
			return Container.calcRedstoneFromInventory( (IInventory) te );
		return 0;
	}

	@Override
	public void onBlockPlacedBy(World w, int x, int y, int z, EntityLivingBase player, ItemStack is)
	{
		if ( is.hasDisplayName() )
		{
			TileEntity te = getTileEntity( w, x, y, z );
			if ( te instanceof AEBaseTile )
				((AEBaseTile) w.getTileEntity( x, y, z )).setName( is.getDisplayName() );
		}
	}

}
