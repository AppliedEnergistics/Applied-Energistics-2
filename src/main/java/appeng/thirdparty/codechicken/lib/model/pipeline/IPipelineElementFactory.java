package appeng.thirdparty.codechicken.lib.model.pipeline;

/**
 * Created by covers1624 on 30/07/18.
 */
@FunctionalInterface
public interface IPipelineElementFactory<T extends IPipelineConsumer> {

    T create();
}
