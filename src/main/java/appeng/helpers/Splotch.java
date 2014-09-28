package appeng.helpers;

import io.netty.buffer.ByteBuf;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import appeng.api.util.AEColor;

public class Splotch
{

	public Splotch(AEColor col, boolean lit, ForgeDirection side, Vec3 Pos) {
		color = col;
		lumen = lit;

		double x, y;

		if ( side == ForgeDirection.SOUTH || side == ForgeDirection.NORTH )
		{
			x = Pos.xCoord;
			y = Pos.yCoord;
		}

		else if ( side == ForgeDirection.UP || side == ForgeDirection.DOWN )
		{
			x = Pos.xCoord;
			y = Pos.zCoord;
		}

		else
		{
			x = Pos.yCoord;
			y = Pos.zCoord;
		}

		int a = (int) (x * 0xF);
		int b = (int) (y * 0xF);
		this.pos = a | (b << 4);

		this.side = side;
	}

	public Splotch(ByteBuf data) {

		pos = data.readByte();
		int val = data.readByte();

		side = ForgeDirection.getOrientation( val & 0x07 );
		color = AEColor.values()[(val >> 3) & 0x0F];
		lumen = ((val >> 7) & 0x01) > 0;
	}

	public void writeToStream(ByteBuf stream)
	{
		stream.writeByte( pos );
		int val = side.ordinal() | (color.ordinal() << 3) | (lumen ? 0x80 : 0x00);
		stream.writeByte( val );
	}

	final private int pos;
	final public ForgeDirection side;
	final public boolean lumen;
	final public AEColor color;

	public float x()
	{
		return (float) (pos & 0x0f) / 15.0f;
	}

	public float y()
	{
		return (float) ((pos >> 4) & 0x0f) / 15.0f;
	}

	public int getSeed()
	{
		int val = side.ordinal() | (color.ordinal() << 3) | (lumen ? 0x80 : 0x00);
		return Math.abs( pos + val );
	}
}
