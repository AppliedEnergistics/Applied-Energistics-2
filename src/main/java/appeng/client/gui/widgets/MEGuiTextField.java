package appeng.client.gui.widgets;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiTextField;

public class MEGuiTextField extends GuiTextField
{

	final int posX;
	final int posY;

	final int myWidth;
	final int myHeight;

	public MEGuiTextField(FontRenderer par1FontRenderer, int xPos, int yPos, int width, int height) {
		super( par1FontRenderer, xPos, yPos, width, height );
		posX = xPos;
		posY = yPos;
		myWidth = width;
		myHeight = height;
	}

	public boolean isMouseIn(int xCoord, int yCoord)
	{
				return xCoord >= posX && xCoord < posX + myWidth + 2 && yCoord >= posY -1 && yCoord < posY + myHeight;
	}
	
	@Override
	public void mouseClicked(int x, int y, int btn)
	{
		super.mouseClicked(x, y, btn);
	
		boolean setFocus = isMouseIn(x, y);
		this.setFocused(setFocus);
	}

}
