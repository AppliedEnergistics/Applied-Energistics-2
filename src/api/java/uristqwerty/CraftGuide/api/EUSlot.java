package uristqwerty.CraftGuide.api;

import java.util.ArrayList;
import java.util.List;


public class EUSlot implements Slot
{
	public static final int VALUE_UNSPECIFIED = Integer.MIN_VALUE;
	private int constantValue = VALUE_UNSPECIFIED;
	private int constantSize = VALUE_UNSPECIFIED;
	private final int x, y;
	private final static int width = 16;
	private final static int height = 16;

	private static NamedTexture image;

	public EUSlot(int x, int y)
	{
		this.x = x;
		this.y = y;

		if(image == null)
		{
			image = Util.instance.getTexture("EU-icon");
		}
	}

	@Override
	public void draw(Renderer renderer, int recipeX, int recipeY, Object[] data, int dataIndex, boolean isMouseOver)
	{
		renderer.renderRect(recipeX + x, recipeY + y, width, height, image);
	}

	@Override
	public ItemFilter getClickedFilter(int x, int y, Object[] data, int dataIndex)
	{
		return null;
	}

	@Override
	public boolean isPointInBounds(int x, int y, Object[] data, int dataIndex)
	{
		return x >= this.x && x < this.x + width
			&& y >= this.y && y < this.y + height;
	}

	@Override
	public List<String> getTooltip(int x, int y, Object[] data, int dataIndex)
	{
		int value = EUVaue(data[dataIndex]);
		int size = PacketSize(data[dataIndex]);

		ArrayList<String> tooltip = new ArrayList<String>();
		tooltip.add("\u00a7fIC2 energy");

		if(value != -1)
		{
			tooltip.add("\u00a77  " + Math.abs(value) + " EU " + (value > 0? "produced" : "used"));
		}

		if(size != -1)
		{
			tooltip.add("\u00a77  " + size + " EU/t");
		}

		return tooltip;
	}

	@Override
	public boolean matches(ItemFilter filter, Object[] data, int dataIndex, SlotType type)
	{
		return false;
	}

	public int EUVaue(Object data)
	{
		if(data instanceof Object[] && ((Object[])data)[0] instanceof Integer)
		{
			return (Integer)((Object[])data)[0];
		}
		else if(constantValue != VALUE_UNSPECIFIED)
		{
			return constantValue;
		}
		else if(data instanceof Integer)
		{
			return (Integer)data;
		}
		else
		{
			return VALUE_UNSPECIFIED;
		}
	}

	public int PacketSize(Object data)
	{
		if(data instanceof Object[] && ((Object[])data)[1] instanceof Integer)
		{
			return (Integer)((Object[])data)[1];
		}
		else if(constantSize != VALUE_UNSPECIFIED)
		{
			return constantSize;
		}
		else if(data instanceof Integer)
		{
			return (Integer)data;
		}
		else
		{
			return VALUE_UNSPECIFIED;
		}
	}

	public EUSlot setConstantEUValue(int value)
	{
		constantValue = value;
		return this;
	}
	public EUSlot setConstantPacketSize(int size)
	{
		constantSize = size;
		return this;
	}
}
