package mc.recraftors.stack_follies.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class StackFolliesClient implements ClientModInitializer {
    public static final String MODEL_GROUP_PROCESSOR_KEY = "sf$computeGroups";
    public static final String MODEL_ELEM_NAME_KEY = "name";

    @Override
    public void onInitializeClient() {
    }

    public static void animate(GroupedBakedModel model, Animation animation, long runningTime, float scale, Vector3f interpolation) {
        float f = getRunningSeconds(animation, runningTime);

        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            Optional<ModelGroupElement> optional = model.getSourceAccessor().sf_getChild(entry.getKey());
            if (optional.isEmpty()) continue;
            ModelGroupElement group = optional.get();
            List<Transformation> list = entry.getValue();
            list.forEach(transformation -> {
                Keyframe[] keyframes = transformation.keyframes();
                int i = Math.max(0, MathHelper.binarySearch(0, keyframes.length, (index) -> f <= keyframes[index].timestamp()) - 1);
                int j = Math.min(keyframes.length - 1, i + 1);
                Keyframe k1 = keyframes[i];
                Keyframe k2 = keyframes[2];
                float h = f - k1.timestamp();
                float k = i == j ? 0 : MathHelper.clamp(h / (k2.timestamp() - k1.timestamp()), 0, 1);

                k2.interpolation().apply(interpolation, k, keyframes, i, j, scale);
                //TODO bake cuboid
            });
        }
    }

	private static float getRunningSeconds(Animation animation, long runningTime) {
		float f = (float)runningTime / 1000.0F;
		return animation.looping() ? f % animation.lengthInSeconds() : f;
	}
}
