package mc.recraftors.stack_follies.mixin.client;

import com.mojang.datafixers.util.Either;
import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.client.GroupedBakedModelBuilder;
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
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Environment(EnvType.CLIENT)
@Mixin(JsonUnbakedModel.class)
public abstract class JsonUnbakedModelMixin implements GroupedModelAccessor {

    @Shadow public abstract List<ModelElement> getElements();

    @Shadow @Final protected Map<String, Either<SpriteIdentifier, String>> textureMap;
    @Unique private boolean sf_grouped = false;
    @Unique private final List<ModelGroupElement> sf_groups = new ArrayList<>();
    @Unique private int sf_textureSizeX;
    @Unique private int sf_textureSizeY;

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

    @Override
    public void sf_setTextureSize(int x, int y) {
        this.sf_textureSizeX = x;
        this.sf_textureSizeY = y;
    }

    @Inject(
            method = "bake(Lnet/minecraft/client/render/model/Baker;Lnet/minecraft/client/render/model/json/JsonUnbakedModel;Ljava/util/function/Function;Lnet/minecraft/client/render/model/ModelBakeSettings;Lnet/minecraft/util/Identifier;Z)Lnet/minecraft/client/render/model/BakedModel;",
            at = @At(value = "RETURN", ordinal = 1),
            cancellable = true
    )
    private void sf_bakeGroups(Baker baker, JsonUnbakedModel parent, Function<SpriteIdentifier, Sprite> textureGetter,
                               ModelBakeSettings settings, Identifier id, boolean hasDepth,
                               CallbackInfoReturnable<BakedModel> cir) {
        if (this.sf_grouped) {
            BakedModel model = cir.getReturnValue();
            if (!(model instanceof GroupedModelAccessor accessor)) return;
            accessor.sf_setGrouped(true);
            accessor.sf_setGroups(this.sf_groups);
            accessor.sf_setElements(this.getElements());
            accessor.sf_setTextureSize(this.sf_textureSizeX, this.sf_textureSizeY);
            accessor.sf_setTextureMap(this.textureMap.entrySet().stream()
                    .map(e -> new AbstractMap.SimpleImmutableEntry<>(e.getKey(), e.getValue()
                            .mapRight(Identifier::tryParse).mapLeft(SpriteIdentifier::getTextureId)
                            .right().orElseGet(e.getValue().left().get()::getTextureId)))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            cir.setReturnValue(new GroupedBakedModelBuilder(cir.getReturnValue(), accessor));
        }
    }
}
