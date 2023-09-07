package com.example.throwablegunpowder;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.List;

import static com.example.throwablegunpowder.throwablegunpowder.*;
import static net.minecraft.world.level.Explosion.getSeenPercent;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class EventHandler {

    @SubscribeEvent
    public void onExplosion(ExplosionEvent.Detonate event) {
        LOGGER.info(event.getExplosion().getClass().toString());
        // Check if it's a gunpowder explosion or a vanilla explosion
        if (event.getExplosion() instanceof CustomGunpowderExplosion customExplosion) {
            LOGGER.info("Custom explosion detected");
            Vec3 explode_pos = customExplosion.getPosition();
            float f2;
            Object test = ObfuscationReflectionHelper.getPrivateValue(Explosion.class, customExplosion, "radius");
            if (test instanceof Float) {
                f2 = (2.0F * (float) test);
                LOGGER.info("f2 -> {}", f2);
            } else {
                f2 = (float) (2.0F * EXPLOSION_SIZE.get());
                LOGGER.info("test class -> {}", test.getClass().toString());
            }
            List<Entity> list = event.getAffectedEntities();
            Vec3 vec3 = customExplosion.getPosition();

            for (Entity entity : list) {
                if (!entity.ignoreExplosion()) {
                    double d12 = Math.sqrt(entity.distanceToSqr(vec3)) / (double) f2;
                    if (d12 <= 1.0D) {
                        double d5 = entity.getX() - explode_pos.x;
                        double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - explode_pos.y;
                        double d9 = entity.getZ() - explode_pos.z;
                        double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                        if (d13 != 0.0D) {
                            d5 /= d13;
                            d7 /= d13;
                            d9 /= d13;
                            double d14 = (double) getSeenPercent(vec3, entity);
                            double d10 = (1.0D - d12) * d14;
//                            entity.hurt(customExplosion.getDamageSource(), (float)((int)((d10 * d10 + d10) / 2.0D * 7.0D * (double)f2 + 1.0D)));
                            double d11;
                            if (entity instanceof LivingEntity livingentity) {
                                d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d10);
                            } else {
                                d11 = d10;
                            }

                            d5 *= d11;
                            d7 *= d11;
                            d9 *= d11;
                            Vec3 vec31 = new Vec3(d5, d7, d9);
                            entity.setDeltaMovement(entity.getDeltaMovement().add(vec31));
                            if (entity instanceof Player player) {
                                if (!player.isSpectator() && (!player.isCreative() || !player.getAbilities().flying)) {
                                    customExplosion.getHitPlayers().put(player, vec31);
                                }
                            }
                        }
                    }
                }
            }
            // stop explosion
            event.setCanceled(true);
        }
    }
}
