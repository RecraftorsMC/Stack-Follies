package mc.recraftors.stack_follies.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import mc.recraftors.stack_follies.client.GroupedModelPart;
import mc.recraftors.stack_follies.client.ModelGroupPart;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.*;
import net.minecraft.client.render.model.json.JsonUnbakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.SpriteIdentifier;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.*;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin implements GroupedModelAccessor {

    @Shadow public abstract List<ModelElement> getElements();

    @Shadow protected abstract ModelOverrideList compileOverrides(Baker baker, JsonUnbakedModel parent);

    @Shadow public abstract JsonUnbakedModel.GuiLight getGuiLight();

    @Shadow @Final private ModelTransformation transformations;

    @Shadow public abstract boolean useAmbientOcclusion();

    @Unique private boolean sf_grouped = false;
    @Unique private final List<ModelGroupPart> sf_groups = new ArrayList<>();

    @Override
    public boolean sf_isGrouped() {
        return this.sf_grouped;
    }

    @Override
    public List<ModelGroupPart> sf_getGroups() {
        return this.sf_groups;
    }

    @Override
    public void sf_setGrouped(boolean b) {
        this.sf_grouped = b;
    }

    @Override
    public void sf_setGroups(List<ModelGroupPart> list) {
        this.sf_groups.clear();
        this.sf_groups.addAll(list);
    }

    @Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At("HEAD")
    )
    private void sf_bakeHeadInit(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                                 ModelBakeSettings settings, Identifier id, boolean hasDepth,
                                 CallbackInfoReturnable<BakedModel> cir,
                                 @Share("a") LocalRef<Map<String, Map<Direction, BakedQuad>>> ref) {
        if (!this.sf_isGrouped()) return;
        ref.set(new HashMap<>(this.getElements().size()));
    }

    @Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Ljava/util/Map;keySet()Ljava/util/Set;")
    )
    private void sf_bakeLoopInit(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                                 ModelBakeSettings settings, Identifier id, boolean hasDepth,
                                 CallbackInfoReturnable<BakedModel> cir,
                                 @Local ModelElement modelElement,
                                 @Share("a") LocalRef<Map<String, Map<Direction, BakedQuad>>> ref,
                                 @Share("b") LocalRef<Map<Direction, BakedQuad>> ref2) {
        if (!this.sf_isGrouped()) return;
        Map<Direction, BakedQuad> map = new HashMap<>(6);
        ref2.set(map);
        ref.get().put(((NamedElementAccessor)modelElement).sf_getElemName(), map);
    }

    @Redirect(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BasicBakedModel$Builder;addQuad(Lnet/minecraft/client/render/model/BakedQuad;)Lnet/minecraft/client/render/model/BasicBakedModel$Builder;")
    )
    private BasicBakedModel.Builder sf_bakeQuadNoCull(BasicBakedModel.Builder instance, BakedQuad quad,
                                                      @Local Direction direction,
                                                      @Share("b") LocalRef<Map<Direction, BakedQuad>> ref) {
        if (!this.sf_isGrouped()) {
            instance.addQuad(quad);
        } else {
            ref.get().put(direction, quad);
        }
        return instance;
    }

    @Redirect(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/model/BasicBakedModel$Builder;addQuad(Lnet/minecraft/util/math/Direction;Lnet/minecraft/client/render/model/BakedQuad;)Lnet/minecraft/client/render/model/BasicBakedModel$Builder;")
    )
    private BasicBakedModel.Builder sf_bakeQuadCull(BasicBakedModel.Builder instance, Direction side, BakedQuad quad,
                                                    @Share("b") LocalRef<Map<Direction, BakedQuad>> ref) {
        if (!this.sf_isGrouped()) {
            return instance.addQuad(side, quad);
        }
        ref.get().put(side, quad);
        return instance;
    }

    @Redirect(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Function;apply(Ljava/lang/Object;)Ljava/lang/Object;", ordinal = 0)
    )
    private <T,R> R sf_bakeSprite(Function<T,R> instance, T t,
                                 @Share("c") LocalRef<R> ref) {
        ref.set(instance.apply(t));
        return ref.get();
    }

    @Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true
    )
    private void sf_bakeGroups(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                               ModelBakeSettings settings, Identifier id, boolean hasDepth,
                               CallbackInfoReturnable<BakedModel> cir,
                               @Share("a") LocalRef<Map<String, Map<Direction, BakedQuad>>> mapRef,
                               @Share("c") LocalRef<?> spriteRef) {
        if (this.sf_isGrouped()) {
            Sprite sprite = (Sprite) spriteRef.get();
            Map<String, Map<Direction, BakedQuad>> map = mapRef.get();
            Map<String, GroupedModelPart> modelMap = new HashMap<>();
            Map<String, GroupedModelPart> treeMap = new HashMap<>();
            ModelOverrideList overrideList = this.compileOverrides(baker, parent);

            map.forEach((k, m) -> {
                BasicBakedModel.Builder builder = new BasicBakedModel.Builder((JsonUnbakedModel) ((Object)this), overrideList, hasDepth).setParticle(sprite);
                m.forEach(builder::addQuad);
                modelMap.put(k, GroupedModelPart.element(k, builder.build()));
            });

            this.sf_getGroups().forEach(
                    element -> treeMap.put(element.getName(), element.fillIn(modelMap, this.useAmbientOcclusion(), hasDepth, this.getGuiLight().isSide(), sprite, transformations, overrideList))
            );

            BakedModel n = GroupedModelPart.group(treeMap, null, this.useAmbientOcclusion(), hasDepth, this.getGuiLight().isSide(), sprite, transformations, overrideList);
            cir.setReturnValue(n);
        }
    }
}
