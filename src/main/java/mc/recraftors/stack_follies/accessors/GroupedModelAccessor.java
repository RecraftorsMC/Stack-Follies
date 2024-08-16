package mc.recraftors.stack_follies.accessors;

import mc.recraftors.stack_follies.client.ModelGroupElement;
import mc.recraftors.stack_follies.util.Pair;
import net.minecraft.client.render.model.json.ModelElement;

import java.util.List;
import java.util.Optional;

public interface GroupedModelAccessor {
    default boolean sf_isGrouped() {
        return false;
    }

    default List<ModelGroupElement> sf_getGroups() {
        return List.of();
    }

    default void sf_setGrouped(boolean b) {}

    default void sf_setGroups(List<ModelGroupElement> list) {}

    default void sf_setElements(List<ModelElement> list) {}

    default void sf_setTextureSize(int x, int y) {}

    default List<ModelElement> sf_getElements() {
        return List.of();
    }

    default Optional<ModelGroupElement> sf_getChild(String s) {
        return Optional.empty();
    }

    default Pair<Integer, Integer> sf_getSize() {
        return Pair.of(0, 0);
    }
}
