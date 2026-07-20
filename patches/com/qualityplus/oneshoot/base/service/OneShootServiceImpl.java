package com.qualityplus.oneshoot.base.service;

import com.google.common.collect.ImmutableList;
import com.qualityplus.assistant.TheAssistantPlugin;
import com.qualityplus.assistant.api.util.IPlaceholder;
import com.qualityplus.assistant.lib.de.tr7zw.changeme.nbtapi.NBTItem;
import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Component;
import com.qualityplus.assistant.util.StringUtils;
import com.qualityplus.assistant.util.placeholder.Placeholder;
import com.qualityplus.oneshoot.OneShoot;
import com.qualityplus.oneshoot.api.domain.OneShootArena;
import com.qualityplus.oneshoot.api.domain.OneShootGame;
import com.qualityplus.oneshoot.api.domain.OneShootGameStatus;
import com.qualityplus.oneshoot.api.domain.OneShootGameTurn;
import com.qualityplus.oneshoot.api.exception.AlreadyInGameException;
import com.qualityplus.oneshoot.api.exception.ArenaNotAvailableException;
import com.qualityplus.oneshoot.api.service.OneShootArenaService;
import com.qualityplus.oneshoot.api.service.OneShootService;
import com.qualityplus.oneshoot.base.config.Config;
import com.qualityplus.oneshoot.base.config.Messages;
import com.qualityplus.oneshoot.persistance.data.OneShootPlayer;
import com.qualityplus.oneshoot.util.Util;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

@Component
public final class OneShootServiceImpl implements OneShootService {
   private final Map<UUID, OneShootGame> games = new HashMap<>();
   @Inject
   private OneShootArenaService arenaService;
   @Inject
   private Messages messages;
   @Inject
   private Config config;

   private int minPlayers() {
      return this.config.getMinPlayers();
   }

   /**
    * Give the lobby leave item only when it is enabled and has a valid material.
    * Stock config ships with material: null / enabled: false, which previously NPE'd on join.
    */
   private void prepareLobbyInventory(Player player) {
      player.getInventory().clear();
      Config.LobbyItemConfig.LobbyItem lobbyItem = this.config.getLobbyItemConfig().leaveItem;
      if (lobbyItem == null || !lobbyItem.isEnabled() || lobbyItem.getMaterial() == null) {
         return;
      }
      ItemStack leaveButton = lobbyItem.getMaterial().parseItem();
      if (leaveButton == null) {
         return;
      }
      ItemMeta leaveButtonMeta = leaveButton.getItemMeta();
      if (leaveButtonMeta != null) {
         if (lobbyItem.getDisplayName() != null) {
            leaveButtonMeta.setDisplayName(StringUtils.color(lobbyItem.getDisplayName()));
         }
         leaveButtonMeta.setCustomModelData(lobbyItem.getCustomModelData());
         leaveButton.setItemMeta(leaveButtonMeta);
      }
      NBTItem leaveNBTItem = new NBTItem(leaveButton);
      leaveNBTItem.setBoolean("UNO_LEAVE_NBT_KEY", true);
      int slot = lobbyItem.getSlot() > 0 ? lobbyItem.getSlot() : 8;
      player.getInventory().setItem(slot, leaveNBTItem.getItem());
   }

