package com.example.throwablegunpowder;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

public class CustomGunpowderExplosion extends Explosion {
    public CustomGunpowderExplosion(Level p_46051_, @Nullable Entity p_46052_, @Nullable DamageSource p_46053_, @Nullable ExplosionDamageCalculator p_46054_, double p_46055_, double p_46056_, double p_46057_, float p_46058_, boolean p_46059_, BlockInteraction p_46060_) {
        super(p_46051_, p_46052_, p_46053_, p_46054_, p_46055_, p_46056_, p_46057_, p_46058_, p_46059_, p_46060_);
    }

    public static Explosion levelStartExplode(@javax.annotation.Nullable Entity p_256233_, @javax.annotation.Nullable DamageSource p_255861_, @javax.annotation.Nullable ExplosionDamageCalculator p_255867_, double p_256447_, double p_255732_, double p_255717_, float p_256013_, boolean p_256228_, Level.ExplosionInteraction p_255784_, Level level) {

        Explosion.BlockInteraction explosion$blockinteraction = level.getGameRules().getBoolean(GameRules.RULE_BLOCK_EXPLOSION_DROP_DECAY) ? Explosion.BlockInteraction.DESTROY_WITH_DECAY : Explosion.BlockInteraction.DESTROY;
        CustomGunpowderExplosion explosion = new CustomGunpowderExplosion(level, p_256233_, p_255861_, p_255867_, p_256447_, p_255732_, p_255717_, p_256013_, p_256228_, explosion$blockinteraction);
        if (net.minecraftforge.event.ForgeEventFactory.onExplosionStart(level, explosion)) return explosion;
        explosion.explode();
        explosion.finalizeExplosion(true);
        return explosion;
    }

}
