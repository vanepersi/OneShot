package com.qualityplus.oneshoot.base.handler;

import com.qualityplus.assistant.api.util.IPlaceholder;
import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.bukkit.annotation.Scheduled;
import com.qualityplus.assistant.util.StringUtils;
import com.qualityplus.assistant.util.placeholder.Placeholder;
import com.qualityplus.assistant.util.time.Markable;
import com.qualityplus.assistant.util.time.TimeUtils;
import com.qualityplus.oneshoot.OneShoot;
import com.qualityplus.oneshoot.api.domain.OneShootGame;
import com.qualityplus.oneshoot.api.domain.OneShootGameStatus;
import com.qualityplus.oneshoot.api.domain.OneShootGameTurn;
import com.qualityplus.oneshoot.api.service.OneShootService;
import com.qualityplus.oneshoot.base.config.Config;
import com.qualityplus.oneshoot.base.config.Messages;
import com.qualityplus.oneshoot.persistance.data.OneShootPlayer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Map.Entry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

@Scheduled(
   rate = 20,
   delay = 0,
   async = false
)
public final class ArenaHandler implements Runnable {
   @Inject
   private OneShootService service;
   @Inject
   private Messages messages;
   @Inject
   private Config config;

   @Override
   public void run() {
      for (OneShootGame game : this.service.getGames()) {
         switch (game.getStatus()) {
            case IN_GAME:
               OneShootGameTurn gameTurn = game.getOneShootGameTurn();
               this.sendScoreBoard(game);
               if (gameTurn.getTurnTime() == null) {
                  game.handleNextTurn();
               } else if (gameTurn.getTurnTime().getRemainingTime().isZero()) {
                  game.handleNextTurn();
               }
               break;
            case WAITING:
               break;
            case FINISHED:
               this.sendScoreBoard(game);
               break;
            case STARTING:
               int countdown = game.getCountdown();
               if (countdown > 0) {
                  String message = StringUtils.processMulti(this.messages.gameMessages.gameStartingIn, new Placeholder("time", countdown).alone());
                  game.sendMessage(message);
                  game.setCountdown(countdown - 1);
               } else {
                  game.setStatus(OneShootGameStatus.IN_GAME);
                  // Both players get bow + arrow; turn system decides who may shoot
                  game.giveItemsToAllPlayers();
                  game.handleNextTurn();
                  game.sendMessage(StringUtils.color(this.messages.gameMessages.gameStarted));
                  game.setGameTime(new Markable(game.getArena().getArenaSettings().getGameTime().getEffectiveTime(), System.currentTimeMillis()));
               }
               break;
         }
      }
   }

   public void sendScoreBoard(OneShootGame game) {
      Bukkit.getScheduler()
         .runTask(
            OneShoot.getInstance(),
            () -> {
               Scoreboard scoreboard = game.getScoreboard() == null ? Bukkit.getScoreboardManager().getNewScoreboard() : game.getScoreboard();
               if (game.getScoreboard() == null) {
                  game.setScoreboard(scoreboard);
               }

               Messages.ScoreBoardMessages scoreBoardMessages = OneShoot.getApi().getBox().getFiles().getMessages().scoreBoardMessages;
               Objective objective = scoreboard.registerNewObjective(UUID.randomUUID().toString(), "dummy");
               objective.setDisplayName(StringUtils.color(scoreBoardMessages.scoreBoardTitle));
               objective.setDisplaySlot(DisplaySlot.SIDEBAR);
               String remainingGameTime = TimeUtils.getParsedTime(
                  game.getGameTime().getRemainingTime(),
                  scoreBoardMessages.timeFormat,
                  scoreBoardMessages.days,
                  scoreBoardMessages.hours,
                  scoreBoardMessages.minutes,
                  scoreBoardMessages.seconds,
                  scoreBoardMessages.noTimeFormat,
                  scoreBoardMessages.showNoTimeSymbol
               );
               String remainingTurnTime = Optional.ofNullable(game.getOneShootGameTurn().getTurnTime())
                  .map(
                     turnTime -> TimeUtils.getParsedTime(
                        game.getOneShootGameTurn().getTurnTime().getRemainingTime(),
                        scoreBoardMessages.timeFormat,
                        scoreBoardMessages.days,
                        scoreBoardMessages.hours,
                        scoreBoardMessages.minutes,
                        scoreBoardMessages.seconds,
                        scoreBoardMessages.noTimeFormat,
                        scoreBoardMessages.showNoTimeSymbol
                     )
                  )
                  .orElse("");
               String currentPlayer = Optional.ofNullable(game.getOneShootGameTurn().getPlayer())
                  .map(OneShootPlayer::getBukkitPlayer)
                  .filter(Optional::isPresent)
                  .map(Optional::get)
                  .<String>map(Player::getName)
                  .orElse("");
               List<IPlaceholder> placeholders = List.of(
                  new Placeholder("game_turn_player", currentPlayer),
                  new Placeholder("game_remaining_time", remainingGameTime),
                  new Placeholder("game_turn_remaining_time", remainingTurnTime)
               );

               for (Entry<Integer, String> scoreboardEntry : scoreBoardMessages.scoreboard.entrySet()) {
                  objective.getScore(StringUtils.processMulti(scoreboardEntry.getValue(), placeholders)).setScore(scoreboardEntry.getKey());
               }

               for (OneShootPlayer player : game.getPlayers()) {
                  player.getBukkitPlayer().ifPresent(p -> p.setScoreboard(scoreboard));
               }
            }
         );
   }
}
