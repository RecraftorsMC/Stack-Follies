package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.client.ModelGroupElement;
import mc.recraftors.stack_follies.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.*;

@Environment(EnvType.CLIENT)
@Mixin(BasicBakedModel.class)
public abstract class BasicBakedModelMixin implements GroupedModelAccessor {
    @Unique private boolean sf_grouped = false;
    @Unique private final List<ModelGroupElement> sf_groupElements = new ArrayList<>();
    @Unique private final List<ModelElement> sf_modelElements = new ArrayList<>();
    @Unique private final Map<String, ModelGroupElement> sf_namedGroups = new HashMap<>();
    @Unique private int sf_textureSizeX;
    @Unique private int sf_textureSizeY;

    @Override
    public void sf_setGrouped(boolean b) {
        this.sf_grouped = b;
    }

    @Override
    public boolean sf_isGrouped() {
        return this.sf_grouped;
    }

    @Override
    public void sf_setGroups(List<ModelGroupElement> list) {
        this.sf_groupElements.clear();
        this.sf_namedGroups.clear();
        this.sf_groupElements.addAll(list);
        list.forEach(this::sf_addGroup);
    }

    @Override
    public void sf_setElements(List<ModelElement> list) {
        this.sf_modelElements.clear();
        this.sf_modelElements.addAll(list);
    }

    @Unique
    private void sf_addGroup(ModelGroupElement e) {
        if (e.getChildren() == null) return;
        this.sf_namedGroups.putIfAbsent(e.getName(), e);
        Arrays.stream(e.getChildren()).forEach(this::sf_addGroup);
    }

    @Override
    public List<ModelGroupElement> sf_getGroups() {
        return List.copyOf(this.sf_groupElements);
    }

    @Override
    public List<ModelElement> sf_getElements() {
        return List.copyOf(this.sf_modelElements);
    }

    @Override
    public Optional<ModelGroupElement> sf_getChild(String s) {
        return Optional.ofNullable(this.sf_namedGroups.getOrDefault(s, null));
    }

    @Override
    public void sf_setTextureSize(int x, int y) {
        this.sf_textureSizeX = x;
        this.sf_textureSizeY = y;
    }

    @Override
    public Pair<Integer, Integer> sf_getSize() {
        return Pair.of(this.sf_textureSizeX, this.sf_textureSizeY);
    }
}
