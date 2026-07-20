package com.qualityplus.oneshoot.api.domain;

import com.qualityplus.assistant.util.StringUtils;
import com.qualityplus.assistant.util.itemstack.ItemStackUtils;
import com.qualityplus.assistant.util.location.ALocation;
import com.qualityplus.assistant.util.time.Markable;
import com.qualityplus.oneshoot.OneShoot;
import com.qualityplus.oneshoot.persistance.data.OneShootPlayer;
import com.qualityplus.oneshoot.util.OneShootTitleUtil;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

public final class OneShootGame {
   private UUID gameId;
   private OneShootGameStatus status;
   private int countdown;
   private OneShootArena arena;
   private Scoreboard scoreboard;
   private List<OneShootPlayer> players;
   private final Map<UUID, Integer> spawnCache = new HashMap<>();
   private OneShootGameTurn oneShootGameTurn;
   private Markable gameTime;

   public void sendMessage(String message) {
      this.players.forEach(p -> p.getBukkitPlayer().ifPresent(u -> u.sendMessage(StringUtils.color(message))));
   }

   public Optional<OneShootPlayer> getPlayer(UUID uuid) {
      return this.players.stream().filter(p -> p.getUuid() != null && p.getUuid().equals(uuid)).findFirst();
   }

   public void giveOneShootItems(OneShootPlayer oneShootPlayer) {
      oneShootPlayer.getBukkitPlayer().ifPresent(p -> {
         ItemStack bow = ItemStackUtils.makeItem(OneShoot.getApi().getConfig().getGameItemConfig().bowItem);
         ItemStack arrow = ItemStackUtils.makeItem(OneShoot.getApi().getConfig().getGameItemConfig().arrowItem);
         if (bow != null && !p.getInventory().contains(org.bukkit.Material.BOW)) {
            p.getInventory().addItem(bow);
         }
         if (arrow != null && !p.getInventory().contains(org.bukkit.Material.ARROW)) {
            p.getInventory().addItem(arrow);
         }
      });
   }

   /** Give bow+arrow to every player in the match (used at game start). */
   public void giveItemsToAllPlayers() {
      for (OneShootPlayer player : this.players) {
         player.getBukkitPlayer().ifPresent(p -> p.getInventory().clear());
         this.giveOneShootItems(player);
      }
   }

   /** Ensure both players still have bow/arrow without wiping inventories. */
   public void ensureItemsForAllPlayers() {
      for (OneShootPlayer player : this.players) {
         this.giveOneShootItems(player);
      }
   }

   public OneShootPlayer getCurrentPlayer() {
      return this.oneShootGameTurn == null ? null : this.oneShootGameTurn.getPlayer();
   }

   public OneShootPlayer getNextPlayer() {
      if (this.players == null || this.players.isEmpty()) {
         return null;
      }
      OneShootPlayer current = this.getCurrentPlayer();
      if (current == null) {
         return this.players.get(0);
      }
      for (OneShootPlayer oneShootPlayer : this.players) {
         if (!current.getUuid().equals(oneShootPlayer.getUuid())) {
            return oneShootPlayer;
         }
      }
      return null;
   }

   public boolean isPlayersTurn(UUID uuid) {
      OneShootPlayer current = this.getCurrentPlayer();
      return current != null && current.getUuid() != null && current.getUuid().equals(uuid);
   }

   public void handleNextTurn() {
      if (this.status.equals(OneShootGameStatus.FINISHED)) {
         return;
      }
      OneShootPlayer nextPlayer = this.getNextPlayer();
      if (nextPlayer == null) {
         return;
      }
      OneShootArena.ArenaSettings settings = this.arena.arenaSettings;
      this.oneShootGameTurn.setPlayer(nextPlayer);
      this.oneShootGameTurn.setTurnTime(new Markable(settings.getTurnTime().getEffectiveTime(), System.currentTimeMillis()));
      // Keep both players geared; refill missing bow/arrow for everyone
      this.ensureItemsForAllPlayers();
      OneShootTitleUtil.sendYourTurnTitle(nextPlayer);
   }

   public void setCacheSpawn(OneShootPlayer paramPlayer) {
      int spawnCount = 0;
      java.util.Set<Integer> used = new java.util.HashSet<>(this.spawnCache.values());
      while (used.contains(spawnCount)) {
         spawnCount++;
      }
      this.spawnCache.put(paramPlayer.getUuid(), spawnCount);
   }

