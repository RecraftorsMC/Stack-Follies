package mc.recraftors.stack_follies.accessors;

import net.minecraft.client.render.model.json.ModelRotation;

public interface NamedElementAccessor {
    String sf_getElemName();
    void sf_setElemName(String s);
    float sf_getUvX();
    float sf_getUvY();
    ModelRotation sf_getRotation();
}
