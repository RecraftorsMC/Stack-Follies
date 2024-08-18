package mc.recraftors.stack_follies.accessors;

import mc.recraftors.stack_follies.client.ModelGroupElement;
import mc.recraftors.stack_follies.util.Pair;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.Map;
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

    default Pair<Integer, Integer> sf_getTextureSize() {
        return Pair.of(0, 0);
    }

    default void sf_setTextureMap(Map<String, Identifier> map) {}

    default Map<String, Identifier> sf_getTextureMap() {
        return Map.of();
    }
}
