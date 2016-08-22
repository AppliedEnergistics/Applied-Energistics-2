package appeng.bootstrap.components;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.Item;
import net.minecraftforge.fml.relauncher.Side;


public class ItemColorComponent implements InitComponent
{

	private final Item item;

	private final IItemColor itemColor;

	public ItemColorComponent( Item item, IItemColor itemColor )
	{
		this.item = item;
		this.itemColor = itemColor;
	}

	@Override
	public void initialize( Side side )
	{
		Minecraft.getMinecraft().getItemColors().registerItemColorHandler( itemColor, item );
	}
}
