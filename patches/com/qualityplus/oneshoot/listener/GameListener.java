package com.qualityplus.oneshoot.listener;

import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Component;
import com.qualityplus.assistant.util.StringUtils;
import com.qualityplus.oneshoot.OneShoot;
import com.qualityplus.oneshoot.api.domain.OneShootGame;
import com.qualityplus.oneshoot.api.domain.OneShootGameStatus;
import com.qualityplus.oneshoot.api.service.OneShootService;
import com.qualityplus.oneshoot.base.config.Messages;
import com.qualityplus.oneshoot.persistance.data.OneShootPlayer;
import com.qualityplus.oneshoot.util.OneShootMetadata;
import com.qualityplus.oneshoot.util.OneShootTitleUtil;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.metadata.FixedMetadataValue;

@Component
public final class GameListener implements Listener {
    private static final String PROCESSED_META = "oneshot_arrow_processed";

    @Inject
    private OneShootService service;
    @Inject
    private Messages messages;

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }
        Optional<OneShootGame> gameOpt = this.service.getGameByPlayer(player.getUniqueId());
        if (gameOpt.isEmpty()) {
            return;
        }
        OneShootGame game = gameOpt.get();
        if (game.getStatus() != OneShootGameStatus.IN_GAME) {
            event.setCancelled(true);
            return;
        }
        if (!game.isPlayersTurn(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(StringUtils.color(this.messages.gameMessages.itsNotYourTurn));
            return;
        }
        if (event.getProjectile() instanceof Arrow arrow) {
            arrow.setMetadata(OneShootMetadata.ONE_SHOOT_METADATA, new FixedMetadataValue(OneShoot.getInstance(), true));
            arrow.setDamage(20.0); // one-shot potential if damage event is used
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Arrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player shooter)) {
            return;
        }
        if (!(event.getEntity() instanceof Player victim)) {
            return;
        }
        Optional<OneShootGame> gameOpt = this.service.getGameByPlayer(shooter.getUniqueId());
        if (gameOpt.isEmpty() || gameOpt.get().getStatus() != OneShootGameStatus.IN_GAME) {
            return;
        }
        OneShootGame game = gameOpt.get();
        if (!game.isValidShoot(shooter.getUniqueId(), victim.getUniqueId())) {
            event.setCancelled(true);
            return;
        }
        if (arrow.hasMetadata(PROCESSED_META)) {
            event.setCancelled(true);
            return;
        }
        arrow.setMetadata(PROCESSED_META, new FixedMetadataValue(OneShoot.getInstance(), true));
        event.setCancelled(true);
        arrow.remove();
        this.resolveHit(game, shooter, victim);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = false)
    public void onArrowHit(ProjectileHitEvent event) {
        if (!(event.getEntity() instanceof Arrow arrow)) {
            return;
        }
        if (!(arrow.getShooter() instanceof Player shooter)) {
            return;
        }
        Optional<OneShootGame> gameOpt = this.service.getGameByPlayer(shooter.getUniqueId());
        if (gameOpt.isEmpty() || gameOpt.get().getStatus() != OneShootGameStatus.IN_GAME) {
            return;
        }
        if (arrow.hasMetadata(PROCESSED_META)) {
            return;
        }
        OneShootGame game = gameOpt.get();
        if (!game.isPlayersTurn(shooter.getUniqueId())) {
            return;
        }

        if (event.getHitEntity() instanceof Player receiver && game.isValidShoot(shooter.getUniqueId(), receiver.getUniqueId())) {
            arrow.setMetadata(PROCESSED_META, new FixedMetadataValue(OneShoot.getInstance(), true));
            arrow.remove();
            this.resolveHit(game, shooter, receiver);
            return;
        }

        // Miss (block / wrong entity) — advance turn on main thread
        arrow.setMetadata(PROCESSED_META, new FixedMetadataValue(OneShoot.getInstance(), true));
        arrow.remove();
        Bukkit.getScheduler().runTask(OneShoot.getInstance(), () -> {
            if (game.getStatus() != OneShootGameStatus.IN_GAME) {
                return;
            }
            OneShootPlayer current = game.getCurrentPlayer();
            if (current != null) {
                OneShootTitleUtil.sendYourMissed(current);
            }
            game.handleNextTurn();
        });
    }

    private void resolveHit(OneShootGame game, Player shooter, Player victim) {
        Bukkit.getScheduler().runTask(OneShoot.getInstance(), () -> {
            if (game.getStatus() != OneShootGameStatus.IN_GAME) {
                return;
            }
            // Apply lethal damage so the hit feels real, then end the match
            victim.setHealth(0.0);
            this.service.finishGame(game, game.getCurrentPlayer(), true);
        });
    }
}
