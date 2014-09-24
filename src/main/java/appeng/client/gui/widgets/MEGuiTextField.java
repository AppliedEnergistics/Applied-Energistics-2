package appeng.client.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class MEGuiTextField extends GuiTextField
{

	int posX;
	int posY;

	int myWidth;
	int myHeight;

	public MEGuiTextField(FontRenderer par1FontRenderer, int xPos, int yPos, int width, int height) {
		super( par1FontRenderer, xPos, yPos, width, height );
		posX = xPos;
		posY = yPos;
		myWidth = width;
		myHeight = height;
	}

	public boolean isMouseIn(int xCoord, int yCoord)
	{
		return xCoord >= posX && xCoord < posX + myWidth && yCoord >= posY && yCoord < posY + myHeight;
	}

}
