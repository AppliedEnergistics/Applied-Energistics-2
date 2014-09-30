package appeng.items.tools.powered;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.AEApi;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.config.Upgrades;
import appeng.api.implementations.items.IStorageCell;
import appeng.api.networking.security.PlayerSource;
import appeng.api.storage.ICellInventory;
import appeng.api.storage.ICellInventoryHandler;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.api.storage.data.IAEStack;
import appeng.api.storage.data.IItemList;
import appeng.api.util.AEColor;
import appeng.api.util.DimensionalCoord;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.hooks.DispenserMatterCannon;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.misc.ItemPaintBall;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventoryHandler;
import appeng.tile.misc.TilePaint;
import appeng.util.Platform;

public class ToolMassCannon extends AEBasePoweredItem implements IStorageCell
{

	public ToolMassCannon() {
		super( ToolMassCannon.class, null );
		setFeature( EnumSet.of( AEFeature.MatterCannon, AEFeature.PoweredTools ) );
		maxStoredPower = AEConfig.instance.matterCannonBattery;
	}

	@Override
	public void postInit()
	{
		super.postInit();
		BlockDispenser.dispenseBehaviorRegistry.putObject( this, new DispenserMatterCannon() );
	}

	@Override
	public void addInformation(ItemStack is, EntityPlayer player, List lines, boolean advancedItemTooltips)
	{
		super.addInformation( is, player, lines, advancedItemTooltips );

		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( is, null, StorageChannel.ITEMS );

		if ( cdi instanceof CellInventoryHandler )
		{
			ICellInventory cd = ((ICellInventoryHandler) cdi).getCellInv();
			if ( cd != null )
			{
				lines.add( cd.getUsedBytes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalBytes() + " " + GuiText.BytesUsed.getLocal() );
				lines.add( cd.getStoredItemTypes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalItemTypes() + " " + GuiText.Types.getLocal() );
			}
		}
	}

	@Override
	public ItemStack onItemRightClick(ItemStack item, World w, EntityPlayer p)
	{
		if ( this.getAECurrentPower( item ) > 1600 )
		{
			int shots = 1;

			CellUpgrades cu = (CellUpgrades) getUpgradesInventory( item );
			if ( cu != null )
				shots += cu.getInstalledUpgrades( Upgrades.SPEED );

			IMEInventory inv = AEApi.instance().registries().cell().getCellInventory( item, null, StorageChannel.ITEMS );
			if ( inv != null )
			{
				IItemList itemList = inv.getAvailableItems( AEApi.instance().storage().createItemList() );
				IAEStack aeAmmo = itemList.getFirstItem();
				if ( aeAmmo instanceof IAEItemStack )
				{
					shots = Math.min( shots, (int) aeAmmo.getStackSize() );
					for (int sh = 0; sh < shots; sh++)
					{
						extractAEPower( item, 1600 );

						if ( Platform.isClient() )
							return item;

						aeAmmo.setStackSize( 1 );
						ItemStack ammo = ((IAEItemStack) aeAmmo).getItemStack();
						if ( ammo == null )
							return item;

						ammo.stackSize = 1;
						aeAmmo = inv.extractItems( aeAmmo, Actionable.MODULATE, new PlayerSource( p, null ) );
						if ( aeAmmo == null )
							return item;

						float f = 1.0F;
						float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * f;
						float f2 = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * f;
						double d0 = p.prevPosX + (p.posX - p.prevPosX) * f;
						double d1 = p.prevPosY + (p.posY - p.prevPosY) * f + 1.62D - p.yOffset;
						double d2 = p.prevPosZ + (p.posZ - p.prevPosZ) * f;
						Vec3 vec3 = Vec3.createVectorHelper( d0, d1, d2 );
						float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
						float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
						float f5 = -MathHelper.cos( -f1 * 0.017453292F );
						float f6 = MathHelper.sin( -f1 * 0.017453292F );
						float f7 = f4 * f5;
						float f8 = f3 * f5;
						double d3 = 32.0D;

						Vec3 vec31 = vec3.addVector( f7 * d3, f6 * d3, f8 * d3 );
						Vec3 direction = Vec3.createVectorHelper( f7 * d3, f6 * d3, f8 * d3 );
						direction.normalize();

						float penetration = AEApi.instance().registries().matterCannon().getPenetration( ammo ); // 196.96655f;
						if ( penetration <= 0 )
						{
							ItemStack type = ((IAEItemStack) aeAmmo).getItemStack();
							if ( type.getItem() instanceof ItemPaintBall )
							{
								shootPaintBalls( type, w, p, vec3, vec31, direction, d0, d1, d2 );
							}
							return item;
						}
						else
						{
							standardAmmo( penetration, w, p, vec3, vec31, direction, d0, d1, d2 );
						}

					}
				}
				else
				{
					if ( Platform.isServer() )
						p.addChatMessage( PlayerMessages.AmmoDepleted.get() );
					return item;
				}
			}
		}
		return item;
	}

