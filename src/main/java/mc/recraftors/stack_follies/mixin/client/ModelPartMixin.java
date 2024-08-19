package mc.recraftors.stack_follies.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

@Mixin(ModelPart.class)
@Environment(EnvType.CLIENT)
public abstract class ModelPartMixin {
    @Shadow @Final private Map<String, ModelPart> children;

    @Inject(method = "getChild", at = @At(value = "INVOKE", target = "Ljava/util/Map;get(Ljava/lang/Object;)Ljava/lang/Object;"), cancellable = true)
    private void sf_getChildBFS(String name, CallbackInfoReturnable<ModelPart> cir, @Local ModelPart modelPart, @Share("q") LocalRef<Queue<ModelPart>> queue) {
        boolean b = false;
        if (queue.get() == null) {
            b = true;
            queue.set(new ArrayDeque<>());
        }
        if (modelPart == null) {
            for (Map.Entry<String, ModelPart> e : this.children.entrySet()) {
                if (e.getValue().hasChild(name)) {
                    cir.setReturnValue(e.getValue().getChild(name));
                    return;
                }
                queue.get().add(e.getValue());
            }
        }
        if (b) {
            while (!queue.get().isEmpty()) {
                modelPart = queue.get().remove().getChild(name);
                if (modelPart != null) {
                    cir.setReturnValue(modelPart);
                    return;
                }
            }
        }
    }
}
