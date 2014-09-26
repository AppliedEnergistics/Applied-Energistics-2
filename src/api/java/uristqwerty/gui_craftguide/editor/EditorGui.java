package uristqwerty.gui_craftguide.editor;

import uristqwerty.gui_craftguide.components.DropdownSelector;
import uristqwerty.gui_craftguide.components.Image;
import uristqwerty.gui_craftguide.minecraft.Gui;
import uristqwerty.gui_craftguide.texture.BorderedTexture;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.SubTexture;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.texture.TextureClip;

public class EditorGui extends Gui
{
	public TextureDisplay textureDisplay;
	public TextureProperties textureProperties;

	public EditorGui(int windowWidth, int windowHeight)
	{
		super(windowWidth, windowHeight);
		
		textureDisplay = new TextureDisplay(windowWidth / 3, 20, windowWidth * 2 / 3 - 20, windowHeight - 40);
		textureProperties = new TextureProperties(20, 20, windowWidth / 3 - 40, windowHeight - 40);
		
		setupGui();
	}
	
	private void setupGui()
	{
		Texture texture = DynamicTexture.instance("base_image");
		textureProperties.addElement(
				new Image(0, 0, textureProperties.width(), textureProperties.height(), 
						new BorderedTexture(
								new Texture[]{
										new TextureClip(texture, 1, 1, 4, 4),
										new SubTexture(texture, 6, 1, 64, 4),
										new TextureClip(texture, 71, 1, 4, 4),
										new SubTexture(texture, 1, 6, 4, 64),
										new SubTexture(texture, 6, 6, 64, 64),
										new SubTexture(texture, 71, 6, 4, 64),
										new TextureClip(texture, 1, 71, 4, 4),
										new SubTexture(texture, 6, 71, 64, 4),
										new TextureClip(texture, 71, 71, 4, 4),
								}, 4),
						0, 0));
		
		textureProperties.addElement(new DropdownSelector(10, 10, 60, 20));
		
		guiWindow.addElement(textureProperties);
		guiWindow.addElement(textureDisplay);
	}
}
