package mc.recraftors.stack_follies.client;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.AnimationState;
import org.joml.Vector3f;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class GroupedBakedModel extends Model {
    public static final Vector3f INTERPOLATION_DEFAULT = new Vector3f();

    private final Map<String, ModelPart> partMap;
    private final ModelPart root;

    GroupedBakedModel(Map<String, ModelPart> map, ModelPart root) {
        super(RenderLayer::getEntityTranslucent);
        this.partMap = Collections.unmodifiableMap(map);
        this.root = root;
    }

    public void updateAnimation(AnimationState state, Animation animation, float progress) {
        this.updateAnimation(state, animation, progress, 1f);
    }

    public void updateAnimation(AnimationState state, Animation animation, float progress, float speed) {
        state.update(progress, speed);
        state.run(s -> StackFolliesClient.animate(this, animation, s.getTimeRunning(), 1f, INTERPOLATION_DEFAULT));
    }

    Optional<ModelPart> getChild(String key) {
        return Optional.ofNullable(partMap.get(key));
    }

    @Override
    public void render(MatrixStack matrices, VertexConsumer vertices, int light, int overlay, float red, float green, float blue, float alpha) {
        this.root.render(matrices, vertices, light, overlay, red, green, blue, alpha);
    }
}
