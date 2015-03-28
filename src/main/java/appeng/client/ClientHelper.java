/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
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

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

import appeng.api.parts.CableRenderMode;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.TESRWrapper;
import appeng.client.render.WorldRender;
import appeng.client.render.effects.AssemblerFX;
import appeng.client.render.effects.CraftingFx;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningArcFX;
import appeng.client.render.effects.LightningFX;
import appeng.client.render.effects.VibrantFX;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.ExtraBlockTextures;
import appeng.client.texture.ExtraItemTextures;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.CommonHelper;
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
import appeng.transformer.MissingCoreMod;
import appeng.util.Platform;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.ENTITY;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

public class ClientHelper extends ServerHelper
{

	private static final RenderItem ITEM_RENDERER = new RenderItem();
	private static final RenderBlocks BLOCK_RENDERER = new RenderBlocks();

	@Override
	public CableRenderMode getRenderMode()
	{
		if ( Platform.isServer() )
			return super.getRenderMode();

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		return this.renderModeForPlayer( player );
	}

	@Override
	public void triggerUpdates()
	{
		Minecraft mc = Minecraft.getMinecraft();
		if ( mc == null || mc.thePlayer == null || mc.theWorld == null )
			return;

		EntityPlayer player = mc.thePlayer;

		if ( player == null )
			return;

		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;

		int range = 16 * 16;

		mc.theWorld.markBlockRangeForRenderUpdate( x - range, y - range, z - range, x + range, y + range, z + range );
	}

	@SubscribeEvent
	public void postPlayerRender(RenderLivingEvent.Pre p)
	{
		PlayerColor player = TickHandler.INSTANCE.getPlayerColors().get( p.entity.getEntityId() );
		if ( player != null )
		{
			AEColor col = player.myColor;

			float r = 0xff & (col.mediumVariant >> 16);
			float g = 0xff & (col.mediumVariant >> 8);
			float b = 0xff & (col.mediumVariant);
			GL11.glColor3f( r / 255.0f, g / 255.0f, b / 255.0f );
		}
	}

