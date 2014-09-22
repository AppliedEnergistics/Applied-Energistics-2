package appeng.client.texture;

import net.minecraft.init.Blocks;
import net.minecraft.util.IIcon;

public class TmpFlippableIcon extends FlippableIcon
{

	private static final IIcon nullIcon = new MissingIcon( Blocks.diamond_block );

	public TmpFlippableIcon() {
		super( nullIcon );
	}

	public void setOriginal(IIcon i)
	{
		setFlip( false, false );

		while (i instanceof FlippableIcon)
		{
			FlippableIcon fi = (FlippableIcon) i;
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