   @Override
   public void joinGame(OneShootPlayer player) throws ArenaNotAvailableException, AlreadyInGameException {
      List<OneShootGame> availableArenas = this.getAvailableGames();
      if (availableArenas.isEmpty()) {
         throw new ArenaNotAvailableException();
      } else {
         OneShootGame game = availableArenas.getFirst();
         Optional<OneShootPlayer> gamePlayer = game.getPlayer(player.getUuid());
         if (gamePlayer.isPresent()) {
            throw new AlreadyInGameException();
         } else {
            player.getBukkitPlayer().ifPresent(this::prepareLobbyInventory);
            game.getPlayers().add(player);
            if (game.getPlayers().size() == this.minPlayers()) {
               game.setCountdown(game.getArena().getArenaSettings().countdownSeconds);
               game.setStatus(OneShootGameStatus.STARTING);
            }

            game.setCacheSpawn(player);
            game.teleportToSpawn(player);
            List<IPlaceholder> placeholders = List.of(
               new Placeholder("current_players", game.getPlayers().size()),
               new Placeholder("max_players", this.minPlayers()),
               new Placeholder("player", player.getName())
            );
            String message = StringUtils.processMulti(this.messages.gameMessages.playerJoinedGame, placeholders);
            game.sendMessage(message);
         }
      }
   }

   @Override
   public void joinGame(OneShootPlayer player, OneShootGame game) throws AlreadyInGameException {
      Optional<OneShootPlayer> gamePlayer = game.getPlayer(player.getUuid());
      if (gamePlayer.isPresent()) {
         throw new AlreadyInGameException();
      } else {
         player.getBukkitPlayer().ifPresent(this::prepareLobbyInventory);
         game.getPlayers().add(player);
         if (game.getPlayers().size() == this.minPlayers()) {
            game.setCountdown(game.getArena().getArenaSettings().countdownSeconds);
            game.setStatus(OneShootGameStatus.STARTING);
         }

         game.setCacheSpawn(player);
         game.teleportToSpawn(player);
         List<IPlaceholder> placeholders = List.of(
            new Placeholder("current_players", game.getPlayers().size()),
            new Placeholder("max_players", this.minPlayers()),
            new Placeholder("player", player.getName())
         );
         String message = StringUtils.processMulti(this.messages.gameMessages.playerJoinedGame, placeholders);
         game.sendMessage(message);
      }
   }

   @Override
   public void leaveGame(UUID player, OneShootGame game) {
      Location mainLobby = this.config.getMainSpawn().getLocation();
      Optional<OneShootPlayer> player1 = game.getPlayer(player);
      player1.flatMap(OneShootPlayer::getBukkitPlayer).ifPresent(pl -> {
         pl.teleport(mainLobby);
         pl.getInventory().clear();
      });
      player1.ifPresent(p -> game.getPlayers().remove(p));
      game.getSpawnCache().remove(player);
      if (game.getStatus() == OneShootGameStatus.STARTING && game.getPlayers().size() < this.minPlayers()) {
         game.setStatus(OneShootGameStatus.WAITING);
      }

      List<IPlaceholder> placeholders = List.of(
         new Placeholder("current_players", game.getPlayers().size()),
         new Placeholder("max_players", this.minPlayers()),
         new Placeholder("player", player1.map(OneShootPlayer::getName).orElse(""))
      );
      String message;
      if (game.getStatus().equals(OneShootGameStatus.IN_GAME)) {
         message = StringUtils.processMulti(this.messages.gameMessages.playerLeftGameWhilePlaying, placeholders);
         if (game.getPlayers().size() <= 1) {
            OneShootPlayer winner = game.getPlayers().stream().findFirst().orElse(null);
            this.finishGame(game, winner, true);
         }
      } else {
         message = StringUtils.processMulti(this.messages.gameMessages.playerLeftGame, placeholders);
      }

      game.sendMessage(message);
   }

