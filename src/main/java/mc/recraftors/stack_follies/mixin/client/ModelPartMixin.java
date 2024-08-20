package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.accessors.ModelPartDepthAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(ModelPart.class)
@Environment(EnvType.CLIENT)
public abstract class ModelPartMixin implements ModelPartDepthAccessor {
    @Shadow @Final private Map<String, ModelPart> children;

    @Override
    public Map<String, ModelPart> sf_getChildren() {
        return Map.copyOf(this.children);
    }
}
