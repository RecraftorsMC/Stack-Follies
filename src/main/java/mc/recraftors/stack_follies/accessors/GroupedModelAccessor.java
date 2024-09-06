package mc.recraftors.stack_follies.accessors;

import mc.recraftors.stack_follies.client.ModelGroupPart;

import java.util.List;

public interface GroupedModelAccessor {
    default boolean sf_isGrouped() {
        return false;
    }

    default List<ModelGroupPart> sf_getGroups() {
        return List.of();
    }

    default void sf_setGrouped(boolean b) {}

    default void sf_setGroups(List<ModelGroupPart> list) {}
}
