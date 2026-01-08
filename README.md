# InnoAdditions

## Boss MobEffect (`boss`)

This repo contains a custom MobEffect called `innoadditions:boss`.

### How it’s configured now

All numbers are in a Forge **COMMON** config file:

- `config/innoadditions-boss-effect.toml`

The config supports **multiple effect levels** (MobEffect amplifier values).

- `boss_effect.levels = N`
  - `N = 1` => only amplifier `0` is used
  - `N = 3` => amplifier `0..2` each gets its own tuned values

You configure values either in:
- `boss_effect.defaults` (template/fallback values)
- `boss_effect.levels.level0`, `boss_effect.levels.level1`, ... (overrides per amplifier)

If a value is not specified in a specific `levelX`, it falls back to `boss_effect.defaults`.

### What it does (server side)

When a mob has the `boss` effect (you said you’ll apply it to bosses yourself), the following server-side logic is applied:

- **Regen from max HP**
  - If there are **no players within `nearbyPlayerRadius`**, regen becomes **`noPlayersRegenPerTick` * maxHP per tick**.
  - Otherwise regen per second is:
    - `baseRegenPerSecond + regenPerAttackerPerSecond * uniqueAttackersLastWindow`

- **Damage aura**
  - Every `auraIntervalTicks`, boss damages all players in `auraRadius`.
  - Damage is `auraDamageFraction * maxHP`.

- **Teleport on attack (anti-kiting)**
  - If a player hits the boss `hitsRequiredForTeleport` times and the boss hasn’t damaged that player with a direct attack in-between (aura doesn’t count), then when the boss attacks that player it can teleport to a random nearby spot (`teleportMinDist..teleportMaxDist`).

- **Unreachable buff**
  - If the boss teleports `maxTeleportsTracked` times and after each teleport it still can’t find a path to the player, it gains the unreachable buff:
    - `unreachableRegenMultiplier` × regen
    - `unreachableAuraMultiplier` × aura damage

### Where the logic lives

- MobEffect registration: `dev.necr0manthre.innoadditions.init.InnoMobEffects`
- Effect class: `dev.necr0manthre.innoadditions.mob_effect.BossMobEffect`
- Config: `dev.necr0manthre.innoadditions.config.BossEffectForgeConfig`
- Server-side behavior: `dev.necr0manthre.innoadditions.boss.BossEffectTracker`

### Notes

- This implementation is event-driven (Forge events) so it works for any mob carrying the effect.
- Aura uses `mobAttack` damage source; if you want a custom damage type, we can switch it.
