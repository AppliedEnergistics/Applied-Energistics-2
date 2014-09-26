package uristqwerty.CraftGuide.client.ui;

import java.util.HashMap;
import java.util.Map;

import uristqwerty.CraftGuide.client.ui.GuiButton.ButtonState;
import uristqwerty.gui_craftguide.texture.Texture;

public class ButtonTemplate
{
	private Map<ButtonState, Texture> stateImages = new HashMap<ButtonState, Texture>();

	public ButtonTemplate(Texture[] images)
	{
		int i = 0;
		for(ButtonState state: ButtonState.values())
		{
			setStateImage(state, images[i]);
			i = Math.min(i + 1, images.length);
		}
	}

	public ButtonTemplate()
	{
	}

	public Texture getStateImage(ButtonState state)
	{
		return stateImages.get(state);
	}

	public ButtonTemplate setStateImage(ButtonState state, Texture image)
	{
		stateImages.put(state, image);
		return this;
	}
}
