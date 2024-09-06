package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.client.ModelGroupPart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BasicBakedModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Environment(EnvType.CLIENT)
@Mixin(BasicBakedModel.class)
public abstract class BasicBakedModelMixin implements GroupedModelAccessor {
    @Unique private boolean sf_grouped = false;
    @Unique private final List<ModelGroupPart> sf_groupElements = new ArrayList<>();

    @Override
    public void sf_setGrouped(boolean b) {
        this.sf_grouped = b;
    }

    @Override
    public boolean sf_isGrouped() {
        return this.sf_grouped;
    }

    @Override
    public void sf_setGroups(List<ModelGroupPart> list) {
        this.sf_groupElements.clear();
        this.sf_groupElements.addAll(list);
    }

    @Override
    public List<ModelGroupPart> sf_getGroups() {
        return List.copyOf(this.sf_groupElements);
    }
}
