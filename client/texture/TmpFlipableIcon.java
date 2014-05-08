package appeng.client.texture;

import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class TmpFlipableIcon extends FlipableIcon
{

	private static final IIcon nullIcon = new MissingIcon( Blocks.diamond_block );

	public TmpFlipableIcon() {
		super( nullIcon );
	}

	public void setOriginal(IIcon i)
	{
		while (i instanceof FlipableIcon)
			i = ((FlipableIcon) i).getOriginal();

		if ( i == null )
			original = nullIcon;
		else
			original = i;
	}

}