   public void teleportToSpawn(OneShootPlayer paramPlayer) {
      Integer playerSpawn = this.spawnCache.getOrDefault(paramPlayer.getUuid(), null);
      if (playerSpawn != null) {
         int spawnCount = 0;

         for (ALocation location : this.arena.getArenaSettings().getPlayerSpawnPoints()) {
            if (spawnCount == playerSpawn) {
               paramPlayer.getBukkitPlayer().ifPresent(p -> p.teleport(location.getLocation()));
               break;
            }

            spawnCount++;
         }
      }
   }

   public boolean isValidShoot(UUID shooter, UUID receiver) {
      if (this.oneShootGameTurn.getPlayer() == null) {
         return false;
      } else {
         OneShootPlayer current = this.getCurrentPlayer();
         OneShootPlayer opponent = this.getNextPlayer();
         if (current != null && opponent != null) {
            return !current.getUuid().equals(shooter) ? false : opponent.getUuid().equals(receiver);
         } else {
            return false;
         }
      }
   }

   OneShootGame(
      UUID gameId,
      OneShootGameStatus status,
      int countdown,
      OneShootArena arena,
      Scoreboard scoreboard,
      List<OneShootPlayer> players,
      OneShootGameTurn oneShootGameTurn,
      Markable gameTime
   ) {
      this.gameId = gameId;
      this.status = status;
      this.countdown = countdown;
      this.arena = arena;
      this.scoreboard = scoreboard;
      this.players = players;
      this.oneShootGameTurn = oneShootGameTurn;
      this.gameTime = gameTime;
   }

   public static OneShootGame.OneShootGameBuilder builder() {
      return new OneShootGame.OneShootGameBuilder();
   }

   public UUID getGameId() {
      return this.gameId;
   }

   public OneShootGameStatus getStatus() {
      return this.status;
   }

   public int getCountdown() {
      return this.countdown;
   }

   public OneShootArena getArena() {
      return this.arena;
   }

   public Scoreboard getScoreboard() {
      return this.scoreboard;
   }

   public List<OneShootPlayer> getPlayers() {
      return this.players;
   }

   public Map<UUID, Integer> getSpawnCache() {
      return this.spawnCache;
   }

   public OneShootGameTurn getOneShootGameTurn() {
      return this.oneShootGameTurn;
   }

   public Markable getGameTime() {
      return this.gameTime;
   }

   public void setGameId(UUID gameId) {
      this.gameId = gameId;
   }

   public void setStatus(OneShootGameStatus status) {
      this.status = status;
   }

   public void setCountdown(int countdown) {
      this.countdown = countdown;
   }

   public void setArena(OneShootArena arena) {
      this.arena = arena;
   }

   public void setScoreboard(Scoreboard scoreboard) {
      this.scoreboard = scoreboard;
   }

   public void setPlayers(List<OneShootPlayer> players) {
      this.players = players;
   }

   public void setOneShootGameTurn(OneShootGameTurn oneShootGameTurn) {
      this.oneShootGameTurn = oneShootGameTurn;
   }

   public void setGameTime(Markable gameTime) {
      this.gameTime = gameTime;
   }

   @Override
   public boolean equals(Object o) {
      if (o == this) {
         return true;
      } else if (!(o instanceof OneShootGame other)) {
         return false;
      } else if (this.getCountdown() != other.getCountdown()) {
         return false;
      } else {
         Object this$gameId = this.getGameId();
         Object other$gameId = other.getGameId();
         if (this$gameId == null ? other$gameId == null : this$gameId.equals(other$gameId)) {
            Object this$status = this.getStatus();
            Object other$status = other.getStatus();
            if (this$status == null ? other$status == null : this$status.equals(other$status)) {
               Object this$arena = this.getArena();
               Object other$arena = other.getArena();
               if (this$arena == null ? other$arena == null : this$arena.equals(other$arena)) {
                  Object this$scoreboard = this.getScoreboard();
                  Object other$scoreboard = other.getScoreboard();
                  if (this$scoreboard == null ? other$scoreboard == null : this$scoreboard.equals(other$scoreboard)) {
                     Object this$players = this.getPlayers();
                     Object other$players = other.getPlayers();
                     if (this$players == null ? other$players == null : this$players.equals(other$players)) {
                        Object this$spawnCache = this.getSpawnCache();
                        Object other$spawnCache = other.getSpawnCache();
                        if (this$spawnCache == null ? other$spawnCache == null : this$spawnCache.equals(other$spawnCache)) {
                           Object this$oneShootGameTurn = this.getOneShootGameTurn();
                           Object other$oneShootGameTurn = other.getOneShootGameTurn();
                           if (this$oneShootGameTurn == null ? other$oneShootGameTurn == null : this$oneShootGameTurn.equals(other$oneShootGameTurn)) {
                              Object this$gameTime = this.getGameTime();
                              Object other$gameTime = other.getGameTime();
                              return this$gameTime == null ? other$gameTime == null : this$gameTime.equals(other$gameTime);
                           } else {
                              return false;
                           }
                        } else {
                           return false;
                        }
                     } else {
                        return false;
                     }
                  } else {
                     return false;
                  }
               } else {
                  return false;
               }
            } else {
               return false;
            }
         } else {
            return false;
         }
      }
   }

