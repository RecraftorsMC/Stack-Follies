package mc.recraftors.stack_follies.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.block.StainedGlassPaneBlock;
import net.minecraft.block.TransparentBlock;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.*;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.entity.animation.Keyframe;
import net.minecraft.client.render.entity.animation.Transformation;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@SuppressWarnings("unused")
public class StackFolliesClient implements ClientModInitializer {
    public static final String MODEL_GROUP_PROCESSOR_KEY = "sf$computeGroups";
    public static final String MODEL_ELEM_NAME_KEY = "name";

    @Override
    public void onInitializeClient() {
    }

    public static void animate(BakedModel model, Animation animation, long runningTime, float scale, Vector3f interpolation) {
        float f = getRunningSeconds(animation, runningTime);
        if (!(model instanceof GroupedModelPart group)) return;

        for (Map.Entry<String, List<Transformation>> entry : animation.boneAnimations().entrySet()) {
            Optional<ModelPart> optional = group.sf_getChild(entry.getKey());
            if (optional.isEmpty()) continue;
            ModelPart part = optional.get();
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
                transformation.target().apply(part, interpolation);
            });
        }
    }

    public static boolean render(
            ItemStack stack, World world, LivingEntity entity, int seed, int light, int overlay, ItemRenderer renderer,
            ModelTransformationMode transformationMode, MatrixStack matrices,
            VertexConsumerProvider vertices
    ) {
        return render(stack, world, entity,seed, light, overlay, renderer, transformationMode, matrices, vertices, true);
    }

    public static boolean render(
            ItemStack stack, World world, LivingEntity entity, int seed, int light, int overlay, ItemRenderer renderer,
            ModelTransformationMode transformationMode, MatrixStack matrices,
            VertexConsumerProvider vertices, boolean resetTransform
    ) {
        BakedModel model = renderer.getModel(stack, world, entity, seed);
        if (!(model instanceof GroupedModelPart part)) {
            renderer.renderItem(entity, stack, transformationMode, false, matrices, vertices, world, light, overlay, seed);
            return false;
        }
        boolean direct = (!(stack.getItem() instanceof BlockItem block) || !(block.getBlock() instanceof TransparentBlock || block.getBlock() instanceof StainedGlassPaneBlock));
        RenderLayer renderLayer = RenderLayers.getItemLayer(stack, direct);
        VertexConsumer vertex = direct ?
                ItemRenderer.getDirectItemGlintConsumer(vertices, renderLayer, true, stack.hasGlint()) :
                ItemRenderer.getItemGlintConsumer(vertices, renderLayer, true, stack.hasGlint());
        part.sf_render(stack, matrices, vertex, light, overlay, renderer);
        if (resetTransform) part.traverse().forEach(ModelPart::resetTransform);
        return true;
    }

	private static float getRunningSeconds(Animation animation, long runningTime) {
		float f = (float)runningTime / 1000.0F;
		return animation.looping() ? f % animation.lengthInSeconds() : f;
	}
}
