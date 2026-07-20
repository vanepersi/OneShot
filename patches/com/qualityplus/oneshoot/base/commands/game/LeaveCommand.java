package com.qualityplus.oneshoot.base.commands.game;

import com.qualityplus.assistant.TheAssistantPlugin;
import com.qualityplus.assistant.api.commands.command.AssistantCommand;
import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.bukkit.annotation.Delayed;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Component;
import com.qualityplus.assistant.util.StringUtils;
import com.qualityplus.oneshoot.api.box.Box;
import com.qualityplus.oneshoot.api.domain.OneShootGame;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@Component
public final class LeaveCommand extends AssistantCommand {
   @Inject
   private Box box;

   public boolean execute(CommandSender sender, String[] args) {
      if (args.length == 1) {
         Player playerSender = (Player)sender;
         Optional<OneShootGame> game = this.box.getOneShootService().getGameByPlayer(playerSender.getUniqueId());
         if (game.isEmpty()) {
            playerSender.sendMessage(StringUtils.color(this.box.getFiles().getMessages().gameMessages.youNeedToBeInGame));
            return false;
         } else {
            this.box.getOneShootService().leaveGame(playerSender.getUniqueId(), game.get());
            // Always clear/teleport locally in case leaveGame missed due to stale player data
            playerSender.getInventory().clear();
            try {
               playerSender.teleport(this.box.getFiles().getConfig().getMainSpawn().getLocation());
            } catch (Exception ignored) {
            }
            playerSender.sendMessage(StringUtils.color(this.box.getFiles().getMessages().gameMessages.successfullyLeftGame));
            return true;
         }
      } else {
         sender.sendMessage(StringUtils.color(this.box.getFiles().getMessages().pluginMessages.useSyntax.replace("%usage%", this.syntax)));
         return false;
      }
   }

   public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
      return Collections.emptyList();
   }

   @Delayed(
      time = 20
   )
   public void register(@Inject Box box) {
      TheAssistantPlugin.getAPI()
         .getCommandProvider()
         .registerCommand(this, e -> ((AssistantCommand)e.getCommand()).setDetails(box.getFiles().getCommands().leaveCommand));
   }
}
