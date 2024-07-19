package mc.recraftors.stack_follies.client;

import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelPartBuilder;
import net.minecraft.client.render.entity.animation.Animation;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.BasicBakedModel;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.AnimationState;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class GroupedBakedModel implements BakedModel {
    public static final Vector3f INTERPOLATION_DEFAULT = new Vector3f();

    private final BasicBakedModel sourceModel;
    // Store accessor as a field to avoid repetitive cast
    private final GroupedModelAccessor sourceAccessor;
    private final Map<Integer, ModelPartData> cuboidMap;
    private final ModelData modelData = new ModelData();
    GroupedBakedModel(BasicBakedModel model, GroupedModelAccessor accessor) {
        if (model != accessor) throw new IllegalArgumentException("Different models provided");
        this.sourceModel = model;
        this.sourceAccessor = accessor;
        this.cuboidMap = this.bakeCuboid();
        //TODO link names and model parts + setup unified model builder
    }

    private Map<Integer, ModelPartData> bakeCuboid() {
        return this.sourceAccessor.sf_getGroups().stream()
                .mapMulti(this::groupStreamMultiMapper)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    private void groupStreamMultiMapper(ModelGroupElement group, Consumer<Map.Entry<Integer, ModelPartData>> consumer) {
        if (group.type == ModelGroupElement.GroupType.GROUP) {
            for (ModelGroupElement e : group.getChildren()) {
                groupStreamMultiMapper(e, consumer);
            }
            return;
        }
        int index = group.element;
        ModelElement element = this.sourceAccessor.sf_getElements().get(index);
        NamedElementAccessor elementAccessor = (NamedElementAccessor) element;
        ModelPartBuilder builder = ModelPartBuilder.create().uv((int) elementAccessor.sf_getUvX(), (int) elementAccessor.sf_getUvY()).cuboid(); //TODO calculate offsets based on element coords
        ModelPartData part = this.modelData.getRoot().addChild(elementAccessor.sf_getElemName(), builder, null); //TODO calculate pivot based on element rotation
        consumer.accept(Map.entry(index, part));
    }

    public void updateAnimation(AnimationState state, Animation animation, float progress) {
        this.updateAnimation(state, animation, progress, 1f);
    }

    public void updateAnimation(AnimationState state, Animation animation, float progress, float speed) {
        state.update(progress, speed);
        state.run(s -> {
            StackFolliesClient.animate(this, animation, s.getTimeRunning(), 1f, INTERPOLATION_DEFAULT);
        });
    }

    public BasicBakedModel getSourceModel() {
        return sourceModel;
    }

    public GroupedModelAccessor getSourceAccessor() {
        return sourceAccessor;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction face, Random random) {
        return this.sourceModel.getQuads(state, face, random);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return this.sourceModel.useAmbientOcclusion();
    }

    @Override
    public boolean hasDepth() {
        return this.sourceModel.hasDepth();
    }

    @Override
    public boolean isSideLit() {
        return this.sourceModel.isSideLit();
    }

    @Override
    public boolean isBuiltin() {
        // should always be false
        return this.sourceModel.isBuiltin();
    }

    @Override
    public Sprite getParticleSprite() {
        return this.sourceModel.getParticleSprite();
    }

    @Override
    public ModelTransformation getTransformation() {
        return this.sourceModel.getTransformation();
    }

    @Override
    public ModelOverrideList getOverrides() {
        return this.sourceModel.getOverrides();
    }
}
