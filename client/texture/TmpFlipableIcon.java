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
		setFlip( false, false );

		while (i instanceof FlipableIcon)
		{
			FlipableIcon fi = (FlipableIcon) i;
			if ( fi.flip_u )
				this.flip_u = !this.flip_u;

			if ( fi.flip_v )
				this.flip_v = !this.flip_v;

			i = fi.getOriginal();
		}

		if ( i == null )
			original = nullIcon;
		else
			original = i;
	}

}
