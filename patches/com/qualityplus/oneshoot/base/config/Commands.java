package com.qualityplus.oneshoot.base.config;

import com.qualityplus.assistant.api.commands.details.CommandDetails;
import com.qualityplus.assistant.lib.eu.okaeri.configs.OkaeriConfig;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Header;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Headers;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.NameModifier;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.NameStrategy;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Names;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Configuration;
import java.time.Duration;
import java.util.Collections;

@Configuration(
   path = "commands.yml"
)
@Headers({@Header({"================================"}), @Header({"       Commands      "}), @Header({"================================"})})
@Names(
   strategy = NameStrategy.HYPHEN_CASE,
   modifier = NameModifier.TO_LOWER_CASE
)
public final class Commands extends OkaeriConfig {
   public CommandDetails reloadCommand = new CommandDetails(
      Collections.singletonList("reload"), "Reload files", "/OneShot reload", "oneshoot.reload", true, Duration.ZERO.getSeconds(), true, "oneshot"
   );
   public CommandDetails arenaCreateCommand = new CommandDetails(
      Collections.singletonList("arenacreate"),
      "Creates a new arena",
      "/OneShot arenacreate <arena>",
      "oneshoot.arena.arenacreate",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaDeleteCommand = new CommandDetails(
      Collections.singletonList("arenadelete"),
      "Deletes an arena",
      "/OneShot arenadelete <arena>",
      "oneshoot.arena.arenadelete",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaAddPlayerSpawn = new CommandDetails(
      Collections.singletonList("arenaddplayerspawn"),
      "Add spawn for arena ",
      "/OneShot arenaddplayerspawn <arena>",
      "oneshoot.arena.arenaddplayerspawn",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaSetWaitSpawn = new CommandDetails(
      Collections.singletonList("arenasetwaitspawn"),
      "Set wait spawn for arena ",
      "/OneShot arenasetwaitspawn <arena>",
      "oneshoot.arena.arenasetwaitspawn",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaListCommand = new CommandDetails(
      Collections.singletonList("arenalist"),
      "List all arenas",
      "/OneShot arenalist",
      "oneshoot.arena.arenalist",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaSetDisplayName = new CommandDetails(
      Collections.singletonList("arenasetdisplayname"),
      "Set arena displayname",
      "/OneShot arenasetdisplayname <arena> <displayname>",
      "oneshoot.arena.arenasetdisplayname",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaEnableCommand = new CommandDetails(
      Collections.singletonList("arenaenable"),
      "Enable arena",
      "/OneShot arenaenable <arena>",
      "oneshoot.arena.arenaenable",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaDisableCommand = new CommandDetails(
      Collections.singletonList("arenadisable"),
      "Disable arena",
      "/OneShot arenadisable <arena>",
      "oneshoot.arena.arenadisable",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaSetCenterCommand = new CommandDetails(
      Collections.singletonList("arenasetcenter"),
      "Set center for arena ",
      "/OneShot arenasetcenter <arena>",
      "oneshoot.arena.arenasetcenter",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails setMainSpawnCommand = new CommandDetails(
      Collections.singletonList("setmainspawn"),
      "Set main spawn ",
      "/OneShot setmainspawn",
      "oneshoot.arena.arenasetcenter",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaSetMinPlayers = new CommandDetails(
      Collections.singletonList("arenasetminplayers"),
      "Set arena min players",
      "/OneShot arenasetminplayers <arena> <minplayers>",
      "oneshoot.arena.arenasetminplayers",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails arenaSetMaxPlayers = new CommandDetails(
      Collections.singletonList("arenasetmaxplayers"),
      "Set arena max players",
      "/OneShot arenasetmaxplayers <arena> <maxplayers>",
      "oneshoot.arena.arenasetmaxplayers",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails joinCommand = new CommandDetails(
      Collections.singletonList("join"), "Join arena", "/OneShot join", "oneshoot.join", true, Duration.ZERO.getSeconds(), true, "oneshot"
   );
   public CommandDetails leaveCommand = new CommandDetails(
      Collections.singletonList("leave"), "Leave arena", "/OneShot leave", "oneshoot.leave", true, Duration.ZERO.getSeconds(), true, "oneshot"
   );
   public CommandDetails inviteCommand = new CommandDetails(
      Collections.singletonList("invite"),
      "Invite player to game",
      "/OneShot invite <player>",
      "oneshoot.invite",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails acceptCommand = new CommandDetails(
      Collections.singletonList("accept"),
      "Accept player invite",
      "/OneShot accept <player>",
      "oneshoot.accept",
      true,
      Duration.ZERO.getSeconds(),
      true,
      "oneshot"
   );
   public CommandDetails testGiveCommand = new CommandDetails(
      Collections.singletonList("test"), "Set main spawn ", "/OneShot test", "oneshoot.arena.test", true, Duration.ZERO.getSeconds(), true, "oneshot"
   );
   public CommandDetails helpCommand = new CommandDetails(
      Collections.singletonList("help"), "Show all commands", "/OneShot help", "oneshoot.help", true, Duration.ZERO.getSeconds(), true, "oneshot"
   );

   public CommandDetails getReloadCommand() {
      return this.reloadCommand;
   }

   public CommandDetails getArenaCreateCommand() {
      return this.arenaCreateCommand;
   }

   public CommandDetails getArenaDeleteCommand() {
      return this.arenaDeleteCommand;
   }

   public CommandDetails getArenaAddPlayerSpawn() {
      return this.arenaAddPlayerSpawn;
   }

   public CommandDetails getArenaSetWaitSpawn() {
      return this.arenaSetWaitSpawn;
   }

   public CommandDetails getArenaListCommand() {
      return this.arenaListCommand;
   }

   public CommandDetails getArenaSetDisplayName() {
      return this.arenaSetDisplayName;
   }

   public CommandDetails getArenaEnableCommand() {
      return this.arenaEnableCommand;
   }

   public CommandDetails getArenaDisableCommand() {
      return this.arenaDisableCommand;
   }

   public CommandDetails getArenaSetCenterCommand() {
      return this.arenaSetCenterCommand;
   }

   public CommandDetails getSetMainSpawnCommand() {
      return this.setMainSpawnCommand;
   }

   public CommandDetails getArenaSetMinPlayers() {
      return this.arenaSetMinPlayers;
   }

   public CommandDetails getArenaSetMaxPlayers() {
      return this.arenaSetMaxPlayers;
   }

   public CommandDetails getJoinCommand() {
      return this.joinCommand;
   }

   public CommandDetails getLeaveCommand() {
      return this.leaveCommand;
   }

   public CommandDetails getInviteCommand() {
      return this.inviteCommand;
   }

   public CommandDetails getAcceptCommand() {
      return this.acceptCommand;
   }

   public CommandDetails getTestGiveCommand() {
      return this.testGiveCommand;
   }

   public CommandDetails getHelpCommand() {
      return this.helpCommand;
   }

   public void setReloadCommand(CommandDetails reloadCommand) {
      this.reloadCommand = reloadCommand;
   }

   public void setArenaCreateCommand(CommandDetails arenaCreateCommand) {
      this.arenaCreateCommand = arenaCreateCommand;
   }

   public void setArenaDeleteCommand(CommandDetails arenaDeleteCommand) {
      this.arenaDeleteCommand = arenaDeleteCommand;
   }

   public void setArenaAddPlayerSpawn(CommandDetails arenaAddPlayerSpawn) {
      this.arenaAddPlayerSpawn = arenaAddPlayerSpawn;
   }

   public void setArenaSetWaitSpawn(CommandDetails arenaSetWaitSpawn) {
      this.arenaSetWaitSpawn = arenaSetWaitSpawn;
   }

   public void setArenaListCommand(CommandDetails arenaListCommand) {
      this.arenaListCommand = arenaListCommand;
   }

   public void setArenaSetDisplayName(CommandDetails arenaSetDisplayName) {
      this.arenaSetDisplayName = arenaSetDisplayName;
   }

   public void setArenaEnableCommand(CommandDetails arenaEnableCommand) {
      this.arenaEnableCommand = arenaEnableCommand;
   }

   public void setArenaDisableCommand(CommandDetails arenaDisableCommand) {
      this.arenaDisableCommand = arenaDisableCommand;
   }

   public void setArenaSetCenterCommand(CommandDetails arenaSetCenterCommand) {
      this.arenaSetCenterCommand = arenaSetCenterCommand;
   }

   public void setSetMainSpawnCommand(CommandDetails setMainSpawnCommand) {
      this.setMainSpawnCommand = setMainSpawnCommand;
   }

   public void setArenaSetMinPlayers(CommandDetails arenaSetMinPlayers) {
      this.arenaSetMinPlayers = arenaSetMinPlayers;
   }

   public void setArenaSetMaxPlayers(CommandDetails arenaSetMaxPlayers) {
      this.arenaSetMaxPlayers = arenaSetMaxPlayers;
   }

   public void setJoinCommand(CommandDetails joinCommand) {
      this.joinCommand = joinCommand;
   }

   public void setLeaveCommand(CommandDetails leaveCommand) {
      this.leaveCommand = leaveCommand;
   }

   public void setInviteCommand(CommandDetails inviteCommand) {
      this.inviteCommand = inviteCommand;
   }

   public void setAcceptCommand(CommandDetails acceptCommand) {
      this.acceptCommand = acceptCommand;
   }

   public void setTestGiveCommand(CommandDetails testGiveCommand) {
      this.testGiveCommand = testGiveCommand;
   }

   public void setHelpCommand(CommandDetails helpCommand) {
      this.helpCommand = helpCommand;
   }
}
