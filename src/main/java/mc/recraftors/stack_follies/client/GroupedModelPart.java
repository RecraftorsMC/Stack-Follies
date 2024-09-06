package mc.recraftors.stack_follies.client;

import mc.recraftors.stack_follies.accessors.ModelPartDepthAccessor;
import mc.recraftors.stack_follies.accessors.NamedModelPart;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public sealed abstract class GroupedModelPart extends ModelPart
        implements BakedModel, ModelPartDepthAccessor, NamedModelPart {
    protected final String name;
    protected final boolean usesOcclusion;
    protected final boolean hasDepth;
    protected final boolean isSideLit;
	protected final Sprite sprite;
	protected final ModelTransformation transformation;
	protected final ModelOverrideList overrideList;

    public GroupedModelPart(Map<String, GroupedModelPart> children, String name, boolean usesOcclusion, boolean hasDepth,
                            boolean isSideLit, Sprite sprite, ModelTransformation transformation,
                            ModelOverrideList overrideList) {
        super(List.of(), Map.copyOf(children));
        this.name = name;
        this.usesOcclusion = usesOcclusion;
        this.hasDepth = hasDepth;
        this.isSideLit = isSideLit;
        this.sprite = sprite;
        this.transformation = transformation;
        this.overrideList = overrideList;
    }

    @Override
    public final String sf_getName() {
        return this.name;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.usesOcclusion;
    }

    @Override
    public boolean hasDepth() {
        return this.hasDepth;
    }

    @Override
    public final boolean isBuiltin() {
        return false;
    }

    @Override
    public boolean isSideLit() {
        return this.isSideLit;
    }

    @Override
    public Sprite getParticleSprite() {
        return this.sprite;
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.transformation;
    }

    @Override
    public ModelOverrideList getOverrides() {
        return this.overrideList;
    }

    public abstract void sf_render(ItemStack stack, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, ItemRenderer renderer);

    public static GroupedModelPart group(Map<String, GroupedModelPart> children, String name, boolean usesOcclusion,
                                         boolean hasDepth, boolean isSideLit, Sprite sprite,
                                         ModelTransformation transformation, ModelOverrideList overrideList) {
        return new GroupModelPart(children, name, usesOcclusion, hasDepth, isSideLit, sprite, transformation, overrideList);
    }

    public static GroupedModelPart element(String name, BakedModel wrapped) {
        return new ElementModelPart(name, wrapped);
    }

    static final class GroupModelPart extends GroupedModelPart {
        public GroupModelPart(Map<String, GroupedModelPart> children, String name, boolean usesOcclusion, boolean hasDepth,
                              boolean isSideLit, Sprite sprite, ModelTransformation transformation,
                            ModelOverrideList overrideList) {
            super(children, name, usesOcclusion, hasDepth, isSideLit, sprite, transformation, overrideList);
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            List<BakedQuad> list = new ArrayList<>();
            this.sf_getChildren().values().forEach(m -> list.addAll(((GroupedModelPart)m).getQuads(state, face, random)));
            return List.copyOf(list);
        }

        @Override
        public void sf_render(ItemStack stack, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, ItemRenderer renderer) {
            if (!this.visible) return;
            Map<String, ModelPart> map = this.sf_getChildren();
            if (map.isEmpty()) return;

            matrices.push();

            super.rotate(matrices); // why "rotate", Yarn, "transform" would have made so much more sense

            for (ModelPart part : map.values()) {
                ((GroupedModelPart) part).sf_render(stack, matrices, vertices, light, overlay, renderer);
            }

            matrices.pop();
        }
    }

    static final class ElementModelPart extends GroupedModelPart {
        private final BakedModel wrapped;

        public ElementModelPart(String name, BakedModel wrapped) {
            super(Map.of(), name, wrapped.useAmbientOcclusion(), wrapped.hasDepth(), wrapped.isSideLit(),
                    wrapped.getParticleSprite(), wrapped.getTransformation(), wrapped.getOverrides());
            this.wrapped = wrapped;
        }

        @Override
        public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
            return this.wrapped.getQuads(state, face, random);
        }

        @Override
        public void sf_render(ItemStack stack, MatrixStack matrices, VertexConsumer vertices, int light, int overlay, ItemRenderer renderer) {
            if (!this.visible) return;

            matrices.push();

            super.rotate(matrices); // why "rotate", Yarn, "transform" would have made so much more sense

            renderer.renderBakedItemModel(this, stack, light, overlay, matrices, vertices);

            matrices.pop();
        }
    }
}