	private void shootPaintBalls(ItemStack type, World w, EntityPlayer p, Vec3 vec3, Vec3 vec31, Vec3 direction, double d0, double d1, double d2)
	{
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ),
				Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ),
				Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

		Entity entity = null;
		List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
		double closest = 9999999.0D;
		int l;

		for (l = 0; l < list.size(); ++l)
		{
			Entity entity1 = (Entity) list.get( l );

			if ( entity1.isDead == false && entity1 != p && !(entity1 instanceof EntityItem) )
			{
				if ( entity1.isEntityAlive() )
				{
					// prevent killing / flying of mounts.
					if ( entity1.riddenByEntity == p )
						continue;

					float f1 = 0.3F;

					AxisAlignedBB boundingBox = entity1.boundingBox.expand( f1, f1, f1 );
					MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

					if ( movingObjectPosition != null )
					{
						double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

						if ( nd < closest )
						{
							entity = entity1;
							closest = nd;
						}
					}
				}
			}
		}

		MovingObjectPosition pos = w.rayTraceBlocks( vec3, vec31, false );

		Vec3 vec = Vec3.createVectorHelper( d0, d1, d2 );
		if ( entity != null && pos != null && pos.hitVec.squareDistanceTo( vec ) > closest )
		{
			pos = new MovingObjectPosition( entity );
		}
		else if ( entity != null && pos == null )
		{
			pos = new MovingObjectPosition( entity );
		}

		try
		{
			CommonHelper.proxy.sendToAllNearExcept( null, d0, d1, d2, 128, w, new PacketMatterCannon( d0, d1, d2, (float) direction.xCoord,
					(float) direction.yCoord, (float) direction.zCoord, (byte) (pos == null ? 32 : pos.hitVec.squareDistanceTo( vec ) + 1) ) );

		}
		catch (Exception err)
		{
			AELog.error( err );
		}

		if ( pos != null && type != null && type.getItem() instanceof ItemPaintBall )
		{
			ItemPaintBall ipb = (ItemPaintBall) type.getItem();

			AEColor col = ipb.getColor( type );
			// boolean lit = ipb.isLumen( type );

			if ( pos.typeOfHit == MovingObjectType.ENTITY )
			{
				int id = pos.entityHit.getEntityId();
				PlayerColor marker = new PlayerColor( id, col, 20 * 30 );
				TickHandler.instance.getPlayerColors().put( id, marker );

				if ( pos.entityHit instanceof EntitySheep )
				{
					EntitySheep sh = (EntitySheep) pos.entityHit;
					sh.setFleeceColor( col.ordinal() );
				}

				pos.entityHit.attackEntityFrom( DamageSource.causePlayerDamage( p ), 0 );
				NetworkHandler.instance.sendToAll( marker.getPacket() );
			}
			else if ( pos.typeOfHit == MovingObjectType.BLOCK )
			{
				ForgeDirection side = ForgeDirection.getOrientation( pos.sideHit );

				int x = pos.blockX + side.offsetX;
				int y = pos.blockY + side.offsetY;
				int z = pos.blockZ + side.offsetZ;

				if ( !Platform.hasPermissions( new DimensionalCoord( w, x, y, z ), p ) )
					return;

				Block whatsThere = w.getBlock( x, y, z );
				if ( whatsThere == AEApi.instance().blocks().blockPaint.block() )
				{

				}
				else if ( whatsThere.isReplaceable( w, x, y, z ) && w.isAirBlock( x, y, z ) )
				{
					w.setBlock( x, y, z, AEApi.instance().blocks().blockPaint.block(), 0, 3 );
				}

				TileEntity te = w.getTileEntity( x, y, z );
				if ( te instanceof TilePaint )
				{
					pos.hitVec.xCoord -= x;
					pos.hitVec.yCoord -= y;
					pos.hitVec.zCoord -= z;
					((TilePaint) te).addBlot( type, side.getOpposite(), pos.hitVec );
				}
			}

		}
	}

	private void standardAmmo(float penetration, World w, EntityPlayer p, Vec3 vec3, Vec3 vec31, Vec3 direction, double d0, double d1, double d2)
	{
		boolean hasDestroyedSomething = true;
		while (penetration > 0 && hasDestroyedSomething)
		{
			hasDestroyedSomething = false;

			AxisAlignedBB bb = AxisAlignedBB.getBoundingBox( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ),
					Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ), Math.max( vec3.yCoord, vec31.yCoord ),
					Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

			Entity entity = null;
			List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
			double closest = 9999999.0D;
			int l;

			for (l = 0; l < list.size(); ++l)
			{
				Entity entity1 = (Entity) list.get( l );

				if ( entity1.isDead == false && entity1 != p && !(entity1 instanceof EntityItem) )
				{
					if ( entity1.isEntityAlive() )
					{
						// prevent killing / flying of mounts.
						if ( entity1.riddenByEntity == p )
							continue;

						float f1 = 0.3F;

						AxisAlignedBB boundingBox = entity1.boundingBox.expand( f1, f1, f1 );
						MovingObjectPosition movingObjectPosition = boundingBox.calculateIntercept( vec3, vec31 );

						if ( movingObjectPosition != null )
						{
							double nd = vec3.squareDistanceTo( movingObjectPosition.hitVec );

							if ( nd < closest )
							{
								entity = entity1;
								closest = nd;
							}
						}
					}
				}
			}

			Vec3 vec = Vec3.createVectorHelper( d0, d1, d2 );
			MovingObjectPosition pos = w.rayTraceBlocks( vec3, vec31, true );
			if ( entity != null && pos != null && pos.hitVec.squareDistanceTo( vec ) > closest )
			{
				pos = new MovingObjectPosition( entity );
			}
			else if ( entity != null && pos == null )
			{
				pos = new MovingObjectPosition( entity );
			}

			try
			{
				CommonHelper.proxy.sendToAllNearExcept( null, d0, d1, d2, 128, w, new PacketMatterCannon( d0, d1, d2, (float) direction.xCoord,
						(float) direction.yCoord, (float) direction.zCoord, (byte) (pos == null ? 32 : pos.hitVec.squareDistanceTo( vec ) + 1) ) );

			}
			catch (Exception err)
			{
				AELog.error( err );
			}

			if ( pos != null )
			{
				DamageSource dmgSrc = DamageSource.causePlayerDamage( p );
				dmgSrc.damageType = "masscannon";

				if ( pos.typeOfHit == MovingObjectType.ENTITY )
				{
					int dmg = (int) Math.ceil( penetration / 20.0f );
					if ( pos.entityHit instanceof EntityLivingBase )
					{
						EntityLivingBase el = (EntityLivingBase) pos.entityHit;
						penetration -= dmg;
						el.knockBack( p, 0, -direction.xCoord, -direction.zCoord );
						// el.knockBack( p, 0, vec3.xCoord,
						// vec3.zCoord );
						el.attackEntityFrom( dmgSrc, dmg );
						if ( !el.isEntityAlive() )
							hasDestroyedSomething = true;
					}
					else if ( pos.entityHit instanceof EntityItem )
					{
						hasDestroyedSomething = true;
						pos.entityHit.setDead();
					}
					else if ( pos.entityHit.attackEntityFrom( dmgSrc, dmg ) )
					{
						hasDestroyedSomething = true;
					}
				}
				else if ( pos.typeOfHit == MovingObjectType.BLOCK )
				{
					if ( !AEConfig.instance.isFeatureEnabled( AEFeature.MassCannonBlockDamage ) )
						penetration = 0;
					else
					{
						Block b = w.getBlock( pos.blockX, pos.blockY, pos.blockZ );
						// int meta = w.getBlockMetadata(
						// pos.blockX, pos.blockY, pos.blockZ );

						float hardness = b.getBlockHardness( w, pos.blockX, pos.blockY, pos.blockZ ) * 9.0f;
						if ( hardness >= 0.0 )
						{
							if ( penetration > hardness && Platform.hasPermissions( new DimensionalCoord( w, pos.blockX, pos.blockY, pos.blockZ ), p ) )
							{
								hasDestroyedSomething = true;
								penetration -= hardness;
								penetration *= 0.60;
								w.func_147480_a( pos.blockX, pos.blockY, pos.blockZ, true );
								// w.destroyBlock( pos.blockX, pos.blockY, pos.blockZ, true );
							}
						}
					}
				}
			}
		}
	}

	@Override
	public boolean storableInStorageCell()
	{
		return true;
	}

	@Override
	public boolean isStorageCell(ItemStack i)
	{
		return true;
	}

	@Override
	public double getIdleDrain()
	{
		return 0.5;
	}

	@Override
	public IInventory getUpgradesInventory(ItemStack is)
	{
		return new CellUpgrades( is, 4 );
	}

	@Override
	public IInventory getConfigInventory(ItemStack is)
	{
		return new CellConfig( is );
	}

	@Override
	public FuzzyMode getFuzzyMode(ItemStack is)
	{
		String fz = Platform.openNbtData( is ).getString( "FuzzyMode" );
		try
		{
			return FuzzyMode.valueOf( fz );
		}
		catch (Throwable t)
		{
			return FuzzyMode.IGNORE_ALL;
		}
	}

	@Override
	public void setFuzzyMode(ItemStack is, FuzzyMode fzMode)
	{
		Platform.openNbtData( is ).setString( "FuzzyMode", fzMode.name() );
	}

	@Override
	public boolean isEditable(ItemStack is)
	{
		return true;
	}

	@Override
	public int getBytes(ItemStack cellItem)
	{
		return 512;
	}

	@Override
	public int BytePerType(ItemStack cell)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 1;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requestedAddition)
	{
		float pen = AEApi.instance().registries().matterCannon().getPenetration( requestedAddition.getItemStack() );
		if ( pen > 0 )
			return false;

		if ( requestedAddition.getItem() instanceof ItemPaintBall )
			return false;

		return true;
	}
}
