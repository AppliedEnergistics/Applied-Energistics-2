
package appeng.api.parts;


import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;


public interface IPartBakedModel
{
	List<BakedQuad> getPartQuads( @Nullable Long partFlags, long rand );
}
