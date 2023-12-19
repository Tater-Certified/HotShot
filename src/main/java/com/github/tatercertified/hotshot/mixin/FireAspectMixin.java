package com.github.tatercertified.hotshot.mixin;

import com.github.tatercertified.hotshot.interfaces.EntityAttackerInterface;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentTarget;
import net.minecraft.enchantment.FireAspectEnchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FireAspectEnchantment.class)
public class FireAspectMixin extends Enchantment  {
    protected FireAspectMixin(Rarity weight, EnchantmentTarget target, EquipmentSlot[] slotTypes) {
        super(weight, target, slotTypes);
    }

    @Override
    public void onTargetDamaged(LivingEntity user, Entity target, int level) {
        super.onTargetDamaged(user, target, level);
        if (target instanceof LivingEntity livingEntity) {
            ((EntityAttackerInterface)livingEntity).setAttackerFireAspect(user);
        }
    }
}
