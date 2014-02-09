package appeng.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.EntityFX;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.world.World;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.common.MinecraftForge;
import appeng.block.AEBaseBlock;
import appeng.client.render.BaseBlockRender;
import appeng.client.render.TESRWrapper;
import appeng.client.render.WorldRender;
import appeng.client.render.effects.EnergyFx;
import appeng.client.render.effects.LightningEffect;
import appeng.client.render.entity.RenderTinyTNTPrimed;
import appeng.client.texture.CableBusTextures;
import appeng.client.texture.ExtraTextures;
import appeng.entity.EntityTinyTNTPrimed;
import appeng.server.ServerHelper;
import appeng.util.Platform;
import cpw.mods.fml.client.registry.ClientRegistry;
import cpw.mods.fml.client.registry.RenderingRegistry;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ClientHelper extends ServerHelper
{

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
	public void spawnLightning(World worldObj, double posX, double posY, double posZ)
	{
		LightningEffect fx = new LightningEffect( worldObj, posX, posY + 0.3f, posZ, 0.0f, 0.0f, 0.0f );
		Minecraft.getMinecraft().effectRenderer.addEffect( (EntityFX) fx );
	}

	@Override
	public void spawnEnergy(World w, double posX, double posY, double posZ)
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
		if ( Minecraft.getMinecraft().gameSettings.particleSetting == 0 )
			return true;
		return r.nextInt( Minecraft.getMinecraft().gameSettings.particleSetting ) == 0;
	}

}