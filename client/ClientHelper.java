package appeng.client;

import static net.minecraftforge.client.IItemRenderer.ItemRenderType.ENTITY;
import static net.minecraftforge.client.IItemRenderer.ItemRendererHelper.BLOCK_3D;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;

import org.lwjgl.opengl.GL11;

import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.TESRWrapper;
import appeng.client.render.WorldRender;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningEffect;
import appeng.client.render.effects.VibrantEffect;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.ExtraTextures;
import appeng.core.AEConfig;
import appeng.core.CommonHelper;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.entity.RenderTinyTNTPrimed;
import appeng.server.ServerHelper;
import appeng.util.Platform;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientHelper extends ServerHelper
{

	private static RenderItem itemRenderer = new RenderItem();
	private static RenderBlocks blockRenderer = new RenderBlocks();

	@Override
	public void doRenderItem(ItemStack itemstack, TileEntity par1EntityItemFrame)
	{
		if ( itemstack != null )
		{
			EntityItem entityitem = new EntityItem( par1EntityItemFrame.getWorldObj(), 0.0D, 0.0D, 0.0D, itemstack );
			entityitem.getEntityItem().stackSize = 1;

			// set all this stuff and then do shit? meh?
			entityitem.hoverStart = 0;
			entityitem.age = 0;
			entityitem.rotationYaw = 0;

			GL11.glPushMatrix();
			GL11.glTranslatef( 0, -0.04F, 0 );
			GL11.glColor4f( 1.0F, 1.0F, 1.0F, 1.0F );
			// GL11.glDisable( GL11.GL_CULL_FACE );

			if ( itemstack.isItemEnchanted() )
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
				if ( !ForgeHooksClient.renderInventoryItem( blockRenderer, Minecraft.getMinecraft().renderEngine, itemstack, true, 0, (float) 0, (float) 0 ) )
				{
					itemRenderer.renderItemIntoGUI( fr, Minecraft.getMinecraft().renderEngine, itemstack, 0, 0, false );
				}
			}

			GL11.glPopMatrix();
		}
	}

	@Override
	public void init()
	{
		MinecraftForge.EVENT_BUS.register( this );

		RenderingRegistry.registerBlockHandler( WorldRender.instance );
		RenderManager.instance.entityRenderMap.put( EntityTinyTNTPrimed.class, new RenderTinyTNTPrimed() );
	}

	@SubscribeEvent
	public void updateTextureSheet(TextureStitchEvent.Pre ev)
	{
		if ( ev.map.getTextureType() == 0 )
		{
			for (ExtraTextures et : ExtraTextures.values())
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
		if ( bbr.hasTESR && tile != null )
			ClientRegistry.bindTileEntitySpecialRenderer( tile, new TESRWrapper( bbr ) );
	}

	@Override
	public List<EntityPlayer> getPlayers()
	{
		if ( Platform.isClient() )
		{
			List<EntityPlayer> o = new ArrayList();
			o.add( Minecraft.getMinecraft().thePlayer );
			return o;
		}
		else
			return super.getPlayers();
	}

	@Override
	public void spawnEffect(EffectType effect, World worldObj, double posX, double posY, double posZ)
	{
		if ( AEConfig.instance.enableEffects )
		{
			switch (effect)
			{
			case Vibrant:
				spawnVibrant( worldObj, posX, posY, posZ );
				return;
			case Energy:
				spawnEnergy( worldObj, posX, posY, posZ );
				return;
			case Lightning:
				spawnLightning( worldObj, posX, posY, posZ );
				return;
			}
		}
	}

	private void spawnVibrant(World w, double x, double y, double z)
	{
		if ( CommonHelper.proxy.shouldAddParticles( Platform.getRandom() ) )
		{
			double d0 = (double) (Platform.getRandomFloat() - 0.5F) * 0.26D;
			double d1 = (double) (Platform.getRandomFloat() - 0.5F) * 0.26D;
			double d2 = (double) (Platform.getRandomFloat() - 0.5F) * 0.26D;

			VibrantEffect fx = new VibrantEffect( w, x + d0, y + d1, z + d2, 0.0D, 0.0D, 0.0D );
			Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
		}
	}

	private void spawnLightning(World worldObj, double posX, double posY, double posZ)
	{
		LightningEffect fx = new LightningEffect( worldObj, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
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

		Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
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

}