package mc.recraftors.stack_follies.client;

import mc.recraftors.stack_follies.accessors.GroupedModelAccessor;
import mc.recraftors.stack_follies.accessors.NamedElementAccessor;
import mc.recraftors.stack_follies.util.Pair;
import net.minecraft.block.BlockState;
import net.minecraft.client.model.*;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.model.BakedModel;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.render.model.json.ModelElement;
import net.minecraft.client.render.model.json.ModelOverrideList;
import net.minecraft.client.render.model.json.ModelRotation;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class GroupedBakedModelBuilder implements BakedModel {

    private final BakedModel sourceModel;
    // Store accessor as a field to avoid repetitive cast
    private final GroupedModelAccessor sourceAccessor;
    private final Map<Integer, ModelPartData> cuboidMap;
    private final Map<String, ModelPartData> namedMap;
    private final ModelData modelData = new ModelData();
    private final TexturedModelData texturedModelData;

    private ModelPart model;

    public GroupedBakedModelBuilder(BakedModel model, GroupedModelAccessor accessor) {
        if (model != accessor) throw new IllegalArgumentException("Different models provided");
        Pair<Map<Integer, ModelPartData>, Map<String, ModelPartData>> t = bakeModel();
        Pair<Integer, Integer> pair = accessor.sf_getTextureSize();
        this.sourceModel = model;
        this.sourceAccessor = accessor;
        this.cuboidMap = Collections.unmodifiableMap(t.getFirst());
        this.namedMap = Collections.unmodifiableMap(t.getSecond());
        this.texturedModelData = TexturedModelData.of(this.modelData, pair.getFirst(), pair.getSecond());
        StackFolliesClient.registerGroupedModel(this);
    }

    GroupedBakedModel build() {
        if (this.model != null) {
            return StackFolliesClient.getModel(this);
        }
        this.model = this.texturedModelData.createModel();
        Map<String, ModelPart> map = this.namedMap.keySet().stream().map(modelPartData -> new AbstractMap.SimpleEntry<>(modelPartData, model.getChild(modelPartData))).collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> tMap = this.textureUsages();
        Identifier id = tMap.entrySet().stream().min((e1, e2) -> e2.getValue().compareTo(e1.getValue())).map(e -> this.sourceAccessor.sf_getTextureMap().get(e.getKey())).map(i -> i.getPath().matches(".*\\.[^/]+") ? i : Identifier.of(i.getNamespace(), i.getPath()+".png")).orElseThrow();
        RenderLayer layer = RenderLayer.getEntityTranslucent(id);
        return new GroupedBakedModel(map, model, layer);
    }

    private Pair<Map<Integer, ModelPartData>, Map<String, ModelPartData>> bakeModel() {
        Map<Integer, ModelPartData> map1 = new HashMap<>();
        Map<String, ModelPartData> map2 = new HashMap<>();
        for (ModelGroupElement groupElement : this.sourceAccessor.sf_getGroups()) {
            Pair<String, ModelPartData> pair = devolve(this.modelData.getRoot(), groupElement, map2::put, map1::put);
            map2.put(groupElement.getName(), pair.getSecond());
        }
        return Pair.of(map1, map2);
    }

    private Pair<String, ModelPartData> devolve(
            ModelPartData parent, ModelGroupElement groupElement, BiConsumer<String, ModelPartData> c1,
            BiConsumer<Integer, ModelPartData> c2
    ) {
        if (groupElement.type == ModelGroupElement.GroupType.GROUP) {
            int x = groupElement.getOrigin()[0];
            int y = groupElement.getOrigin()[1];
            int z = groupElement.getOrigin()[2];
            ModelPartBuilder builder = ModelPartBuilder.create().cuboid(x, y, z, 0, 0, 0);
            ModelPartData data = parent.addChild(groupElement.getName(), builder, ModelTransform.NONE);
            for (ModelGroupElement e : groupElement.getChildren()) {
                Pair<String, ModelPartData> d = devolve(data, e, c1, c2);
                c1.accept(d.getFirst(), d.getSecond());
                c2.accept(e.element, d.getSecond());
            }
            return Pair.of(groupElement.getName(), data);
        }
        int index = groupElement.element;
        ModelElement element = this.sourceAccessor.sf_getElements().get(index);
        NamedElementAccessor elementAccessor = (NamedElementAccessor) element;
        ModelRotation rotation = elementAccessor.sf_getRotation();
        float fX = element.from.x();
        float fY = element.from.y();
        float fZ = element.from.z();
        float p = rotation.axis() == Direction.Axis.X ? rotation.angle() : 0;
        float y = rotation.axis() == Direction.Axis.Y ? rotation.angle() : 0;
        float r = rotation.axis() == Direction.Axis.Z ? rotation.angle() : 0;
        ModelPartBuilder builder = ModelPartBuilder.create()
                .uv((int) elementAccessor.sf_getUvX(), (int) elementAccessor.sf_getUvY())
                .cuboid(fX, fY, fZ, element.to.x() - fX, element.to.y() - fY, element.to.z() - fZ);
        return Pair.of(elementAccessor.sf_getElemName(), parent.addChild(elementAccessor.sf_getElemName(), builder, ModelTransform.of(rotation.origin().x, rotation.origin().y, rotation.origin().z, p, y, r)));
    }

    private Map<String, Integer> textureUsages() {
        Map<String, Integer> map = new HashMap<>();
        this.sourceAccessor.sf_getElements().forEach(e -> {
            e.faces.values().forEach(f -> {
                int i = map.getOrDefault(f.textureId, 0);
                i++;
                map.put(f.textureId, i);
            });
        });
        return map;
    }

    public BakedModel getSourceModel() {
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
