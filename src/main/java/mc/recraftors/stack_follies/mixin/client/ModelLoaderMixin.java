package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.client.StackFolliesClient;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.ModelLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ModelLoader.class)
@Environment(EnvType.CLIENT)
public abstract class ModelLoaderMixin {
    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/profiler/Profiler;push(Ljava/lang/String;)V"))
    private void sf_preReloadClearModels(CallbackInfo ci) {
        StackFolliesClient.modelReloadHandler();
    }
}
