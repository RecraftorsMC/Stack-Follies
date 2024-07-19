package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.client.ModelGroupElement;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.Baker;
import net.minecraft.client.render.model.ModelBakeSettings;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin implements GroupedModelAccessor {

    @Shadow public abstract List<ModelElement> getElements();

    @Unique private boolean sf_grouped = false;
    @Unique private final List<ModelGroupElement> sf_groups = new ArrayList<>();

    @Override
    public boolean sf_isGrouped() {
        return this.sf_grouped;
    }

    @Override
    public List<ModelGroupElement> sf_getGroups() {
        return this.sf_groups;
    }

    @Override
    public void sf_setGrouped(boolean b) {
        this.sf_grouped = b;
    }

    @Override
    public void sf_setGroups(List<ModelGroupElement> list) {
        this.sf_groups.clear();
        this.sf_groups.addAll(list);
    }

    /*@Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;getElements()Ljava/util/List;")
    )
    private void sf_bakeCEInit(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                               ModelBakeSettings settings, Identifier id, boolean hasDepth, CallbackInfoReturnable<BakedModel> cir,
                               @Share("sf_keyMap") LocalRef<Map<Integer, List<BakedQuad>>> ref, @Share("sf_eC") LocalIntRef count) {
        ref.set(new HashMap<>());
        count.set(0);
    }

    @Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;")
    )
    private void sf_bakeCount(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                              ModelBakeSettings settings, Identifier id, boolean hasDepth,
                              CallbackInfoReturnable<BakedModel> cir, @Share("sf_eC") LocalIntRef count) {
        count.set(count.get()+1);
    }

    @Redirect(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/json/JsonUnbakedModel;createQuad(Lnet/minecraft/client/render/model/json/ModelElement;Lnet/minecraft/client/render/model/json/ModelElementFace;Lnet/minecraft/client/texture/Sprite;Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;)Lnet/minecraft/client/render/model/BakedQuad;")
    )
    private BakedQuad sf_bakeQuad(ModelElement element, ModelElementFace elementFace, Sprite sprite, Direction side,
                                  ModelBakeSettings settings, Identifier id,
                                  @Share("sf_keyMap") LocalRef<Map<Integer, List<BakedQuad>>> ref, @Share("sf_eC") LocalIntRef count) {
        int i = count.get();
        BakedQuad quad = createQuad(element, elementFace, sprite, side, settings, id);
        ref.get().computeIfAbsent(i, n->new ArrayList<>()).add(quad);
        return quad;
    }*/

    @Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "RETURN", ordinal = 1)
    )
    private void sf_bakeGroups(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                               ModelBakeSettings settings, Identifier id, boolean hasDepth,
                               CallbackInfoReturnable<BakedModel> cir/*, @Share("sf_keyMap")LocalRef<Map<Integer, List<BakedQuad>>> ref*/) {
        if (this.sf_grouped) {
            BakedModel model = cir.getReturnValue();
            if (!(model instanceof GroupedModelAccessor accessor)) return;
            accessor.sf_setGrouped(true);
            accessor.sf_setGroups(this.sf_groups);
            accessor.sf_setElements(this.getElements());
        }
    }
}
