package appeng.client.texture;

import net.minecraft.util.Icon;

public class TmpFlipableIcon extends FlipableIcon
{

	public TmpFlipableIcon() {
		super( null );
	}

	public void setOriginal(Icon i)
	{
		while (i instanceof FlipableIcon)
			i = ((FlipableIcon) i).getOriginal();

		original = i;
	}

}
