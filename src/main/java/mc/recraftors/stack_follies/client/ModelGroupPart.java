package mc.recraftors.stack_follies.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public abstract sealed class ModelGroupPart {
    final String name;

    public ModelGroupPart(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static ModelGroupPart fromJson(JsonElement json, List<ModelElement> elements) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            int i = json.getAsInt();
            ModelElement e = elements.get(i);
            return new ModelGroupElement(((NamedElementAccessor)e).sf_getElemName(), json.getAsInt());
        }
        if (!json.isJsonObject()) throw new IllegalArgumentException();
        JsonObject object = json.getAsJsonObject();
        String name = object.get("name").getAsString();
        JsonArray originArray = object.getAsJsonArray("origin");
        int[] origin = new int[originArray.size()];
        for (int i = 0; i < originArray.size(); i++) origin[i] = originArray.get(i).getAsInt();
        ModelGroupPart[] children = object.getAsJsonArray("children").asList().stream()
                .map(e -> ModelGroupPart.fromJson(e, elements)).toArray(ModelGroupPart[]::new);
        return new ModelGroupGroup(name, origin, children);
    }

    public abstract GroupedModelPart fillIn(Map<String, GroupedModelPart> modelMap, boolean usesOcclusion,
                                            boolean hasDepth, boolean isSideLit, Sprite sprite,
                                            ModelTransformation transformation, ModelOverrideList overrideList);

    public static final class ModelGroupElement extends ModelGroupPart {
        final int element;

        public ModelGroupElement(String name, int element) {
            super(name);
            this.element = element;
        }

        @Override
        public GroupedModelPart fillIn(Map<String, GroupedModelPart> modelMap, boolean usesOcclusion, boolean hasDepth,
                                       boolean isSideLit, Sprite sprite, ModelTransformation transformation,
                                       ModelOverrideList overrideList) {
            GroupedModelPart target = modelMap.get(this.name);
            System.out.println(this.name);
            System.out.printf("[[ %s ]]%n", modelMap.keySet());
            return GroupedModelPart.element(this.name, target);
        }
    }

    public static final class ModelGroupGroup extends ModelGroupPart {
        final int[] origin;
        final ModelGroupPart[] children;

        public ModelGroupGroup(String name, int[] origin, ModelGroupPart[] children) {
            super(name);
            this.origin = Arrays.copyOf(origin, 3);
            this.children = Arrays.copyOf(children, children.length);
        }

        @Override
        public GroupedModelPart fillIn(Map<String, GroupedModelPart> modelMap, boolean usesOcclusion, boolean hasDepth, boolean isSideLit, Sprite sprite, ModelTransformation transformation, ModelOverrideList overrideList) {
            Map<String, GroupedModelPart> childrenMap = Arrays.stream(this.children)
                    .map(m -> m.fillIn(modelMap, usesOcclusion, hasDepth, isSideLit, sprite, transformation, overrideList))
                    .collect(Collectors.toMap(GroupedModelPart::sf_getName, e -> e));
            return GroupedModelPart.group(childrenMap, this.name, usesOcclusion, hasDepth, isSideLit, sprite, transformation, overrideList);
        }
    }
}
