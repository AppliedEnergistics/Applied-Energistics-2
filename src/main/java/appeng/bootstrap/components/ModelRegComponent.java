
package appeng.bootstrap.components;


import net.minecraftforge.fml.relauncher.Side;

import appeng.bootstrap.IBootstrapComponent;


/**
 * @author GuntherDW
 */

@FunctionalInterface
public interface ModelRegComponent extends IBootstrapComponent
{

	@Override
	void modelRegistration( Side side );

}
