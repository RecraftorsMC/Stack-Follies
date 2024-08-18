package mc.recraftors.stack_follies.client;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Arrays;

public class ModelGroupElement {
    final GroupType type;
    final String name;
    final int[] origin;
    final int element;
    final ModelGroupElement[] children;

    public ModelGroupElement(int i) {
        this.type = GroupType.ELEMENT;
        this.name = null;
        this.origin = null;
        this.element = i;
        this.children = null;
    }

    public ModelGroupElement(String name, int[] origin, ModelGroupElement[] children) {
        this.type = GroupType.GROUP;
        this.name = name;
        this.origin = Arrays.copyOf(origin, 3);
        this.element = -1;
        this.children = Arrays.copyOf(children, children.length);
    }

    /**
     * @throws UnsupportedOperationException When applied on an {@code element} group element
     */
    public ModelGroupElement withElement(int i) {
        if (this.type == GroupType.ELEMENT) throw new UnsupportedOperationException();
        ModelGroupElement child = new ModelGroupElement(i);
        ModelGroupElement[] arr = Arrays.copyOf(this.children, this.children.length+1);
        arr[this.children.length] = child;
        return new ModelGroupElement(this.name, this.origin, arr);
    }

    public GroupType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int[] getOrigin() {
        return origin;
    }

    public int getElement() {
        return element;
    }

    public ModelGroupElement[] getChildren() {
        return Arrays.copyOf(this.children, this.children.length);
    }

    public static ModelGroupElement fromJson(JsonElement json) {
        if (json.isJsonPrimitive() && json.getAsJsonPrimitive().isNumber()) {
            return new ModelGroupElement(json.getAsInt());
        }
        if (!json.isJsonObject()) throw new IllegalArgumentException();
        JsonObject object = json.getAsJsonObject();
        String name = object.get("name").getAsString();
        JsonArray originArray = object.getAsJsonArray("origin");
        int[] origin = new int[originArray.size()];
        for (int i = 0; i < originArray.size(); i++) origin[i] = originArray.get(i).getAsInt();
        ModelGroupElement[] children = object.getAsJsonArray("children").asList().stream().map(ModelGroupElement::fromJson).toArray(ModelGroupElement[]::new);
        return new ModelGroupElement(name, origin, children);
    }

    public enum GroupType {
        ELEMENT, GROUP
    }
}
