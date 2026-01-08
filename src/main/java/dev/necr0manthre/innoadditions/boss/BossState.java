package dev.necr0manthre.innoadditions.boss;

import dev.necr0manthre.innoadditions.DamageTypeUtils;
import dev.necr0manthre.innoadditions.Innoadditions;
import dev.necr0manthre.innoadditions.config.BossEffectLevelConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;

import java.util.*;

public final class BossState {
    /**
     * Per-boss data. WeakHashMap so it doesn't prevent GC when entities unload.
     */
    private static final Map<LivingEntity, BossState> STATES = Collections.synchronizedMap(new WeakHashMap<>());

    public static BossState getOrCreate(LivingEntity entity) {
        return STATES.computeIfAbsent(entity, BossState::new);
    }

    private final LivingEntity boss;

    /**
     * attacker UUID -> last tick they hit the boss
     */
    private final Map<UUID, Integer> attackerLastHitTick = new HashMap<>();

    /**
     * player UUID -> count of hits dealt since last time boss hit them
     */
    private final Map<UUID, Integer> hitsWithoutRetaliation = new HashMap<>();

    /**
     * Teleport history for unreachable logic.
     */
    private int teleportsPerformed = 0;
    private int unreachableStreak = 0;
    private boolean unreachableBuff = false;

    private int tickCounter = 0;
    private int auraCooldown = 40;
    private float regenRemainder = 0F;

    /**
     * Track last player target (used for teleport/explosion logic).
     */
    private UUID lastTargetPlayer = null;
    private int teleportRollCooldown = 0;
    private int explosionRollCooldown = 0;

    private BossState(LivingEntity boss) {
        this.boss = boss;
    }

    public void tick(BossEffectLevelConfig cfg) {
        tickCounter++;
        pruneAttackers(cfg.attackerWindowTicks());

        auraCooldown--;
        if (auraCooldown <= 0) {
            auraCooldown = Math.max(1, cfg.auraIntervalTicks());
            doAuraPulse(cfg);
        }

        tickTeleportAndExplosions(cfg);
        doRegenTick(cfg);
    }

    public void noteBossAttemptedAttack(ServerPlayer player) {
        lastTargetPlayer = player.getUUID();
    }

    public void recordPlayerHitBoss(ServerPlayer player) {
        attackerLastHitTick.put(player.getUUID(), tickCounter);
        hitsWithoutRetaliation.merge(player.getUUID(), 1, Integer::sum);
    }

    public void recordBossHitPlayer(ServerPlayer player) {
        hitsWithoutRetaliation.remove(player.getUUID());
    }

    /**
     * Prevent bosses from taking damage from their own block-breaking explosions.
     */
    public boolean shouldCancelSelfExplosion(DamageSource src) {
        return src != null && src.getEntity() == boss && DamageTypeUtils.isExplosion(src);
    }

    private void pruneAttackers(int windowTicks) {
        int cutoff = tickCounter - windowTicks;
        attackerLastHitTick.values().removeIf(last -> last < cutoff);
    }

    private void tickTeleportAndExplosions(BossEffectLevelConfig cfg) {
        if (!(boss.level() instanceof ServerLevel level)) return;

        // Try to pick a target player near the boss.
        ServerPlayer target = pickTargetPlayer(level, cfg.nearbyPlayerRadius());
        if (target != null) {
            lastTargetPlayer = target.getUUID();
        } else if (lastTargetPlayer != null) {
            var p = level.getPlayerByUUID(lastTargetPlayer);
            if (p instanceof ServerPlayer sp) {
                target = sp;
            }
        }
        if (target == null || !target.isAlive() || target.isSpectator()) return;

        teleportRollCooldown--;
        if (teleportRollCooldown <= 0) {
            teleportRollCooldown = Math.max(1, cfg.teleportCheckIntervalTicks());
            rollTeleport(cfg, target);
        }

        explosionRollCooldown--;
        if (explosionRollCooldown <= 0 && !attackerLastHitTick.isEmpty()) {
            explosionRollCooldown = Math.max(1, cfg.explosionCheckIntervalTicks());
            rollExplosion(cfg, target);
        }
    }

