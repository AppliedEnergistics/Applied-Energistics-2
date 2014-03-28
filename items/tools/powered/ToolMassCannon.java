package appeng.items.tools.powered;

import java.util.EnumSet;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
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
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.CommonHelper;
import appeng.core.features.AEFeature;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.sync.packets.PacketMatterCannon;
import appeng.hooks.DispenserMatterCannon;
import appeng.items.contents.CellConfig;
import appeng.items.contents.CellUpgrades;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.storage.CellInventoryHandler;
import appeng.util.Platform;

public class ToolMassCannon extends AEBasePoweredItem implements IStorageCell
{

	public ToolMassCannon() {
		super( ToolMassCannon.class, null );
		setfeature( EnumSet.of( AEFeature.MatterCannon, AEFeature.PoweredTools ) );
		maxStoredPower = AEConfig.instance.mattercannon_battery;
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

		IMEInventory<IAEItemStack> cdi = AEApi.instance().registries().cell().getCellInventory( is, StorageChannel.ITEMS );

		if ( cdi instanceof CellInventoryHandler )
		{
			ICellInventory cd = ((ICellInventoryHandler) cdi).getCellInv();
			if ( cd != null )
			{
				lines.add( cd.usedBytes() + " " + GuiText.Of.getLocal() + " " + cd.totalBytes() + " " + GuiText.BytesUsed.getLocal() );
				lines.add( cd.storedItemTypes() + " " + GuiText.Of.getLocal() + " " + cd.getTotalItemTypes() + " " + GuiText.Types.getLocal() );
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

			IMEInventory inv = AEApi.instance().registries().cell().getCellInventory( item, StorageChannel.ITEMS );
			if ( inv != null )
			{
				IItemList itemList = inv.getAvailableItems( AEApi.instance().storage().createItemList() );
				IAEStack aeammo = itemList.getFirstItem();
				if ( aeammo instanceof IAEItemStack )
				{
					shots = Math.min( shots, (int) aeammo.getStackSize() );
					for (int sh = 0; sh < shots; sh++)
					{
						extractAEPower( item, 1600 );

						if ( Platform.isClient() )
							return item;

						aeammo.setStackSize( 1 );
						ItemStack ammo = ((IAEItemStack) aeammo).getItemStack();
						if ( ammo == null )
							return item;

						ammo.stackSize = 1;
						aeammo = inv.extractItems( aeammo, Actionable.MODULATE, new PlayerSource( p, null ) );
						if ( aeammo == null )
							return item;

						float f = 1.0F;
						float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * f;
						float f2 = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * f;
						double d0 = p.prevPosX + (p.posX - p.prevPosX) * (double) f;
						double d1 = p.prevPosY + (p.posY - p.prevPosY) * (double) f + 1.62D - (double) p.yOffset;
						double d2 = p.prevPosZ + (p.posZ - p.prevPosZ) * (double) f;
						Vec3 vec3 = w.getWorldVec3Pool().getVecFromPool( d0, d1, d2 );
						float f3 = MathHelper.cos( -f2 * 0.017453292F - (float) Math.PI );
						float f4 = MathHelper.sin( -f2 * 0.017453292F - (float) Math.PI );
						float f5 = -MathHelper.cos( -f1 * 0.017453292F );
						float f6 = MathHelper.sin( -f1 * 0.017453292F );
						float f7 = f4 * f5;
						float f8 = f3 * f5;
						double d3 = 32.0D;

						float penitration = AEApi.instance().registries().matterCannon().getPenetration( ammo ); // 196.96655f;
						boolean hasDestroyedSomething = true;
						while (penitration > 0 && hasDestroyedSomething)
						{
							hasDestroyedSomething = false;

							Vec3 vec31 = vec3.addVector( (double) f7 * d3, (double) f6 * d3, (double) f8 * d3 );

							AxisAlignedBB bb = AxisAlignedBB
									.getAABBPool()
									.getAABB( Math.min( vec3.xCoord, vec31.xCoord ), Math.min( vec3.yCoord, vec31.yCoord ),
											Math.min( vec3.zCoord, vec31.zCoord ), Math.max( vec3.xCoord, vec31.xCoord ),
											Math.max( vec3.yCoord, vec31.yCoord ), Math.max( vec3.zCoord, vec31.zCoord ) ).expand( 16, 16, 16 );

							Entity entity = null;
							List list = w.getEntitiesWithinAABBExcludingEntity( p, bb );
							double Closeest = 9999999.0D;
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

										f1 = 0.3F;
										AxisAlignedBB axisalignedbb1 = entity1.boundingBox.expand( (double) f1, (double) f1, (double) f1 );
										MovingObjectPosition movingobjectposition1 = axisalignedbb1.calculateIntercept( vec3, vec31 );

										if ( movingobjectposition1 != null )
										{
											double nd = vec3.squareDistanceTo( movingobjectposition1.hitVec );

											if ( nd < Closeest )
											{
												entity = entity1;
												Closeest = nd;
											}
										}
									}
								}
							}

							Vec3 Srec = w.getWorldVec3Pool().getVecFromPool( d0, d1, d2 );
							MovingObjectPosition pos = w.rayTraceBlocks( vec3, vec31, true );
							if ( entity != null && pos != null && pos.hitVec.squareDistanceTo( Srec ) > Closeest )
							{
								pos = new MovingObjectPosition( entity );
							}
							else if ( entity != null && pos == null )
							{
								pos = new MovingObjectPosition( entity );
							}

							try
							{
								CommonHelper.proxy.sendToAllNearExcept( null, d0, d1, d2, 128, w, new PacketMatterCannon( d0, d1, d2, (float) (f7 * d3),
										(float) (f6 * d3), (float) (f8 * d3), (byte) (pos == null ? 32 : pos.hitVec.squareDistanceTo( Srec ) + 1) ) );

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
									int dmg = (int) Math.ceil( penitration / 20.0f );
									if ( pos.entityHit instanceof EntityLivingBase )
									{
										EntityLivingBase el = (EntityLivingBase) pos.entityHit;
										penitration -= dmg;
										el.knockBack( p, 0, (double) -f7 * d3, (double) -f8 * d3 );
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
										penitration = 0;
									else
									{
										Block b = w.getBlock( pos.blockX, pos.blockY, pos.blockZ );
										// int meta = w.getBlockMetadata(
										// pos.blockX, pos.blockY, pos.blockZ );

										float hardness = b.getBlockHardness( w, pos.blockX, pos.blockY, pos.blockZ ) * 9.0f;
										if ( hardness >= 0.0 )
										{
											if ( penitration > hardness )
											{
												hasDestroyedSomething = true;
												penitration -= hardness;
												penitration *= 0.60;
												w.func_147480_a( pos.blockX, pos.blockY, pos.blockZ, true );
												// w.destroyBlock( pos.blockX, pos.blockY, pos.blockZ, true );
											}
										}
									}
								}
							}
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
	public int BytePerType(ItemStack iscellItem)
	{
		return 8;
	}

	@Override
	public int getTotalTypes(ItemStack cellItem)
	{
		return 1;
	}

	@Override
	public boolean isBlackListed(ItemStack cellItem, IAEItemStack requsetedAddition)
	{
		return AEApi.instance().registries().matterCannon().getPenetration( requsetedAddition.getItemStack() ) == 0;
	}
}