	@Override
	public void doRenderItem(ItemStack itemstack, World w)
	{
		if ( itemstack != null )
		{
			EntityItem entityitem = new EntityItem( w, 0.0D, 0.0D, 0.0D, itemstack );
			entityitem.getEntityItem().stackSize = 1;

			// set all this stuff and then do shit? meh?
			entityitem.hoverStart = 0;
			entityitem.age = 0;
			entityitem.rotationYaw = 0;

			GL11.glPushMatrix();
			GL11.glTranslatef( 0, -0.04F, 0 );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			// GL11.glDisable( GL11.GL_CULL_FACE );

			if ( itemstack.isItemEnchanted() || itemstack.getItem().requiresMultipleRenderPasses() )
			{
				GL11.glTranslatef( 0.0f, -0.05f, -0.25f );
				GL11.glScalef( 1.0f / 1.5f, 1.0f / 1.5f, 1.0f / 1.5f );
				// GL11.glTranslated( -8.0, -12.2, -10.6 );
				GL11.glScalef( 1.0f, -1.0f, 0.005f );
				// GL11.glScalef( 1.0f , -1.0f, 1.0f );

				Block block = Block.getBlockFromItem( itemstack.getItem() );
				if ( (itemstack.getItemSpriteNumber() == 0 && block != null && RenderBlocks.renderItemIn3d( block.getRenderType() )) )
				{
					GL11.glRotatef( 25.0f, 1.0f, 0.0f, 0.0f );
					GL11.glRotatef( 15.0f, 0.0f, 1.0f, 0.0f );
					GL11.glRotatef( 30.0f, 0.0f, 1.0f, 0.0f );
				}

				IItemRenderer customRenderer = MinecraftForgeClient.getItemRenderer( itemstack, ENTITY );
				if ( customRenderer != null && !(itemstack.getItem() instanceof ItemBlock) )
				{
					if ( customRenderer.shouldUseRenderHelper( ENTITY, itemstack, BLOCK_3D ) )
					{
						GL11.glTranslatef( 0, -0.04F, 0 );
						GL11.glScalef( 0.7f, 0.7f, 0.7f );
						GL11.glRotatef( 35, 1, 0, 0 );
						GL11.glRotatef( 45, 0, 1, 0 );
						GL11.glRotatef( -90, 0, 1, 0 );
					}
				}
				else if ( itemstack.getItem() instanceof ItemBlock )
				{
					GL11.glTranslatef( 0, -0.04F, 0 );
					GL11.glScalef( 1.1f, 1.1f, 1.1f );
					GL11.glRotatef( -90, 0, 1, 0 );
				}
				else
				{
					GL11.glTranslatef( 0, -0.14F, 0 );
					GL11.glScalef( 0.8f, 0.8f, 0.8f );
				}

				RenderItem.renderInFrame = true;
				RenderManager.instance.renderEntityWithPosYaw( entityitem, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F );
				RenderItem.renderInFrame = false;
			}
			else
			{
				GL11.glScalef( 1.0f / 42.0f, 1.0f / 42.0f, 1.0f / 42.0f );
				GL11.glTranslated( -8.0, -10.2, -10.4 );
				GL11.glScalef( 1.0f, 1.0f, 0.005f );

				RenderItem.renderInFrame = false;
				FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
				if ( !ForgeHooksClient.renderInventoryItem( BLOCK_RENDERER, Minecraft.getMinecraft().renderEngine, itemstack, true, 0, 0, 0 ) )
				{
					ITEM_RENDERER.renderItemIntoGUI( fr, Minecraft.getMinecraft().renderEngine, itemstack, 0, 0, false );
				}
			}

			GL11.glPopMatrix();
		}
	}

	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register( this );
	}

	@Override
	public void postInit()
	{
		RenderingRegistry.registerBlockHandler( WorldRender.INSTANCE );
		RenderManager.instance.entityRenderMap.put( EntityTinyTNTPrimed.class, new RenderTinyTNTPrimed() );
		RenderManager.instance.entityRenderMap.put( EntityFloatingItem.class, new RenderFloatingItem() );
	}

	@SubscribeEvent
	public void wheelEvent(MouseEvent me)
	{
		if ( me.isCanceled() || me.dwheel == 0 )
			return;

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		ItemStack is = player.getHeldItem();

		if ( is != null && is.getItem() instanceof IMouseWheelItem && player.isSneaking() )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Item", me.dwheel > 0 ? "WheelUp" : "WheelDown" ) );
				me.setCanceled( true );
			}
			catch (IOException e)
			{
				AELog.error( e );
			}
		}
	}

	@SubscribeEvent
	public void updateTextureSheet(TextureStitchEvent.Pre ev)
	{
		if ( ev.map.getTextureType() == 1 )
		{
			for (ExtraItemTextures et : ExtraItemTextures.values())
				et.registerIcon( ev.map );
		}

		if ( ev.map.getTextureType() == 0 )
		{
			for (ExtraBlockTextures et : ExtraBlockTextures.values())
				et.registerIcon( ev.map );

			for (CableBusTextures cb : CableBusTextures.values())
				cb.registerIcon( ev.map );
		}
	}

	@Override
	public World getWorld()
	{
		if ( Platform.isClient() )
			return Minecraft.getMinecraft().theWorld;
		else
			return super.getWorld();
	}

	@Override
	public void bindTileEntitySpecialRenderer(Class tile, AEBaseBlock blk)
	{
		BaseBlockRender bbr = blk.getRendererInstance().rendererInstance;
		if ( bbr.hasTESR() && tile != null )
			ClientRegistry.bindTileEntitySpecialRenderer( tile, new TESRWrapper( bbr ) );
	}

	@Override
	public List<EntityPlayer> getPlayers()
	{
		if ( Platform.isClient() )
		{
			List<EntityPlayer> o = new ArrayList<EntityPlayer>();
			o.add( Minecraft.getMinecraft().thePlayer );
			return o;
		}
		else
			return super.getPlayers();
	}

	@Override
	public void spawnEffect(EffectType effect, World worldObj, double posX, double posY, double posZ, Object o)
	{
		if ( AEConfig.instance.enableEffects )
		{
			switch (effect)
			{
			case Assembler:
				this.spawnAssembler( worldObj, posX, posY, posZ, o );
				return;
			case Vibrant:
				this.spawnVibrant( worldObj, posX, posY, posZ );
				return;
			case Crafting:
				this.spawnCrafting( worldObj, posX, posY, posZ );
				return;
			case Energy:
				this.spawnEnergy( worldObj, posX, posY, posZ );
				return;
			case Lightning:
				this.spawnLightning( worldObj, posX, posY, posZ );
				return;
			case LightningArc:
				this.spawnLightningArc( worldObj, posX, posY, posZ, (Vec3) o );
				return;
			default:
			}
		}
	}

	private void spawnAssembler(World worldObj, double posX, double posY, double posZ, Object o)
	{
		PacketAssemblerAnimation paa = (PacketAssemblerAnimation) o;

		AssemblerFX fx = new AssemblerFX( Minecraft.getMinecraft().theWorld, posX, posY, posZ, 0.0D, 0.0D, 0.0D, paa.rate, paa.is );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnVibrant(World w, double x, double y, double z)
	{
		if ( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
		{
			double d0 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
			double d1 = (Platform.getRandomFloat() - 0.5F) * 0.26D;
			double d2 = (Platform.getRandomFloat() - 0.5F) * 0.26D;

			VibrantFX fx = new VibrantFX( w, x + d0, y + d1, z + d2, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}

	private void spawnLightningArc(World worldObj, double posX, double posY, double posZ, Vec3 second)
	{
		LightningFX fx = new LightningArcFX( worldObj, posX, posY, posZ, second.xCoord, second.yCoord, second.zCoord, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightning(World worldObj, double posX, double posY, double posZ)
	{
		LightningFX fx = new LightningFX( worldObj, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnEnergy(World w, double posX, double posY, double posZ)
	{
		float x = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
		float y = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
		float z = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;

		EnergyFx fx = new EnergyFx( w, posX + x, posY + y, posZ + z, Items.diamond );

		fx.motionX = -x * 0.1;
		fx.motionY = -y * 0.1;
		fx.motionZ = -z * 0.1;

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnCrafting(World w, double posX, double posY, double posZ)
	{
		float x = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
		float y = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;
		float z = (float) (((Platform.getRandomInt() % 100) * 0.01) - 0.5) * 0.7f;

		CraftingFx fx = new CraftingFx( w, posX + x, posY + y, posZ + z, Items.diamond );

		fx.motionX = -x * 0.2;
		fx.motionY = -y * 0.2;
		fx.motionZ = -z * 0.2;

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	@Override
	public boolean shouldAddParticles(Random r)
	{
		int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
		if ( setting == 2 )
			return false;
		if ( setting == 0 )
			return true;
		return r.nextInt( 2 * (setting + 1) ) == 0;
	}

	@Override
	public MovingObjectPosition getMOP()
	{
		return Minecraft.getMinecraft().objectMouseOver;
	}

	@Override
	public void missingCoreMod()
	{
		throw new MissingCoreMod();
	}

}