    private void rollTeleport(BossEffectLevelConfig cfg, ServerPlayer target) {
        double chance = computeAbilityChance(cfg.teleportBaseChance(), cfg.teleportChancePerHitWithoutRetaliation(), cfg.teleportChanceBonusIfPlayerUnreachable(), target);
        if (boss.getRandom().nextDouble() >= chance) return;

        boolean teleported = tryTeleportNearPlayer(target, cfg);
        if (teleported) {
            evaluateReachabilityAfterTeleport(target, cfg);
        }
    }

    private void rollExplosion(BossEffectLevelConfig cfg, ServerPlayer target) {
        double chance = computeAbilityChance(cfg.explosionBaseChance(), cfg.explosionChancePerHitWithoutRetaliation(), cfg.explosionChanceBonusIfPlayerUnreachable(), target);
        if (boss.getRandom().nextDouble() >= chance) return;

        doBlockBreakingExplosion(cfg);
    }

    private double computeAbilityChance(double base, double perStack, double unreachableBonus, ServerPlayer target) {
        double chance = Mth.clamp(base, 0D, 1D);

        int stacks = hitsWithoutRetaliation.getOrDefault(target.getUUID(), 0);
        if (stacks > 0) {
            chance += perStack * stacks;
        }

        // Bonus if boss can't path to the target.
        if (!canPathTo(target)) {
            chance += unreachableBonus;
        }

        // Small synergy: if unreachableBuff is active (after 3 bad teleports), also increase chance a bit.
        if (unreachableBuff) {
            chance += unreachableBonus * 0.25D;
        }

        return Mth.clamp(chance, 0D, 1D);
    }

    private void doBlockBreakingExplosion(BossEffectLevelConfig cfg) {
        if (!(boss.level() instanceof ServerLevel level)) return;

        float power = Math.max(0F, cfg.explosionStrength());
        if (power <= 0F) return;

        level.explode(
                boss,
                boss.getX(), boss.getY(0.5D), boss.getZ(),
                power,
                Level.ExplosionInteraction.MOB
        );
    }

    private ServerPlayer pickTargetPlayer(ServerLevel level, double radius) {
        double r = Math.max(0D, radius);
        if (r <= 0D) return null;

        AABB box = boss.getBoundingBox().inflate(Math.max(r, 16D));
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box, p -> !p.isSpectator() && p.isAlive());
        if (players.isEmpty()) return null;

