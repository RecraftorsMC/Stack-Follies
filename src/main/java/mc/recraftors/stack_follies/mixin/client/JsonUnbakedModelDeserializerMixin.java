package mc.recraftors.stack_follies.mixin.client;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.client.ModelGroupElement;
import mc.recraftors.stack_follies.client.StackFolliesClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.lang.reflect.Type;
import java.util.List;

@Environment(EnvType.CLIENT)
@Mixin(JsonUnbakedModel.Deserializer.class)
public abstract class JsonUnbakedModelDeserializerMixin {
    @Inject(method = "deserialize(Lcom/google/gson/JsonElement;Ljava/lang/reflect/Type;Lcom/google/gson/JsonDeserializationContext;)Lnet/minecraft/client/render/model/json/JsonUnbakedModel;", at = @At("RETURN"))
    private void sf_modelDeserializeGroupInjector(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext, CallbackInfoReturnable<JsonUnbakedModel> cir) {
        JsonObject object = jsonElement.getAsJsonObject();
        boolean b = false;
        if (object.has(StackFolliesClient.MODEL_GROUP_PROCESSOR_KEY)) {
            JsonElement e = object.get(StackFolliesClient.MODEL_GROUP_PROCESSOR_KEY);
            if (e.isJsonPrimitive() && e.getAsJsonPrimitive().isBoolean()) {
                b = e.getAsBoolean();
            }
        }
        if (!b) return;
        JsonElement e = object.get("groups");
        JsonElement t = object.get("texture_size");
        if (t == null || e == null || !e.isJsonArray() || !t.isJsonArray()) return;
        List<ModelGroupElement> list = e.getAsJsonArray().asList().stream().map(ModelGroupElement::fromJson).toList();
        int x = t.getAsJsonArray().get(0).getAsInt();
        int y = t.getAsJsonArray().get(1).getAsInt();
        JsonUnbakedModel model = cir.getReturnValue();
        ((GroupedModelAccessor)model).sf_setGrouped(true);
        ((GroupedModelAccessor)model).sf_setGroups(list);
        ((GroupedModelAccessor)model).sf_setTextureSize(x, y);
    }
}
