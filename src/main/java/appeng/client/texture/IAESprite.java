
package appeng.client.texture;


import net.minecraft.client.renderer.texture.TextureAtlasSprite;


public interface IAESprite
{

	int getIconWidth();

	int getIconHeight();

	float getMaxU();

	float getInterpolatedU( double px );

	float getMinV();

	float getMaxV();

	String getIconName();

	float getInterpolatedV( double px );

	float getMinU();

	TextureAtlasSprite getAtlas();

}