        ServerPlayer best = null;
        double bestD = Double.MAX_VALUE;
        for (ServerPlayer p : players) {
            double d = boss.distanceToSqr(p);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    private boolean tryTeleportNearPlayer(ServerPlayer player, BossEffectLevelConfig cfg) {
        if (!(boss.level() instanceof ServerLevel level)) return false;

        BlockPos playerPos = player.blockPosition();
        BlockPos best = findTeleportPos(level, playerPos, cfg);
        if (best == null) return false;

        boolean ok = boss.randomTeleport(best.getX() + 0.5D, best.getY(), best.getZ() + 0.5D, true);
        if (ok) {
            teleportsPerformed++;
            int cap = Math.max(1, cfg.maxTeleportsTracked());
            if (teleportsPerformed > cap) teleportsPerformed = cap;
        }
        return ok;
    }

    private void evaluateReachabilityAfterTeleport(ServerPlayer player, BossEffectLevelConfig cfg) {
        boolean reachable = canPathTo(player);
        if (!reachable) unreachableStreak++;
        else unreachableStreak = 0;

        int cap = Math.max(1, cfg.maxTeleportsTracked());
        if (!unreachableBuff && teleportsPerformed >= cap && unreachableStreak >= cap) {
            unreachableBuff = true;
        }
    }

    private BlockPos findTeleportPos(ServerLevel level, BlockPos around, BossEffectLevelConfig cfg) {
        RandomSource rand = level.getRandom();
        int tries = 24;
        for (int i = 0; i < tries; i++) {
            double angle = rand.nextDouble() * (Math.PI * 2);
            double dist = Mth.lerp(rand.nextDouble(), cfg.teleportMinDist(), cfg.teleportMaxDist());
            int dx = Mth.floor(Math.cos(angle) * dist);
            int dz = Mth.floor(Math.sin(angle) * dist);

            BlockPos base = around.offset(dx, 0, dz);
            BlockPos pos = findSurface(level, base);
            if (pos == null) continue;

            if (level.noCollision(boss, boss.getBoundingBox().move(pos.getX() + 0.5D - boss.getX(), pos.getY() - boss.getY(), pos.getZ() + 0.5D - boss.getZ()))) {
                return pos;
            }
        }
        return null;
    }

    private BlockPos findSurface(ServerLevel level, BlockPos start) {
        int minY = Math.max(level.getMinBuildHeight(), start.getY() - 6);
        int maxY = Math.min(level.getMaxBuildHeight() - 1, start.getY() + 6);
        BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
        for (int y = maxY; y >= minY; y--) {
            m.set(start.getX(), y, start.getZ());
            if (!level.isEmptyBlock(m)) continue;
            if (!level.isEmptyBlock(m.above())) continue;
            if (level.getBlockState(m.below()).isFaceSturdy(level, m.below(), net.minecraft.core.Direction.UP)) {
                return m.immutable();
            }
        }
        return null;
    }

    private boolean canPathTo(ServerPlayer player) {
        PathNavigation nav;
        if (boss instanceof Mob mob)
            nav = mob.getNavigation();
        else return true;
        var path = nav.createPath(player, 1);
        return path != null && path.canReach();
    }

    private void doAuraPulse(BossEffectLevelConfig cfg) {
        if (!(boss.level() instanceof ServerLevel level)) return;

        double r = cfg.auraRadius();
        if (r <= 0D) return;

        AABB box = boss.getBoundingBox().inflate(r);
        List<ServerPlayer> players = level.getEntitiesOfClass(ServerPlayer.class, box, p -> !p.isSpectator() && p.isAlive());
        if (players.isEmpty()) return;

        float mult = unreachableBuff ? cfg.unreachableAuraMultiplier() : 1F;

        float maxHp = boss.getMaxHealth();
        float dmg = maxHp * cfg.auraDamageFraction() * mult;
        if (dmg <= 0F) return;
        var damageTypeRegistry = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE);
        var damageType = damageTypeRegistry.getHolderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, Innoadditions.rl("boss_aura")));
        var damageSource = new DamageSource(damageType, boss);
        for (ServerPlayer p : players) {
            if (boss.distanceToSqr(p) <= r * r) {
                p.hurt(damageSource, dmg);
            }
        }
    }

    public float getBlockExplosionResistance(Explosion explosion, BlockGetter blockGetter, BlockPos pos, BlockState state, FluidState fluidState, float resistance){
        return !state.isAir() && !state.is(BlockTags.WITHER_IMMUNE) ? Math.min(0.2F, resistance) : resistance;
    }

    private void doRegenTick(BossEffectLevelConfig cfg) {
        if (!boss.isAlive()) return;
        if (boss.getHealth() >= boss.getMaxHealth()) return;

        float maxHp = boss.getMaxHealth();
        if (maxHp <= 0F) return;

        boolean hasNearbyPlayers = hasNearbyPlayers(cfg.nearbyPlayerRadius());

        float regen;
        if (!hasNearbyPlayers) {
            regen = maxHp * cfg.noPlayersRegenPerTick();
        } else {
            int attackers = Math.min(attackerLastHitTick.size(), Math.max(0, cfg.maxAttackersForScaling()));
            float perSecond = cfg.baseRegenPerSecond() + cfg.regenPerAttackerPerSecond() * attackers;
            float mult = unreachableBuff ? cfg.unreachableRegenMultiplier() : 1F;
            regen = (maxHp * perSecond * mult) / 20F;
        }

        regenRemainder += regen;
        float apply = (float) Math.floor(regenRemainder * 1000F) / 1000F;
        if (apply <= 0F) return;
        regenRemainder -= apply;

        boss.heal(apply);
    }

    private boolean hasNearbyPlayers(double radius) {
        if (!(boss.level() instanceof ServerLevel level)) return false;
        double r = Math.max(0D, radius);
        if (r <= 0D) return false;
        AABB box = boss.getBoundingBox().inflate(r);
        return !level.getEntitiesOfClass(ServerPlayer.class, box, p -> !p.isSpectator() && p.isAlive()).isEmpty();
    }
}
