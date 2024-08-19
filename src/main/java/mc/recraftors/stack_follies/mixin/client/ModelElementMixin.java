package mc.recraftors.stack_follies.mixin.client;

import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelElementFace;
import net.minecraft.client.render.model.json.ModelRotation;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Environment(EnvType.CLIENT)
@Mixin(ModelElement.class)
public abstract class ModelElementMixin implements NamedElementAccessor {
    @Shadow @Final public ModelRotation rotation;
    @Unique private String sf_elemName;
    @Unique private float sf_uvX;
    @Unique private float sf_uvY;

    @Unique private static final Direction[] SF_X_ARR = new Direction[]{
            Direction.UP, Direction.DOWN, Direction.EAST, Direction.NORTH, Direction.WEST, Direction.SOUTH
    };
    @Unique private static final Direction[] SF_Y_ARR = new Direction[]{
            Direction.EAST, Direction.UP, Direction.NORTH, Direction.DOWN, Direction.WEST, Direction.SOUTH
    };

    @Inject(method = "<init>", at = @At("RETURN"))
    private void sf_initFaceUvInit(Vector3f from, Vector3f to, Map<Direction, ModelElementFace> faces,
                                   ModelRotation rotation, boolean shade, CallbackInfo ci) {
        ModelElementFace x = null;
        ModelElementFace y = null;
        for (Direction d : SF_X_ARR) {
            x = faces.get(d);
            if (x != null) break;
        }
        for (Direction d : SF_Y_ARR) {
            y = faces.get(d);
            if (y != null) break;
        }
        this.sf_uvX = x.textureData.uvs[0];
        this.sf_uvY = y.textureData.uvs[1];
    }

    @Override
    public void sf_setElemName(String s) {
        this.sf_elemName = s;
    }

    @Override
    public String sf_getElemName() {
        return this.sf_elemName;
    }

    @Override
    public float sf_getUvX() {
        return this.sf_uvX;
    }

    @Override
    public float sf_getUvY() {
        return this.sf_uvY;
    }

    @Override
    public ModelRotation sf_getRotation() {
        return this.rotation;
    }
}
