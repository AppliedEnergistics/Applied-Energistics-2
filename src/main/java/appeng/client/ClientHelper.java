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
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.statemap.DefaultStateMapper;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry.UniqueIdentifier;

import appeng.api.parts.CableRenderMode;
import appeng.api.parts.IPartItem;
import appeng.api.util.AEColor;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.BlockRenderInfo;
import appeng.client.render.TESRWrapper;
import appeng.client.render.blocks.RendererCableBus;
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
import appeng.core.AppEng;
import appeng.core.CommonHelper;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PacketAssemblerAnimation;
import appeng.core.sync.packets.PacketValueConfig;
import appeng.entity.EntityFloatingItem;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.entity.RenderFloatingItem;
import appeng.entity.RenderTinyTNTPrimed;
import appeng.facade.IFacadeItem;
import appeng.helpers.IMouseWheelItem;
import appeng.hooks.TickHandler;
import appeng.hooks.TickHandler.PlayerColor;
import appeng.items.AEBaseItem;
import appeng.server.ServerHelper;
import appeng.transformer.MissingCoreMod;
import appeng.util.Platform;


public class ClientHelper extends ServerHelper
{

	private final ModelResourceLocation partRenderer = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, "DynamicPartRenderer" ), "inventory" );

	private final Map<Object, List<IconReg>> iconRegistrations = new HashMap<>();
	private final List<IconReg> iconTmp = new ArrayList<>();
	private final List<ResourceLocation> extraIcons = new ArrayList<>();

	@Override
	public void configureIcon( final Object item, final String name )
	{
		this.iconTmp.add( new IconReg( item, 0, name ) );
	}

	@Override
	public void preinit()
	{
		MinecraftForge.EVENT_BUS.register( this );
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
		// final Set<Item> items = AEApi.instance().definitions().blocks().fluixStair().maybeItem().asSet();
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
			return Minecraft.getMinecraft().theWorld;
		}
		else
		{
			return super.getWorld();
		}
	}

	@Override
	public void bindTileEntitySpecialRenderer( final Class<? extends TileEntity> tile, final AEBaseBlock blk )
	{
		final BaseBlockRender bbr = blk.getRendererInstance().getRendererInstance();
		if( bbr.hasTESR() && tile != null )
		{
			ClientRegistry.bindTileEntitySpecialRenderer( tile, new TESRWrapper( bbr ) );
		}
	}

	@Override
	public List<EntityPlayer> getPlayers()
	{
		if( Platform.isClient() )
		{
			final List<EntityPlayer> o = new ArrayList<>();
			o.add( Minecraft.getMinecraft().thePlayer );
			return o;
		}
		else
		{
			return super.getPlayers();
		}
	}

	@Override
	public void spawnEffect( final EffectType effect, final World worldObj, final double posX, final double posY, final double posZ, final Object o )
	{
		if( AEConfig.instance.enableEffects )
		{
			switch( effect )
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
	public MovingObjectPosition getMOP()
	{
		return Minecraft.getMinecraft().objectMouseOver;
	}

	@Override
	public void doRenderItem( final ItemStack itemstack, final World w )
	{
		if( itemstack != null )
		{
			final EntityItem entityitem = new EntityItem( w, 0.0D, 0.0D, 0.0D, itemstack );
			entityitem.getEntityItem().stackSize = 1;

			// set all this stuff and then do shit? meh?
			entityitem.hoverStart = 0;
			entityitem.setNoDespawn();
			entityitem.rotationYaw = 0;

			GL11.glPushMatrix();
			GL11.glTranslatef( 0, -0.04F, 0 );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			// GL11.glDisable( GL11.GL_CULL_FACE );

			// TODO RENDER ITEM FOR STORAGE MONITOR!

			GL11.glPopMatrix();
		}
	}

	@Override
	public void postInit()
	{
		// RenderingRegistry.registerBlockHandler( WorldRender.INSTANCE );
		final RenderManager inst = Minecraft.getMinecraft().getRenderManager();

		inst.entityRenderMap.put( EntityTinyTNTPrimed.class, new RenderTinyTNTPrimed( inst ) );
		inst.entityRenderMap.put( EntityFloatingItem.class, new RenderFloatingItem( inst ) );

		final ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		final ItemMeshDefinition imd = new ItemMeshDefinition()
		{

			@Override
			public ModelResourceLocation getModelLocation( final ItemStack stack )
			{
				return ClientHelper.this.partRenderer;
			}
		};

		for( final IconReg reg : this.iconTmp )
		{
			if( reg.item instanceof IPartItem || reg.item instanceof IFacadeItem )
			{
				mesher.register( reg.item instanceof Item ? (Item) reg.item : Item.getItemFromBlock( (Block) reg.item ), imd );
				continue;
			}

			if( reg.item instanceof AEBaseBlock )
			{
				final BlockRenderInfo renderer = ( (AEBaseBlock) reg.item ).getRendererInstance();
				if( renderer == null )
				{
					continue;
				}

				this.addIcon( reg.name );

				mesher.register( reg.item instanceof Item ? (Item) reg.item : Item.getItemFromBlock( (Block) reg.item ), stack -> renderer.getRendererInstance().getResourcePath() );
				continue;
			}

			if( reg.name == null )
			{
				continue;
			}

			if( reg.item instanceof AEBaseItem )
			{
				( (AEBaseItem) reg.item ).registerIcons( this, reg.name );
			}
			else if( reg.item instanceof Item )
			{
				this.setIcon( (Item) reg.item, 0, reg.name );
			}
		}

		final String MODID = AppEng.MOD_ID + ":";
		for( final List<IconReg> reg : this.iconRegistrations.values() )
		{
			final String[] names = new String[reg.size()];

			Item it = null;

			int offset = 0;
			for( final IconReg r : reg )
			{
				it = (Item) r.item;

				if( r.meta >= 0 )
				{
					mesher.register( (Item) r.item, r.meta, r.loc );
				}

				names[offset++] = MODID + r.name;
			}

			ModelBakery.addVariantName( it, names );
		}
	}

	@Override
	public CableRenderMode getRenderMode()
	{
		if( Platform.isServer() )
		{
			return super.getRenderMode();
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayer player = mc.thePlayer;

		return this.renderModeForPlayer( player );
	}

	@Override
	public void triggerUpdates()
	{
		final Minecraft mc = Minecraft.getMinecraft();
		if( mc == null || mc.thePlayer == null || mc.theWorld == null )
		{
			return;
		}

		final EntityPlayer player = mc.thePlayer;

		final int x = (int) player.posX;
		final int y = (int) player.posY;
		final int z = (int) player.posZ;

		final int range = 16 * 16;

		mc.theWorld.markBlockRangeForRenderUpdate( x - range, y - range, z - range, x + range, y + range, z + range );
	}

	@Override
	public void missingCoreMod()
	{
		throw new MissingCoreMod();
	}

	@Override
	public ResourceLocation addIcon( final String string )
	{
		final ModelResourceLocation n = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, string ), "inventory" );
		this.extraIcons.add( n );
		return n;
	}

	public ModelResourceLocation setIcon( final Item item, final String name )
	{
		List<IconReg> reg = this.iconRegistrations.get( item );
		if( reg == null )
		{
			this.iconRegistrations.put( item, reg = new LinkedList<>() );
		}

		final ModelResourceLocation res = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, name ), "inventory" );
		reg.add( new IconReg( item, -1, name, res ) );
		return res;
	}

	public ModelResourceLocation setIcon( final Item item, final int meta, final String name )
	{
		List<IconReg> reg = this.iconRegistrations.get( item );
		if( reg == null )
		{
			this.iconRegistrations.put( item, reg = new LinkedList<>() );
		}

		final ModelResourceLocation res = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, name ), "inventory" );
		reg.add( new IconReg( item, meta, name, res ) );
		return res;
	}

	@SubscribeEvent
	public void postPlayerRender( final RenderLivingEvent.Pre p )
	{
		final PlayerColor player = TickHandler.INSTANCE.getPlayerColors().get( p.entity.getEntityId() );
		if( player != null )
		{
			final AEColor col = player.myColor;

			final float r = 0xff & ( col.mediumVariant >> 16 );
			final float g = 0xff & ( col.mediumVariant >> 8 );
			final float b = 0xff & ( col.mediumVariant );
			GL11.glColor3f( r / 255.0f, g / 255.0f, b / 255.0f );
		}
	}

	private void spawnAssembler( final World worldObj, final double posX, final double posY, final double posZ, final Object o )
	{
		final PacketAssemblerAnimation paa = (PacketAssemblerAnimation) o;

		final AssemblerFX fx = new AssemblerFX( Minecraft.getMinecraft().theWorld, posX, posY, posZ, 0.0D, 0.0D, 0.0D, paa.rate, paa.is );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnVibrant( final World w, final double x, final double y, final double z )
	{
		if( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
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

		final CraftingFx fx = new CraftingFx( w, posX + x, posY + y, posZ + z, Items.diamond );

		fx.motionX = -x * 0.2;
		fx.motionY = -y * 0.2;
		fx.motionZ = -z * 0.2;

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnEnergy( final World w, final double posX, final double posY, final double posZ )
	{
		final float x = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		final float y = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		final float z = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;

		final EnergyFx fx = new EnergyFx( w, posX + x, posY + y, posZ + z, Items.diamond );

		fx.motionX = -x * 0.1;
		fx.motionY = -y * 0.1;
		fx.motionZ = -z * 0.1;

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightning( final World worldObj, final double posX, final double posY, final double posZ )
	{
		final LightningFX fx = new LightningFX( worldObj, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightningArc( final World worldObj, final double posX, final double posY, final double posZ, final Vec3 second )
	{
		final LightningFX fx = new LightningArcFX( worldObj, posX, posY, posZ, second.xCoord, second.yCoord, second.zCoord, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	@SubscribeEvent
	public void onModelBakeEvent( final ModelBakeEvent event )
	{
		// inventory renderer
		final SmartModel buses = new SmartModel( new BlockRenderInfo( ( new RendererCableBus() ) ) );
		event.modelRegistry.putObject( this.partRenderer, buses );

		for( final IconReg reg : this.iconTmp )
		{
			if( reg.item instanceof IPartItem || reg.item instanceof IFacadeItem )
			{
				final UniqueIdentifier i = GameRegistry.findUniqueIdentifierFor( (Item) reg.item );
				event.modelRegistry.putObject( new ModelResourceLocation( new ResourceLocation( i.modId, i.name ), "inventory" ), buses );
			}

			if( reg.item instanceof AEBaseBlock )
			{
				final BlockRenderInfo renderer = ( (AEBaseBlock) reg.item ).getRendererInstance();
				if( renderer == null )
				{
					continue;
				}

				final SmartModel sm = new SmartModel( renderer );
				event.modelRegistry.putObject( renderer.getRendererInstance().getResourcePath(), sm );

				final Map data = new DefaultStateMapper().putStateModelLocations( (Block) reg.item );
				for( final Object Loc : data.values() )
				{
					final ModelResourceLocation res = (ModelResourceLocation) Loc;
					event.modelRegistry.putObject( res, sm );
				}
			}
		}
	}

	@SubscribeEvent
	public void wheelEvent( final MouseEvent me )
	{
		if( me.isCanceled() || me.dwheel == 0 )
		{
			return;
		}

		final Minecraft mc = Minecraft.getMinecraft();
		final EntityPlayer player = mc.thePlayer;
		final ItemStack is = player.getHeldItem();

		if( is != null && is.getItem() instanceof IMouseWheelItem && player.isSneaking() )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Item", me.dwheel > 0 ? "WheelUp" : "WheelDown" ) );
				me.setCanceled( true );
			}
			catch( final IOException e )
			{
				AELog.error( e );
			}
		}
	}

	@SubscribeEvent
	public void updateTextureSheet( final TextureStitchEvent.Pre ev )
	{
		for( final IconReg reg : this.iconTmp )
		{
			if( reg.item instanceof AEBaseItem )
			{
				( (AEBaseItem) reg.item ).registerCustomIcon( ev.map );
			}
			else if( reg.item instanceof AEBaseBlock )
			{
				final BlockRenderInfo renderer = ( (AEBaseBlock) reg.item ).getRendererInstance();
				if( renderer == null )
				{
					continue;
				}

				( (AEBaseBlock) reg.item ).registerBlockIcons( ev.map, reg.name );
			}
		}

		this.extraIcons.forEach( ev.map::registerSprite );

		// if( ev.map.getTextureType() == ITEM_RENDERER )
		{
			for( final ExtraItemTextures et : ExtraItemTextures.values() )
			{
				et.registerIcon( ev.map );
			}
		}

		// if( ev.map. == BLOCK_RENDERER )
		{
			for( final ExtraBlockTextures et : ExtraBlockTextures.values() )
			{
				et.registerIcon( ev.map );
			}

			for( final CableBusTextures cb : CableBusTextures.values() )
			{
				cb.registerIcon( ev.map );
			}
		}
	}

	private static class IconReg
	{
		public final String name;
		public final Object item;
		public final int meta;
		public final ModelResourceLocation loc;

		public IconReg( final Object item2, final int meta2, final String name2 )
		{
			this.meta = meta2;
			this.name = name2;
			this.item = item2;
			this.loc = null;
		}

		public IconReg( final Item item2, final int meta2, final String name2, final ModelResourceLocation res )
		{
			this.meta = meta2;
			this.name = name2;
			this.item = item2;
			this.loc = res;
		}
	}
}