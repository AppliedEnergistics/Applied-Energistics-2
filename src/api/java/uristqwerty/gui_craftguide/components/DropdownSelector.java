package uristqwerty.gui_craftguide.components;

import java.util.Arrays;

import uristqwerty.gui_craftguide.components.Window.Layer;
import uristqwerty.gui_craftguide.minecraft.Text;
import uristqwerty.gui_craftguide.texture.BorderedTexture;
import uristqwerty.gui_craftguide.texture.DynamicTexture;
import uristqwerty.gui_craftguide.texture.SubTexture;
import uristqwerty.gui_craftguide.texture.Texture;
import uristqwerty.gui_craftguide.texture.TextureClip;

public class DropdownSelector extends GuiElement
{
	private boolean open = false;
	private String[] options;
	private int selected = -1;
	private String selection = null;

	private Text selectedText = new Text(0, 0, "");
	private ScrollPane selectionPane;

	public DropdownSelector(int x, int y, int width, int height)
	{
		super(x, y, width, height);
		selectionPane = new ScrollPane(x, y + height, width, 100);

		Texture texture = DynamicTexture.instance("base_image");
		selectionPane.background = new BorderedTexture(
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
				}, 4);
	}

	@Override
	public void mousePressed(int x, int y)
	{
		if(open && !popupContainsPoint(x, y))
		{
			open = false;
			getLayer(Layer.POPUP).removeElement(selectionPane);
		}

		super.mousePressed(x, y);
	}

	@Override
	public void elementClicked(int x, int y)
	{
		if(!open)
		{
			open = true;
			getLayer(Layer.POPUP).addElement(selectionPane);
		}

		super.elementClicked(x, y);
	}

	private boolean popupContainsPoint(int x, int y)
	{
		// TODO Auto-generated method stub
		return false;
	}

	public void setOptions(String[] list)
	{
		selected = -1;
		options = Arrays.copyOf(list, list.length);

		for(int i = 0; i < options.length; i++)
		{
			if(options[i].equals(selection))
			{
				selected = i;
				break;
			}
		}

		if(selected == -1)
		{
			selection = null;
		}
	}

	public String getCurrentSelection()
	{
		return selection;
	}

	@Override
	public void draw()
	{
		if(open)
		{
			selectionPane.setPositionAbsolute(this.absoluteX(), this.absoluteY() + bounds.height());
		}

		if(selection != null)
		{
			selectedText.setText(selection);
			render(selectedText);
		}

		super.draw();
	}
}
