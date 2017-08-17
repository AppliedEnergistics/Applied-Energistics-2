/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.common.ForgeModContainer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.FMLClientHandler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import appeng.api.parts.CableRenderMode;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.render.effects.AssemblerFX;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.VibrantFX;
import appeng.client.render.model.UVLModelLoader;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.textures.ParticleTextures;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.entity.EntityFloatingItem;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.entity.RenderFloatingItem;
import appeng.entity.RenderTinyTNTPrimed;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.server.ServerHelper;
import appeng.util.Platform;


public class ClientHelper extends ServerHelper
{

	@Override
	public void preinit()
	{
		MinecraftForge.EVENT_BUS.register( this );
		// Do not register the Fullbright hacks if Optifine is present or if the Forge lighting is disabled
		if( !FMLClientHandler.instance().hasOptifine() && ForgeModContainer.forgeLightPipelineEnabled )
		{
			ModelLoaderRegistry.registerLoader( UVLModelLoader.INSTANCE );
		}
	}

	@Override
	public void init()
	{
		// final Block fluixBlock = GameRegistry.findBlock( "appliedenergistics2", "fluix" );
		// Item fluixItem = Item.getItemFromBlock( fluixBlock );
		// ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation( "appliedenergistics2:fluix",
		// "inventory" );
		// final int DEFAULT_ITEM_SUBTYPE = 0;
		// final ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		// // mesher.register( fluixItem, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation );
		//
		// final ResourceLocation resource = new ResourceLocation( "appliedenergistics2", "stair.fluix" );
		// final ModelResourceLocation fluixStairModel = new ModelResourceLocation( resource, "inventory" );
		// AELog.info( "FluixStairModel: " + fluixStairModel );
		//
		// final Set<Item> items = AEApi.instance().definitions().blocks().fluixStairs().maybeItem().asSet();
		// for( Item item : items )
		// {
		// AELog.info( "Registering with %s with unlocalized %s", item, item.getUnlocalizedName() );
		// mesher.register( item, DEFAULT_ITEM_SUBTYPE, fluixStairModel );
		// }

	}

	@Override
	public World getWorld()
	{
		if( Platform.isClient() )
		{
			return Minecraft.getMinecraft().world;
		}
		else
		{
			return super.getWorld();
		}
	}

	@Override
	public void bindTileEntitySpecialRenderer( final Class<? extends TileEntity> tile, final AEBaseBlock blk )
	{

	}

	@Override
	public List<EntityPlayer> getPlayers()
	{
		if( Platform.isClient() )
		{
			final List<EntityPlayer> o = new ArrayList<>();
			o.add( Minecraft.getMinecraft().player );
			return o;
		}
		else
		{
			return super.getPlayers();
		}
	}

	@Override
	public void spawnEffect( final EffectType effect, final World world, final double posX, final double posY, final double posZ, final Object o )
	{
		if( AEConfig.instance().isEnableEffects() )
		{
			switch( effect )
			{
				case Assembler:
					this.spawnAssembler( world, posX, posY, posZ, o );
					return;
				case Vibrant:
					this.spawnVibrant( world, posX, posY, posZ );
					return;
				case Crafting:
					this.spawnCrafting( world, posX, posY, posZ );
					return;
				case Energy:
					this.spawnEnergy( world, posX, posY, posZ );
					return;
				case Lightning:
					this.spawnLightning( world, posX, posY, posZ );
					return;
				case LightningArc:
					this.spawnLightningArc( world, posX, posY, posZ, (Vec3d) o );
					return;
				default:
			}
		}
	}

	@Override
	public boolean shouldAddParticles( final Random r )
	{
		final int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
		if( setting == 2 )
		{
			return false;
		}
		if( setting == 0 )
		{
			return true;
		}
		return r.nextInt( 2 * ( setting + 1 ) ) == 0;
	}

	@Override
	public RayTraceResult getRTR()
	{
		return Minecraft.getMinecraft().objectMouseOver;
	}

	@Override
	public void postInit()
	{
		// RenderingRegistry.registerBlockHandler( WorldRender.INSTANCE );
		final RenderManager inst = Minecraft.getMinecraft().getRenderManager();

		inst.entityRenderMap.put( EntityTinyTNTPrimed.class, new RenderTinyTNTPrimed( inst ) );
		inst.entityRenderMap.put( EntityFloatingItem.class, new RenderFloatingItem( inst ) );
	}

