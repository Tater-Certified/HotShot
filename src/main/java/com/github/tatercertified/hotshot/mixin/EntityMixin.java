package com.github.tatercertified.hotshot.mixin;

import com.github.tatercertified.hotshot.interfaces.EntityAttackerInterface;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class EntityMixin extends Entity implements EntityAttackerInterface {

    @Shadow @Nullable public abstract LivingEntity getPrimeAdversary();

    @Shadow @Nullable public abstract LivingEntity getAttacker();

    @Shadow protected abstract void dropLoot(DamageSource damageSource, boolean causedByPlayer);

    private boolean isBurningFromFireAspect;
    private LivingEntity indirectAttacker;

    public EntityMixin(EntityType<?> type, World world) {
        super(type, world);
    }

    @Override
    public void setAttackerFireAspect(LivingEntity attacker) {
        this.isBurningFromFireAspect = true;
        this.indirectAttacker = attacker;
    }

    private void checkForFire() {
        if (this.isBurningFromFireAspect && this.getFireTicks() <= 0) {
            this.isBurningFromFireAspect = false;
            this.indirectAttacker = null;
        }
    }

    @Inject(method = "baseTick", at = @At(value = "TAIL"))
    private void injectFireCheck(CallbackInfo ci) {
        checkForFire();
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;getPrimeAdversary()Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity redirectGetPrimeAdversary(LivingEntity instance, @Local(ordinal = 0) DamageSource damageSource) {
        if (this.wasKilledByFireAspect(damageSource)) {
            this.indirectAttacker.onKilledOther((ServerWorld) instance.getWorld(), instance);

            return this.indirectAttacker;
        }
        return this.getPrimeAdversary();
    }

    @Redirect(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/damage/DamageSource;getAttacker()Lnet/minecraft/entity/Entity;"))
    private Entity redirectGetAttacker(DamageSource instance) {
        if (this.wasKilledByFireAspect(instance)) {
            return this.indirectAttacker;
        }
        return this.getAttacker();
    }

    @Redirect(method = "drop", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;dropLoot(Lnet/minecraft/entity/damage/DamageSource;Z)V"))
    private void redirectDropLoot(LivingEntity instance, DamageSource damageSource, boolean causedByPlayer) {
        if (this.wasKilledByFireAspect(damageSource)) {
            this.dropLoot(new DamageSource(damageSource.getTypeRegistryEntry(), damageSource.getSource(), this.indirectAttacker), causedByPlayer);
        } else {
            this.dropLoot(damageSource, causedByPlayer);
        }
    }

    private boolean wasKilledByFireAspect(DamageSource source) {
        return this.isBurningFromFireAspect && source.getType() == this.getDamageSources().onFire().getType();
    }

}