   @Override
   public int hashCode() {
      int PRIME = 59;
      int result = 1;
      result = result * 59 + this.getCountdown();
      Object $gameId = this.getGameId();
      result = result * 59 + ($gameId == null ? 43 : $gameId.hashCode());
      Object $status = this.getStatus();
      result = result * 59 + ($status == null ? 43 : $status.hashCode());
      Object $arena = this.getArena();
      result = result * 59 + ($arena == null ? 43 : $arena.hashCode());
      Object $scoreboard = this.getScoreboard();
      result = result * 59 + ($scoreboard == null ? 43 : $scoreboard.hashCode());
      Object $players = this.getPlayers();
      result = result * 59 + ($players == null ? 43 : $players.hashCode());
      Object $spawnCache = this.getSpawnCache();
      result = result * 59 + ($spawnCache == null ? 43 : $spawnCache.hashCode());
      Object $oneShootGameTurn = this.getOneShootGameTurn();
      result = result * 59 + ($oneShootGameTurn == null ? 43 : $oneShootGameTurn.hashCode());
      Object $gameTime = this.getGameTime();
      return result * 59 + ($gameTime == null ? 43 : $gameTime.hashCode());
   }

   @Override
   public String toString() {
      return "OneShootGame(gameId="
         + this.getGameId()
         + ", status="
         + this.getStatus()
         + ", countdown="
         + this.getCountdown()
         + ", arena="
         + this.getArena()
         + ", scoreboard="
         + this.getScoreboard()
         + ", players="
         + this.getPlayers()
         + ", spawnCache="
         + this.getSpawnCache()
         + ", oneShootGameTurn="
         + this.getOneShootGameTurn()
         + ", gameTime="
         + this.getGameTime()
         + ")";
   }

   public static class OneShootGameBuilder {
      private UUID gameId;
      private OneShootGameStatus status;
      private int countdown;
      private OneShootArena arena;
      private Scoreboard scoreboard;
      private List<OneShootPlayer> players;
      private OneShootGameTurn oneShootGameTurn;
      private Markable gameTime;

      OneShootGameBuilder() {
      }

      public OneShootGame.OneShootGameBuilder gameId(UUID gameId) {
         this.gameId = gameId;
         return this;
      }

      public OneShootGame.OneShootGameBuilder status(OneShootGameStatus status) {
         this.status = status;
         return this;
      }

      public OneShootGame.OneShootGameBuilder countdown(int countdown) {
         this.countdown = countdown;
         return this;
      }

      public OneShootGame.OneShootGameBuilder arena(OneShootArena arena) {
         this.arena = arena;
         return this;
      }

      public OneShootGame.OneShootGameBuilder scoreboard(Scoreboard scoreboard) {
         this.scoreboard = scoreboard;
         return this;
      }

      public OneShootGame.OneShootGameBuilder players(List<OneShootPlayer> players) {
         this.players = players;
         return this;
      }

      public OneShootGame.OneShootGameBuilder oneShootGameTurn(OneShootGameTurn oneShootGameTurn) {
         this.oneShootGameTurn = oneShootGameTurn;
         return this;
      }

      public OneShootGame.OneShootGameBuilder gameTime(Markable gameTime) {
         this.gameTime = gameTime;
         return this;
      }

      public OneShootGame build() {
         return new OneShootGame(this.gameId, this.status, this.countdown, this.arena, this.scoreboard, this.players, this.oneShootGameTurn, this.gameTime);
      }

      @Override
      public String toString() {
         return "OneShootGame.OneShootGameBuilder(gameId="
            + this.gameId
            + ", status="
            + this.status
            + ", countdown="
            + this.countdown
            + ", arena="
            + this.arena
            + ", scoreboard="
            + this.scoreboard
            + ", players="
            + this.players
            + ", oneShootGameTurn="
            + this.oneShootGameTurn
            + ", gameTime="
            + this.gameTime
            + ")";
      }
   }
}
