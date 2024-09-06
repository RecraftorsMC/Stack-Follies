package mc.recraftors.stack_follies.accessors;

import net.minecraft.client.model.ModelPart;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface ModelPartDepthAccessor {

    default Optional<ModelPart> sf_getChild(String name, Set<ModelPartDepthAccessor> set) {
        if (set.add(this)) {
            Map<String, ModelPart> map = this.sf_getChildren();
            if (map.containsKey(name)) return Optional.of(map.get(name));
            for (Map.Entry<String, ModelPart> e : map.entrySet()) {
                Optional<ModelPart> o = ((ModelPartDepthAccessor)((Object)e.getValue())).sf_getChild(name, set);
                if (o.isPresent()) return o;
            }
        }
        return Optional.empty();
    }

    default Optional<ModelPart> sf_getChild(String name) {
        return this.sf_getChild(name, new HashSet<>());
    }

    default Map<String, ModelPart> sf_getChildren() {
        return Map.of();
    }
}
