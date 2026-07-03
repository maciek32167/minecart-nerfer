package pl.tntnerfer;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class ExplosionListener implements Listener {

    private final TNTMinecartNerfer plugin;

    // Track TNT minecarts about to explode so we can identify their damage
    private final Set<UUID> pendingExplosions = new HashSet<>();

    public ExplosionListener(TNTMinecartNerfer plugin) {
        this.plugin = plugin;
    }

    // Step 1: TNT minecart is about to explode — modify blast radius and track it
    @EventHandler(priority = EventPriority.HIGH)
    public void onExplosionPrime(ExplosionPrimeEvent event) {
        if (!(event.getEntity() instanceof ExplosiveMinecart minecart)) return;

        double multiplier = plugin.getConfig().getDouble("block-damage-multiplier", 1.0);

        // Modify blast radius — this controls block destruction range
        // We use sqrt because damage scales roughly with radius squared
        float newRadius = (float) (event.getRadius() * Math.sqrt(multiplier));
        event.setRadius(Math.max(0f, newRadius));

        // Track this minecart for entity damage modification
        pendingExplosions.add(minecart.getUniqueId());
    }

    // Step 2: After explosion fires — clean up tracking
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof ExplosiveMinecart minecart)) return;
        // Remove from tracking (explosion already happened)
        pendingExplosions.remove(minecart.getUniqueId());
    }

    // Step 3: Modify entity/player damage from TNT minecart explosions
    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) return;

        Entity damager = event.getDamager();

        // Direct check — damager IS the TNT minecart
        if (damager instanceof ExplosiveMinecart) {
            applyEntityMultiplier(event);
            return;
        }

        // Fallback — check via DamageSource (Paper 1.21 API)
        try {
            Entity causingEntity = event.getDamageSource().getCausingEntity();
            if (causingEntity instanceof ExplosiveMinecart) {
                applyEntityMultiplier(event);
            }
        } catch (Exception ignored) {
            // DamageSource may not always be available
        }
    }

    private void applyEntityMultiplier(EntityDamageByEntityEvent event) {
        double multiplier = plugin.getConfig().getDouble("entity-damage-multiplier", 1.0);
        event.setDamage(event.getDamage() * multiplier);
    }
}