	@Override
	public CableRenderMode getRenderMode()
	{
		if( Platform.isServer() )
		{
			return super.getRenderMode();
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayer player = mc.player;

		return this.renderModeForPlayer( player );
	}

	@Override
	public void triggerUpdates()
	{
		final Minecraft mc = Minecraft.getMinecraft();
		if( mc == null || mc.player == null || mc.world == null )
		{
			return;
		}

		final EntityPlayer player = mc.player;

		final int x = (int) player.posX;
		final int y = (int) player.posY;
		final int z = (int) player.posZ;

		final int range = 16 * 16;

		mc.world.markBlockRangeForRenderUpdate( x - range, y - range, z - range, x + range, y + range, z + range );
	}

	@SubscribeEvent
	public void postPlayerRender( final RenderLivingEvent.Pre p )
	{
		final PlayerColor player = TickHandler.INSTANCE.getPlayerColors().get( p.getEntity().getEntityId() );
		if( player != null )
		{
			final AEColor col = player.myColor;

			final float r = 0xff & ( col.mediumVariant >> 16 );
			final float g = 0xff & ( col.mediumVariant >> 8 );
			final float b = 0xff & ( col.mediumVariant );
			GlStateManager.color( r / 255.0f, g / 255.0f, b / 255.0f );
		}
	}

	private void spawnAssembler( final World world, final double posX, final double posY, final double posZ, final Object o )
	{
		final PacketAssemblerAnimation paa = (PacketAssemblerAnimation) o;

		final AssemblerFX fx = new AssemblerFX( world, posX, posY, posZ, 0.0D, 0.0D, 0.0D, paa.rate, paa.is );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnVibrant( final World w, final double x, final double y, final double z )
	{
		if( AppEng.proxy.shouldAddParticles( Platform.getRandom() ) )
		{
			final double d0 = ( Platform.getRandomFloat() - 0.5F ) * 0.26D;
			final double d1 = ( Platform.getRandomFloat() - 0.5F ) * 0.26D;
			final double d2 = ( Platform.getRandomFloat() - 0.5F ) * 0.26D;

			final VibrantFX fx = new VibrantFX( w, x + d0, y + d1, z + d2, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}

	private void spawnCrafting( final World w, final double posX, final double posY, final double posZ )
	{
		final float x = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		final float y = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		final float z = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;

		final CraftingFx fx = new CraftingFx( w, posX + x, posY + y, posZ + z, Items.DIAMOND );

		fx.setMotionX( -x * 0.2f );
		fx.setMotionY( -y * 0.2f );
		fx.setMotionZ( -z * 0.2f );

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnEnergy( final World w, final double posX, final double posY, final double posZ )
	{
		final float x = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		final float y = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		final float z = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;

		final EnergyFx fx = new EnergyFx( w, posX + x, posY + y, posZ + z, Items.DIAMOND );

		fx.setMotionX( -x * 0.1f );
		fx.setMotionY( -y * 0.1f );
		fx.setMotionZ( -z * 0.1f );

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightning( final World world, final double posX, final double posY, final double posZ )
	{
		final LightningFX fx = new LightningFX( world, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightningArc( final World world, final double posX, final double posY, final double posZ, final Vec3d second )
	{
		final LightningFX fx = new LightningArcFX( world, posX, posY, posZ, second.x, second.y, second.z, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	@SubscribeEvent
	public void wheelEvent( final MouseEvent me )
	{
		if( me.getDwheel() == 0 )
		{
			return;
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayer player = mc.player;
		if( player.isSneaking() )
		{
			final EnumHand hand;
			if( !player.getHeldItem( EnumHand.MAIN_HAND ).isEmpty() && player.getHeldItem( EnumHand.MAIN_HAND ).getItem() instanceof IMouseWheelItem )
			{
				hand = EnumHand.MAIN_HAND;
			}
			else if( !player.getHeldItem( EnumHand.OFF_HAND ).isEmpty() && player.getHeldItem( EnumHand.OFF_HAND ).getItem() instanceof IMouseWheelItem )
			{
				hand = EnumHand.OFF_HAND;
			}
			else
			{
				return;
			}

			final ItemStack is = player.getHeldItem( hand );
			try
			{
				NetworkHandler.instance().sendToServer( new PacketValueConfig( "Item", me.getDwheel() > 0 ? "WheelUp" : "WheelDown" ) );
				me.setCanceled( true );
			}
			catch( final IOException e )
			{
				AELog.debug( e );
			}
		}
	}

	@SubscribeEvent
	public void onTextureStitch( final TextureStitchEvent.Pre event )
	{
		ParticleTextures.registerSprite( event );
		InscriberTESR.registerTexture( event );
	}
}