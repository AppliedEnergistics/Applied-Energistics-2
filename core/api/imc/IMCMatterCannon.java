/* Example:

NBTTagCompound msg = new NBTTagCompound();
NBTTagCompound item = new NBTTagCompound();

new ItemStack( Blocks.anvil ).writeToNBT( item );
msg.setTag( "item", item );
msg.setDouble( "weight", 32.0 );

FMLInterModComms.sendMessage( "appliedenergistics2", "add-mattercannon-ammo", msg );

 */
package appeng.core.api.imc;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.AEApi;
import appeng.core.api.IIMCHandler;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;

public class IMCMatterCannon implements IIMCHandler
{

	@Override
	public void post(IMCMessage m)
	{
		NBTTagCompound msg = m.getNBTValue();
		NBTTagCompound item = (NBTTagCompound) msg.getTag( "item" );

		ItemStack ammo = ItemStack.loadItemStackFromNBT( item );
		double weight = msg.getDouble( "weight" );

		if ( ammo == null )
			throw new RuntimeException( "invalid item" );

		AEApi.instance().registries().matterCannon().registerAmmo( ammo, weight );
	}
}
