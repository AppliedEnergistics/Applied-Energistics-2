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
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
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
import appeng.client.render.ModelGenerator;
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

	private static final RenderItem ITEM_RENDERER = Minecraft.getMinecraft().getRenderItem();
	private static final ModelGenerator BLOCK_RENDERER = new ModelGenerator();
	final ModelResourceLocation partRenderer = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, "DynamicPartRenderer" ), "inventory" );

	;
	public Map<Object, List<IconReg>> iconRegistrations = new HashMap();
	public List<IconReg> iconTmp = new ArrayList<>();
	public List<ResourceLocation> extraIcons = new ArrayList<>();

	@Override
	public void configureIcon( Object item, String name )
	{
		iconTmp.add( new IconReg( item, 0, name ) );
	}

	@Override
	public void preinit()
	{
		MinecraftForge.EVENT_BUS.register( this );
	}

	@Override
	public void init()
	{
		Item fluixItem = GameRegistry.findItem( "appliedenergistics2", "BlockFluix" );
		ModelResourceLocation itemModelResourceLocation = new ModelResourceLocation( "appliedenergistics2:BlockFluix", "inventory" );
		final int DEFAULT_ITEM_SUBTYPE = 0;
		Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register( fluixItem, DEFAULT_ITEM_SUBTYPE, itemModelResourceLocation );
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
	public void bindTileEntitySpecialRenderer( Class tile, AEBaseBlock blk )
	{
		BaseBlockRender bbr = blk.getRendererInstance().rendererInstance;
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
			List<EntityPlayer> o = new ArrayList<EntityPlayer>();
			o.add( Minecraft.getMinecraft().thePlayer );
			return o;
		}
		else
		{
			return super.getPlayers();
		}
	}

	@Override
	public void spawnEffect( EffectType effect, World worldObj, double posX, double posY, double posZ, Object o )
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
	public boolean shouldAddParticles( Random r )
	{
		int setting = Minecraft.getMinecraft().gameSettings.particleSetting;
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
	public void doRenderItem( ItemStack itemstack, World w )
	{
		if( itemstack != null )
		{
			EntityItem entityitem = new EntityItem( w, 0.0D, 0.0D, 0.0D, itemstack );
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
		//RenderingRegistry.registerBlockHandler( WorldRender.INSTANCE );
		RenderManager inst = Minecraft.getMinecraft().getRenderManager();

		inst.entityRenderMap.put( EntityTinyTNTPrimed.class, new RenderTinyTNTPrimed( inst ) );
		inst.entityRenderMap.put( EntityFloatingItem.class, new RenderFloatingItem( inst ) );

		String MODID = AppEng.MOD_ID + ":";

		final ItemModelMesher mesher = Minecraft.getMinecraft().getRenderItem().getItemModelMesher();
		ItemMeshDefinition imd = new ItemMeshDefinition()
		{

			@Override
			public ModelResourceLocation getModelLocation( ItemStack stack )
			{
				return partRenderer;
			}
		};

		for( IconReg reg : iconTmp )
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

				addIcon( reg.name );

				mesher.register( reg.item instanceof Item ? (Item) reg.item : Item.getItemFromBlock( (Block) reg.item ), new ItemMeshDefinition()
				{

					@Override
					public ModelResourceLocation getModelLocation( ItemStack stack )
					{
						return renderer.rendererInstance.getResourcePath();
					}
				} );
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

		for( List<IconReg> reg : iconRegistrations.values() )
		{
			String[] names = new String[reg.size()];

			Item it = null;

			int offset = 0;
			for( IconReg r : reg )
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

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;

		return this.renderModeForPlayer( player );
	}

	@Override
	public void triggerUpdates()
	{
		Minecraft mc = Minecraft.getMinecraft();
		if( mc == null || mc.thePlayer == null || mc.theWorld == null )
		{
			return;
		}

		EntityPlayer player = mc.thePlayer;

		int x = (int) player.posX;
		int y = (int) player.posY;
		int z = (int) player.posZ;

		int range = 16 * 16;

		mc.theWorld.markBlockRangeForRenderUpdate( x - range, y - range, z - range, x + range, y + range, z + range );
	}

	@Override
	public void missingCoreMod()
	{
		throw new MissingCoreMod();
	}

	@Override
	public ResourceLocation addIcon( String string )
	{
		ModelResourceLocation n = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, string ), "inventory" );
		extraIcons.add( n );
		return n;
	}

	public ModelResourceLocation setIcon( Item item, String name )
	{
		List<IconReg> reg = iconRegistrations.get( item );
		if( reg == null )
		{
			iconRegistrations.put( item, reg = new LinkedList<IconReg>() );
		}

		ModelResourceLocation res = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, name ), "inventory" );
		reg.add( new IconReg( item, -1, name, res ) );
		return res;
	}

	public ModelResourceLocation setIcon( Item item, int meta, String name )
	{
		List<IconReg> reg = iconRegistrations.get( item );
		if( reg == null )
		{
			iconRegistrations.put( item, reg = new LinkedList<IconReg>() );
		}

		ModelResourceLocation res = new ModelResourceLocation( new ResourceLocation( AppEng.MOD_ID, name ), "inventory" );
		reg.add( new IconReg( item, meta, name, res ) );
		return res;
	}

	@SubscribeEvent
	public void postPlayerRender( RenderLivingEvent.Pre p )
	{
		PlayerColor player = TickHandler.INSTANCE.getPlayerColors().get( p.entity.getEntityId() );
		if( player != null )
		{
			AEColor col = player.myColor;

			float r = 0xff & ( col.mediumVariant >> 16 );
			float g = 0xff & ( col.mediumVariant >> 8 );
			float b = 0xff & ( col.mediumVariant );
			GL11.glColor3f( r / 255.0f, g / 255.0f, b / 255.0f );
		}
	}

	private void spawnAssembler( World worldObj, double posX, double posY, double posZ, Object o )
	{
		PacketAssemblerAnimation paa = (PacketAssemblerAnimation) o;

		AssemblerFX fx = new AssemblerFX( Minecraft.getMinecraft().theWorld, posX, posY, posZ, 0.0D, 0.0D, 0.0D, paa.rate, paa.is );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnVibrant( World w, double x, double y, double z )
	{
		if( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
		{
			double d0 = ( Platform.getRandomFloat() - 0.5F ) * 0.26D;
			double d1 = ( Platform.getRandomFloat() - 0.5F ) * 0.26D;
			double d2 = ( Platform.getRandomFloat() - 0.5F ) * 0.26D;

			VibrantFX fx = new VibrantFX( w, x + d0, y + d1, z + d2, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( fx );
		}
	}

	private void spawnCrafting( World w, double posX, double posY, double posZ )
	{
		float x = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		float y = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		float z = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;

		CraftingFx fx = new CraftingFx( w, posX + x, posY + y, posZ + z, Items.diamond );

		fx.motionX = -x * 0.2;
		fx.motionY = -y * 0.2;
		fx.motionZ = -z * 0.2;

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnEnergy( World w, double posX, double posY, double posZ )
	{
		float x = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		float y = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;
		float z = (float) ( ( ( Platform.getRandomInt() % 100 ) * 0.01 ) - 0.5 ) * 0.7f;

		EnergyFx fx = new EnergyFx( w, posX + x, posY + y, posZ + z, Items.diamond );

		fx.motionX = -x * 0.1;
		fx.motionY = -y * 0.1;
		fx.motionZ = -z * 0.1;

		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightning( World worldObj, double posX, double posY, double posZ )
	{
		LightningFX fx = new LightningFX( worldObj, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	private void spawnLightningArc( World worldObj, double posX, double posY, double posZ, Vec3 second )
	{
		LightningFX fx = new LightningArcFX( worldObj, posX, posY, posZ, second.xCoord, second.yCoord, second.zCoord, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( fx );
	}

	@SubscribeEvent
	public void onModelBakeEvent( ModelBakeEvent event )
	{
		// inventory renderer
		SmartModel buses = new SmartModel( new BlockRenderInfo( ( new RendererCableBus() ) ) );
		event.modelRegistry.putObject( partRenderer, buses );

		for( IconReg reg : iconTmp )
		{
			if( reg.item instanceof IPartItem || reg.item instanceof IFacadeItem )
			{
				UniqueIdentifier i = GameRegistry.findUniqueIdentifierFor( (Item) reg.item );
				event.modelRegistry.putObject( new ModelResourceLocation( new ResourceLocation( i.modId, i.name ), "inventory" ), buses );
			}

			if( reg.item instanceof AEBaseBlock )
			{
				BlockRenderInfo renderer = ( (AEBaseBlock) reg.item ).getRendererInstance();
				if( renderer == null )
				{
					continue;
				}

				SmartModel sm = new SmartModel( renderer );
				event.modelRegistry.putObject( renderer.rendererInstance.getResourcePath(), sm );

				Map data = new DefaultStateMapper().putStateModelLocations( (Block) reg.item );
				for( Object Loc : data.values() )
				{
					ModelResourceLocation res = (ModelResourceLocation) Loc;
					event.modelRegistry.putObject( res, sm );
				}
			}
		}
	}

	@SubscribeEvent
	public void wheelEvent( MouseEvent me )
	{
		if( me.isCanceled() || me.dwheel == 0 )
		{
			return;
		}

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayer player = mc.thePlayer;
		ItemStack is = player.getHeldItem();

		if( is != null && is.getItem() instanceof IMouseWheelItem && player.isSneaking() )
		{
			try
			{
				NetworkHandler.instance.sendToServer( new PacketValueConfig( "Item", me.dwheel > 0 ? "WheelUp" : "WheelDown" ) );
				me.setCanceled( true );
			}
			catch( IOException e )
			{
				AELog.error( e );
			}
		}
	}

	@SubscribeEvent
	public void updateTextureSheet( TextureStitchEvent.Pre ev )
	{
		for( IconReg reg : iconTmp )
		{
			if( reg.item instanceof AEBaseItem )
			{
				( (AEBaseItem) reg.item ).registerCustomIcon( ev.map );
			}
			else if( reg.item instanceof AEBaseBlock )
			{
				BlockRenderInfo renderer = ( (AEBaseBlock) reg.item ).getRendererInstance();
				if( renderer == null )
				{
					continue;
				}

				( (AEBaseBlock) reg.item ).registerBlockIcons( ev.map, reg.name );
			}
		}

		for( ResourceLocation res : extraIcons )
		{
			ev.map.registerSprite( res );
		}

		//if( ev.map.getTextureType() == ITEM_RENDERER )
		{
			for( ExtraItemTextures et : ExtraItemTextures.values() )
			{
				et.registerIcon( ev.map );
			}
		}

		//if( ev.map. == BLOCK_RENDERER )
		{
			for( ExtraBlockTextures et : ExtraBlockTextures.values() )
			{
				et.registerIcon( ev.map );
			}

			for( CableBusTextures cb : CableBusTextures.values() )
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

		public IconReg( Object item2, int meta2, String name2 )
		{
			meta = meta2;
			name = name2;
			item = item2;
			loc = null;
		}

		public IconReg( Item item2, int meta2, String name2, ModelResourceLocation res )
		{
			meta = meta2;
			name = name2;
			item = item2;
			loc = res;
		}
	}
}