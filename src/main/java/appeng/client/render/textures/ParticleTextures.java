package appeng.client.render.textures;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;


public class ParticleTextures
{
	public static TextureAtlasSprite BlockEnergyParticle;
	public static TextureAtlasSprite BlockMatterCannonParticle;

	public static void registerSprite( TextureStitchEvent.Pre event )
	{
		BlockEnergyParticle = event.getMap().registerSprite( new ResourceLocation( "appliedenergistics2:blocks/BlockEnergyParticle" ) );
		BlockMatterCannonParticle = event.getMap().registerSprite( new ResourceLocation( "appliedenergistics2:blocks/BlockMatterCannonParticle" ) );
	}
}
