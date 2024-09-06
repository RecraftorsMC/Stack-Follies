package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Environment(EnvType.CLIENT)
@Mixin(ModelElement.class)
public abstract class ModelElementMixin implements NamedElementAccessor {
    @Unique private String sf_elemName;

    @Override
    public void sf_setElemName(String s) {
        this.sf_elemName = s;
    }

    @Override
    public String sf_getElemName() {
        return this.sf_elemName;
    }
}
