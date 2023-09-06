package com.example.throwablegunpowder;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import static com.example.throwablegunpowder.throwablegunpowder.*;

public class ThrownGunPowder extends ThrowableItemProjectile {
    public ThrownGunPowder(EntityType<? extends ThrownGunPowder> p_37391_, Level p_37392_) {
        super(p_37391_, p_37392_);
    }

    public ThrownGunPowder(Level p_37399_, LivingEntity p_37400_) {
        super(THROWN_GUNPOWDER.get(), p_37400_, p_37399_);
    }

    public ThrownGunPowder(Level p_37394_, double p_37395_, double p_37396_, double p_37397_) {
        super(THROWN_GUNPOWDER.get(), p_37395_, p_37396_, p_37397_, p_37394_);
    }

    protected Item getDefaultItem() {
        return NEW_GUNPOWDER.get();
    }

    protected void onHitEntity(EntityHitResult p_37404_) {
        super.onHitEntity(p_37404_);
        Entity entity = p_37404_.getEntity();
        entity.hurt(this.damageSources().thrown(this, this.getOwner()), 1);
        this.boooom();
    }

    protected void onHit(HitResult p_37406_) {
        super.onHit(p_37406_);
        if (!this.level().isClientSide) {
            this.level().broadcastEntityEvent(this, (byte)3);
            this.discard();
            this.boooom();
        }
    }

    protected void boooom() {
//        this.level.explode(this, this.getX(), this.getY(), this.getZ(), 2.0F, false, Level.ExplosionInteraction.TNT);
        this.level().explode(this, null, new GunPowderExplosionDamageCalculator(), this.getX(), this.getY(), this.getZ(), EXPLOSION_SIZE.get().floatValue(), false, Level.ExplosionInteraction.TNT);
    }
}