   @Override
   public void finishGame(OneShootGame game, OneShootPlayer player, boolean waitBeforeTeleport) {
      List<Player> players = game.getPlayers().stream().map(OneShootPlayer::getBukkitPlayer).filter(Optional::isPresent).map(Optional::get).toList();
      if (game.getStatus() != OneShootGameStatus.FINISHED) {
         if (player != null) {
            game.sendMessage(StringUtils.processMulti(this.messages.gameMessages.gameFinished, new Placeholder("player", player.getName()).alone()));
            player.setWins(player.getWins() + 1);
            if (this.config.gameEndConfig.fireworksEnabled) {
               player.getBukkitPlayer().ifPresent(px -> Util.fireworks(px, 5, this.config.gameEndConfig.fireWorksPer5Ticks));
            }

            if (this.config.gameEndConfig.particlesEnabled) {
               List<String> types = new ArrayList<>();
               types.add("FIREWORKS_SPARK");
               player.getBukkitPlayer().ifPresent(px -> Util.surroundParticles(px, 1, types, 8, 0.0, TheAssistantPlugin.getAPI().getNms()));
            }

            game.setPlayers(List.of(player));
         } else {
            game.setPlayers(new ArrayList<>());
         }

         game.getPlayers().forEach(px -> px.getBukkitPlayer().ifPresent(bp -> {
            bp.getInventory().clear();
            bp.setGlowing(false);
         }));
         game.setStatus(OneShootGameStatus.FINISHED);
         game.setCountdown(game.getArena().getArenaSettings().countdownSeconds);
         game.getSpawnCache().clear();

         for (Player p : players) {
            if (player == null || p.getUniqueId() != player.getUuid()) {
               p.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard());
            }
         }
      }

      if (waitBeforeTeleport) {
         Bukkit.getScheduler().runTaskLater(OneShoot.getInstance(), () -> this.resetGameStatus(players, game, true, player), 200L);
      } else {
         this.resetGameStatus(players, game, false, player);
      }
   }

   private void resetGameStatus(List<Player> players, OneShootGame game, boolean setWaiting, OneShootPlayer player) {
      if (setWaiting) {
         player.getBukkitPlayer().ifPresent(bp -> bp.setScoreboard(Bukkit.getScoreboardManager().getNewScoreboard()));
         game.setPlayers(new ArrayList<>());
         game.setOneShootGameTurn(OneShootGameTurn.builder().build());
         game.setStatus(OneShootGameStatus.WAITING);
      }

      players.forEach(pl -> {
         if (pl != null) {
            pl.teleport(this.config.getMainSpawn().getLocation());
         }
      });
   }

   @Override
   public void addGame(OneShootGame game) {
      this.games.put(game.getGameId(), game);
   }

   @Override
   public void removeGame(OneShootGame game) {
      this.games.remove(game.getGameId());
   }

   @Override
   public Optional<OneShootGame> getGameByPlayer(UUID uuid) {
      return this.games.values().stream().filter(game -> game.getPlayers().stream().anyMatch(unoPlayer -> unoPlayer.getUuid().equals(uuid))).findFirst();
   }

   @Override
   public void enableGame(OneShootArena arena) {
      UUID uuid = UUID.randomUUID();
      OneShootGame game = OneShootGame.builder()
         .gameId(uuid)
         .status(OneShootGameStatus.WAITING)
         .arena(arena)
         .countdown(arena.getArenaSettings().getCountdownSeconds())
         .oneShootGameTurn(OneShootGameTurn.builder().build())
         .players(new ArrayList<>())
         .build();
      this.games.put(uuid, game);
   }

   @Override
   public List<OneShootGame> getGames() {
      return ImmutableList.copyOf(this.games.values());
   }

   @Override
   public List<OneShootGame> getAvailableGames() {
      for (OneShootArena arena : this.arenaService.getArenas()) {
         Optional<OneShootGame> existentArenaGame = this.games.values().stream().filter(game -> game.getArena().getId().equals(arena.getId())).findFirst();
         if (existentArenaGame.isEmpty() && arena.getArenaSettings().isEnabled()) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.color("&aEnabled arena!"));
            this.enableGame(arena);
         }
      }

      return this.games
         .values()
         .stream()
         .filter(arenax -> arenax.getStatus().equals(OneShootGameStatus.WAITING) || arenax.getStatus().equals(OneShootGameStatus.STARTING))
         .filter(arenax -> arenax.getPlayers().size() < this.minPlayers())
         .collect(Collectors.toList());
   }
}
