package com.badgersmc.tsspear.infrastructure.listener;

import com.badgersmc.tsspear.application.TSSpearRuntime;
import com.badgersmc.tsspear.application.bus.CombatSampleEvent;
import com.badgersmc.tsspear.domain.model.sample.CombatSample;
import com.badgersmc.tsspear.domain.model.world.WorldPosition;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.time.Instant;

public final class CombatListener implements Listener {
    private final TSSpearRuntime runtime;

    public CombatListener(TSSpearRuntime runtime) {
        this.runtime = runtime;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageByEntityEvent event) {
        Player attacker = resolveAttacker(event.getDamager());
        if (attacker == null) {
            return;
        }
        Entity victimEntity = event.getEntity();
        if (!(victimEntity instanceof Player victim)) {
            return;
        }
        if (attacker.getUniqueId().equals(victim.getUniqueId())) {
            return;
        }

        Location attackerLoc = attacker.getLocation();
        Location victimLoc = victim.getLocation();
        if (!attackerLoc.getWorld().equals(victimLoc.getWorld())) {
            return;
        }

        double distance = attackerLoc.distance(victimLoc);
        boolean critical = event.isCritical();
        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        CombatSample sample = new CombatSample(
            attacker.getUniqueId(),
            attacker.getName(),
            victim.getUniqueId(),
            victim.getName(),
            attacker.getWorld().getFullTime(),
            Instant.now(),
            toPosition(attackerLoc),
            toPosition(victimLoc),
            distance,
            event.getFinalDamage(),
            critical,
            attacker.getPing(),
            weapon.getType().name()
        );

        runtime.stateEngine().getOrCreate(attacker.getUniqueId(), attacker.getName());
        runtime.eventBus().publish(new CombatSampleEvent(sample, Instant.now()));
    }

    private static Player resolveAttacker(Entity damager) {
        if (damager instanceof Player player) {
            return player;
        }
        if (damager instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter) {
            return shooter;
        }
        return null;
    }

    private static WorldPosition toPosition(Location loc) {
        return new WorldPosition(
            loc.getWorld().getName(),
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getYaw(),
            loc.getPitch()
        );
    }
}