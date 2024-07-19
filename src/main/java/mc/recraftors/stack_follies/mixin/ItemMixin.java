package mc.recraftors.stack_follies.mixin;

import mc.recraftors.stack_follies.StackFollies;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemConvertible {
    @Shadow @Final private RegistryEntry.Reference<Item> registryEntry;

    @Inject(method = "isFireproof", at = @At("HEAD"), cancellable = true)
    private void sf_fireTagImmune(CallbackInfoReturnable<Boolean> cir) {
        if (this.registryEntry.isIn(StackFollies.FIRE_IMMUNE)) cir.setReturnValue(true);
    }

    @Inject(method = "damage", at = @At("RETURN"), cancellable = true)
    private void sg_damageTypeImmune(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        if (!cir.getReturnValueZ()) return;
        if (this.registryEntry.isIn(StackFollies.immuneTagFor(source.getType()))) cir.setReturnValue(false);
    }
}
