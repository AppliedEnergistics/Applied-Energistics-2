package appeng.transformer.template;

import net.minecraft.nbt.NBTTagCompound;
import appeng.transformer.asm.ASMMigration;

public class ItemStackTemplate
{

	public void readFromNBT(NBTTagCompound par1NBTTagCompound)
	{
		ASMMigration.handleMigration( this );
		return;
	}

}
