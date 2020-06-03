package appeng.items.materials;


import appeng.api.implementations.items.IStorageComponent;
import appeng.items.AEBaseItem;
import net.minecraft.item.ItemStack;


public class ItemStorageComponent extends AEBaseItem implements IStorageComponent
{

	private static final int KILO_SCALAR = 1024;

	public ItemStorageComponent( ComponentType type, Properties properties )
	{
		super(properties);
		this.type = type;
	}

	public enum ComponentType
	{
		CELL1K_PART(KILO_SCALAR),
		CELL4K_PART(KILO_SCALAR * 4),
		CELL16K_PART(KILO_SCALAR * 16),
		CELL64K_PART(KILO_SCALAR * 64);

		private final int byteCount;

		ComponentType( int byteCount )
		{
			this.byteCount = byteCount;
		}

		public int getBytes()
		{
			return byteCount;
		}
	}

	private final ComponentType type;

	@Override
	public int getBytes( ItemStack is )
	{
		return type.getBytes();
	}

	@Override
	public boolean isStorageComponent( ItemStack is )
	{
		return true;
	}

}
