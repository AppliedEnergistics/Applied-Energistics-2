package appeng.util.item;

import java.util.List;

import net.minecraft.item.Item;
import appeng.api.storage.data.IAETagCompound;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class AEItemDef
{

	public int myHash;

	public int def;

	public Item item;
	public int damageValue;

	public int dspDamage;
	public int maxDamage;

	public IAETagCompound tagCompound;

	@SideOnly(Side.CLIENT)
	public String displayName;

	@SideOnly(Side.CLIENT)
	public List tooltip;

	public AEItemDef copy()
	{
		AEItemDef t = new AEItemDef();
		t.def = def;
		t.item = item;
		t.damageValue = damageValue;
		t.dspDamage = dspDamage;
		t.maxDamage = maxDamage;
		return t;
	}
}
