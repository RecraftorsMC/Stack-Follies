package mc.recraftors.stack_follies.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mc.recraftors.stack_follies.StackFollies;
import net.minecraft.entity.ItemEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {
    @Shadow public abstract ItemStack getStack();

    @ModifyExpressionValue(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/World;isClient:Z", ordinal = 3))
    private boolean sf_despawnImmune(boolean original) {
        if (this.getStack().isIn(StackFollies.DESPAWN_IMMUNE)) return true;
        return original;
    }

    @ModifyExpressionValue(method = "damage", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z"))
    private boolean sf_explosion_immune(boolean original) {
        return original || this.getStack().isIn(StackFollies.EXPLOSION_IMMUNE);
    }
}
