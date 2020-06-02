
package appeng.api.parts;


import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.model.BakedQuad;


public interface IPartBakedModel
{
	List<BakedQuad> getPartQuads( @Nullable Long partFlags, Random rand );
}
