package appeng.client.gui.config;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import cpw.mods.fml.client.config.GuiConfig;
import cpw.mods.fml.client.config.IConfigElement;

public class AEConfigGui extends GuiConfig
{

	private static List<IConfigElement> getConfigElements()
	{
		List<IConfigElement> list = new ArrayList<IConfigElement>();

		for (String cat : AEConfig.instance.getCategoryNames())
		{
			if ( cat.equals( "versionchecker" ) )
				continue;

			if ( cat.equals( "settings" ) )
				continue;

			ConfigCategory cc = AEConfig.instance.getCategory( cat );

			if ( cc.isChild() )
				continue;

			ConfigElement ce = new ConfigElement( cc );
			list.add( ce );
		}

		return list;
	}

	public AEConfigGui(GuiScreen parent) {
		super( parent, getConfigElements(), AppEng.modid, false, false, GuiConfig.getAbridgedConfigPath( AEConfig.instance.getFilePath() ) );
	}

}
