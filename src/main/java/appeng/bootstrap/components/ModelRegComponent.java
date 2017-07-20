
package appeng.bootstrap.components;


import appeng.bootstrap.IBootstrapComponent;
import net.minecraftforge.fml.relauncher.Side;


/**
 * @author GuntherDW
 */

@FunctionalInterface
public interface ModelRegComponent extends IBootstrapComponent
{

	void modelReg( Side side );

}
