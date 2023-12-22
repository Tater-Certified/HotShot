package com.github.tatercertified.hotshot.mixin;

import com.github.tatercertified.hotshot.interfaces.EntityAttackerInterface;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PersistentProjectileEntity.class)
public abstract class PersistentProjectileMixin extends ProjectileEntity {
    private boolean hasFlame;

    public PersistentProjectileMixin(EntityType<? extends ProjectileEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onEntityHit", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
    private void injectFlameKillCredit(EntityHitResult entityHitResult, CallbackInfo ci) {
        if (this.hasFlame && entityHitResult.getEntity() instanceof LivingEntity && this.getOwner() instanceof LivingEntity owner) {
            ((EntityAttackerInterface)entityHitResult.getEntity()).setAttackerFireAspect(owner);
        }
    }

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void injectHasFlameCheck(Entity entity, CallbackInfo ci) {
        if (entity instanceof LivingEntity livingEntity) {
            this.hasFlame = EnchantmentHelper.getLevel(Enchantments.FLAME, livingEntity.getMainHandStack()) != 0;
        }
    }
}
