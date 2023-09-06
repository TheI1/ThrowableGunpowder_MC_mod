package com.example.throwablegunpowder;

import com.mojang.logging.LogUtils;
import net.minecraft.Util;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.AbstractProjectileDispenseBehavior;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.ProtectionEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.List;

import static net.minecraft.world.level.Explosion.getSeenPercent;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(throwablegunpowder.MODID)
public class throwablegunpowder
{
    // Define mod id in a common place for everything to reference
    public static final String MODID = "throwablegunpowder";
    // Directly reference a slf4j logger
    private static final Logger LOGGER = LogUtils.getLogger();
    // Create a Deferred Register to hold Blocks which will all be registered under the "throwablegunpowder" namespace

    // Configs
    public static final ForgeConfigSpec.Builder CONFIG_BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec CONFIG_SPEC;
    public static final ForgeConfigSpec.ConfigValue<Double> EXPLOSION_SIZE;
    public static final ForgeConfigSpec.ConfigValue<Double> THROW_STRENGTH;
    public static final ForgeConfigSpec.ConfigValue<Integer> COOLDOWN_TIME;

    static {
        CONFIG_BUILDER.push("Throwable Gunpowder Configs");

        EXPLOSION_SIZE = CONFIG_BUILDER.comment("How large the Gunpowder's explosion is upon hitting a target.").define("Explosion Size", 2.0);
        THROW_STRENGTH = CONFIG_BUILDER.comment("How far the Gunpowder travels when thrown by a player.").define("Player Throw Strength", 1.0);
        COOLDOWN_TIME = CONFIG_BUILDER.comment("How long the cooldown is between the player's throws.").define("Player Cooldown Time", 40);

        CONFIG_BUILDER.pop();
        CONFIG_SPEC = CONFIG_BUILDER.build();
    }



    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, MODID);
    // Create a Deferred Register to hold Items which will all be registered under the "throwablegunpowder" namespace
    public static final DeferredRegister<Item> VANILLA_ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, "minecraft");
    public static final RegistryObject<Item> NEW_GUNPOWDER = VANILLA_ITEMS.register("gunpowder", () -> new ThrowableGunPowderItem(new Item.Properties()));
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MODID);
    public static final RegistryObject<EntityType<ThrownGunPowder>> THROWN_GUNPOWDER = ENTITY_TYPES.register("thrown_gunpowder", () -> EntityType.Builder.of((EntityType.EntityFactory<ThrownGunPowder>) ThrownGunPowder::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(10).build("thrown_gunpowder"));

    public throwablegunpowder()
    {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        // Register the commonSetup method for modloading
        modEventBus.addListener(this::commonSetup);

        // Register the Deferred Register to the mod event bus so blocks get registered
        BLOCKS.register(modEventBus);
        // Register the Deferred Register to the mod event bus so items get registered
        VANILLA_ITEMS.register(modEventBus);
        ENTITY_TYPES.register(modEventBus);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, CONFIG_SPEC , MODID + "-common.toml");
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        DispenserBlock.registerBehavior(NEW_GUNPOWDER.get(), new AbstractProjectileDispenseBehavior() {
            protected Projectile getProjectile(Level world, Position pos, ItemStack sourceItem) {
                return Util.make(new ThrownGunPowder(world, pos.x(), pos.y(), pos.z()), (projectile) -> {
                    projectile.setItem(sourceItem);
                });
            }
        });
    }

    @SubscribeEvent
    public void on(ServerStartingEvent event)
    {
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event)
    {
    }

    @SubscribeEvent
    public static void onExplosion(ExplosionEvent.Detonate event) {
        // Check if it's a gunpowder explosion or a vanilla explosion
        if (event.getExplosion() instanceof CustomGunpowderExplosion) {
            CustomGunpowderExplosion customExplosion = (CustomGunpowderExplosion) event.getExplosion();
            Vec3 explode_pos = customExplosion.getPosition();
            float f2 = (float) (2.0F * EXPLOSION_SIZE.get());
            List<Entity> list = event.getAffectedEntities();
            Vec3 vec3 = customExplosion.getPosition();

            for(int k2 = 0; k2 < list.size(); ++k2) {
                Entity entity = list.get(k2);
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
                            entity.hurt(customExplosion.getDamageSource(), 1F);
                            double d11;
                            if (entity instanceof LivingEntity) {
                                LivingEntity livingentity = (LivingEntity) entity;
                                d11 = ProtectionEnchantment.getExplosionKnockbackAfterDampener(livingentity, d10);
                            } else {
                                d11 = d10;
                            }

                            d5 *= d11;
                            d7 *= d11;
                            d9 *= d11;
                            Vec3 vec31 = new Vec3(d5, d7, d9);
                            entity.setDeltaMovement(entity.getDeltaMovement().add(vec31));
                            if (entity instanceof Player) {
                                Player player = (Player) entity;
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

    // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
    @Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents
    {
        @SubscribeEvent
        public static void doSetup(FMLClientSetupEvent event) {
            EntityRenderers.register(THROWN_GUNPOWDER.get(), ThrownItemRenderer::new);
        }
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event)
        {
        }
    }
}
