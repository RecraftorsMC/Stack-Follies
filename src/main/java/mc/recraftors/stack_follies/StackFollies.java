package mc.recraftors.stack_follies;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.item.Item;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import java.util.LinkedHashMap;
import java.util.Map;

public class StackFollies implements ModInitializer {
    public static final String MOD_ID = "sf";
    public static final String CACTUS_IMMUNE_ID = "cactus_immune";
    public static final String DESPAWN_IMMUNE_ID = "despawn_immune";
    public static final String EXPLOSION_IMMUNE_ID = "explosion_immune";
    public static final String FIRE_IMMUNE_ID = "fire_immune";

    public static final TagKey<Item> CACTUS_IMMUNE;
    public static final TagKey<Item> DESPAWN_IMMUNE;
    public static final TagKey<Item> EXPLOSION_IMMUNE;
    public static final TagKey<Item> FIRE_IMMUNE;

    private static final Map<String, TagKey<Item>> IMMUNE_TAG_MAP;

    static {
        CACTUS_IMMUNE = TagKey.of(RegistryKeys.ITEM, commonId(CACTUS_IMMUNE_ID));
        DESPAWN_IMMUNE = TagKey.of(RegistryKeys.ITEM, commonId(DESPAWN_IMMUNE_ID));
        EXPLOSION_IMMUNE = TagKey.of(RegistryKeys.ITEM, commonId(EXPLOSION_IMMUNE_ID));
        FIRE_IMMUNE = TagKey.of(RegistryKeys.ITEM, commonId(FIRE_IMMUNE_ID));

        IMMUNE_TAG_MAP = new LinkedHashMap<>();
    }

    @Override
    public void onInitialize() {
    }

    public static Identifier commonId(String s) {
        return new Identifier("c", s);
    }

    public static TagKey<Item> immuneTagFor(DamageType type) {
        return IMMUNE_TAG_MAP.computeIfAbsent(type.msgId(), s -> TagKey.of(RegistryKeys.ITEM, commonId("immune/"+type.msgId())));
    }
}
