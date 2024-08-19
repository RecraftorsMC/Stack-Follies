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
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class GroupedBakedModelBuilder implements BakedModel {

    private final BakedModel sourceModel;
    // Store accessor as a field to avoid repetitive cast
    private final GroupedModelAccessor sourceAccessor;
    private final Map<String, ModelPartData> namedMap;
    private final ModelData modelData = new ModelData();
    private final TexturedModelData texturedModelData;

    private ModelPart model;

    public GroupedBakedModelBuilder(BakedModel model, GroupedModelAccessor accessor) {
        if (model != accessor) throw new IllegalArgumentException("Different models provided");
        Pair<Integer, Integer> pair = accessor.sf_getTextureSize();
        this.sourceModel = model;
        this.sourceAccessor = accessor;
        Map<String, ModelPartData> map = bakeModel();
        this.namedMap = Collections.unmodifiableMap(map);
        this.texturedModelData = TexturedModelData.of(this.modelData, pair.getFirst(), pair.getSecond());
        StackFolliesClient.registerGroupedModel(this);
    }

    GroupedBakedModel build() {
        if (this.model != null) {
            return StackFolliesClient.getModel(this);
        }
        this.model = this.texturedModelData.createModel();
        Map<String, ModelPart> map = this.namedMap.keySet().stream()
                .map(name -> new AbstractMap.SimpleEntry<>(name, model.getChild(name)))
                .collect(Collectors.toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));
        Map<String, Integer> tMap = this.textureUsages();
        Identifier id = tMap.entrySet().stream().min((e1, e2) -> e2.getValue().compareTo(e1.getValue())).map(e -> this.sourceAccessor.sf_getTextureMap().get(e.getKey())).map(i -> i.getPath().matches(".*\\.[^/]+") ? i : Identifier.of(i.getNamespace(), i.getPath()+".png")).orElseThrow();
        RenderLayer layer = RenderLayer.getEntityTranslucent(id);
        return new GroupedBakedModel(map, model, layer);
    }

    private Map<String, ModelPartData> bakeModel() {
        Map<String, ModelPartData> map = new HashMap<>();
        Set<ModelGroupElement> set = new HashSet<>(this.sourceAccessor.sf_getGroups());
        for (ModelGroupElement groupElement : this.sourceAccessor.sf_getGroups()) {
            if (groupElement.getType() == ModelGroupElement.GroupType.ELEMENT) continue;
            Pair<String, ModelPartData> pair = devolve(this.modelData.getRoot(), groupElement, map::put, set::remove, map::containsKey);
            if (pair.hasFirst()) map.put(groupElement.getName(), pair.getSecond());
        }
        for (ModelGroupElement groupElement : set) {
            Pair<String, ModelPartData> pair = devolve(this.modelData.getRoot(), groupElement, map::put, set::remove, map::containsKey);
            if (pair.hasFirst()) map.put(groupElement.getName(), pair.getSecond());
        }
        return map;
    }

    private Pair<String, ModelPartData> devolve(
            ModelPartData parent, ModelGroupElement groupElement, BiConsumer<String, ModelPartData> c1,
            Consumer<ModelGroupElement> c2, Predicate<String> predicate
    ) {
        if (predicate.test(groupElement.getName())) return Pair.ofNull();
        if (groupElement.getType() == ModelGroupElement.GroupType.GROUP) {
            c2.accept(groupElement);
            int x = groupElement.getOrigin()[0];
            int y = groupElement.getOrigin()[1];
            int z = groupElement.getOrigin()[2];
            ModelPartBuilder builder = ModelPartBuilder.create().cuboid(x, y, z, 0, 0, 0);
            ModelPartData data = parent.addChild(groupElement.getName(), builder, ModelTransform.NONE);
            for (ModelGroupElement e : groupElement.getChildren()) {
                Pair<String, ModelPartData> d = devolve(data, e, c1, c2, predicate);
                if (d.hasFirst()) c1.accept(d.getFirst(), d.getSecond());
            }
            return Pair.of(groupElement.getName(), data);
        }
        int index = groupElement.getElement();
        ModelElement element = this.sourceAccessor.sf_getElements().get(index);
        NamedElementAccessor elementAccessor = (NamedElementAccessor) element;
        if (predicate.test(elementAccessor.sf_getElemName())) return Pair.ofNull();
        c2.accept(groupElement);
        ModelRotation rotation = elementAccessor.sf_getRotation();
        float fX = element.from.x();
        float fY = element.from.y();
        float fZ = element.from.z();
        float p;
        float y;
        float r;
        float rX;
        float rY;
        float rZ;
        if (rotation == null) {
            p = 0;
            y = 0;
            r = 0;
            rX = 0;
            rY = 0;
            rZ = 0;
        } else {
            p = rotation.axis() == Direction.Axis.X ? rotation.angle() : 0;
            y = rotation.axis() == Direction.Axis.Y ? rotation.angle() : 0;
            r = rotation.axis() == Direction.Axis.Z ? rotation.angle() : 0;
            rX = rotation.origin().x;
            rY = rotation.origin().y;
            rZ = rotation.origin().z;
        }
        ModelPartBuilder builder = ModelPartBuilder.create()
                .uv((int) elementAccessor.sf_getUvX(), (int) elementAccessor.sf_getUvY())
                .cuboid(fX, fY, fZ, element.to.x() - fX, element.to.y() - fY, element.to.z() - fZ);
        ModelPartData child = parent.addChild(elementAccessor.sf_getElemName(), builder, ModelTransform.of(rX, rY, rZ, p, y, r));
        c1.accept(elementAccessor.sf_getElemName(), child);
        return Pair.of(elementAccessor.sf_getElemName(), child);
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
