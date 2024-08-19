package mc.recraftors.stack_follies.mixin.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import mc.recraftors.stack_follies.client.StackFolliesClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;

@Environment(EnvType.CLIENT)
@Mixin(ModelElement.Deserializer.class)
public abstract class ModelElementDeserializerMixin {

    @Inject(
            method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/ModelElement;",
            at = @At("TAIL")
    )
    private void sf_deserializeReturnNamed(JsonElement element, Type type, JsonDeserializationContext context,
                                           CallbackInfoReturnable<ModelElement> cir) {
        JsonObject object = element.getAsJsonObject();
        if (object.has(StackFolliesClient.MODEL_ELEM_NAME_KEY)) {
            JsonElement e = object.get(StackFolliesClient.MODEL_ELEM_NAME_KEY);
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isString()) {
                String s = e.getAsString();
                ((NamedElementAccessor)cir.getReturnValue()).sf_setElemName(s);
            }
        }
    }
}
