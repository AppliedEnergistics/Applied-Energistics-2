package appeng.client.texture;

import net.minecraft.util.IIcon;

public class TmpFlipableIcon extends FlipableIcon
{

	public TmpFlipableIcon() {
		super( null );
	}

	public void setOriginal(IIcon i)
	{
		while (i instanceof FlipableIcon)
			i = ((FlipableIcon) i).getOriginal();

		original = i;
	}

}
