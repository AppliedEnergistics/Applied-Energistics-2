package appeng.client.guidebook.compiler;

import appeng.libs.mdast.model.MdAstNode;

import java.util.List;

@FunctionalInterface
public interface NodeHandler<T> {
    void handle(T node, List<MdAstNode> ancestors);
}
