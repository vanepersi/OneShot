package com.qualityplus.oneshoot.base.config;

import com.qualityplus.assistant.api.config.ConfigDatabase;
import com.qualityplus.assistant.inventory.Item;
import com.qualityplus.assistant.lib.com.cryptomorin.xseries.XMaterial;
import com.qualityplus.assistant.lib.eu.okaeri.configs.OkaeriConfig;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Comment;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Comments;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.CustomKey;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Header;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Headers;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.NameModifier;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.NameStrategy;
import com.qualityplus.assistant.lib.eu.okaeri.configs.annotation.Names;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Configuration;
import com.qualityplus.assistant.util.location.ALocation;
import com.qualityplus.assistant.util.time.HumanTime;
import com.qualityplus.assistant.util.time.HumanTime.TimeType;
import java.util.List;

@Configuration
@Headers({@Header({"  _   _  _   _   ____  "}), @Header({" | | | || \\ | | |  _ \\ "}), @Header({" | | | ||  \\| | | | | |"}), @Header({" | |_| || |\\  | | |_| |\n"}), @Header({"  \\___/ |_| \\_| |____/ "}), @Header({" "}), @Header({"      /\\_/\\  "}), @Header({"     ( o.o ) "}), @Header({"      > ^ < "}), @Header({" "}), @Header({" UNO Game Configuration"})})
@Names(
   strategy = NameStrategy.HYPHEN_CASE,
   modifier = NameModifier.TO_LOWER_CASE
)
public final class Config extends OkaeriConfig {
   @Comments({@Comment({"Database Configuration"}), @Comment({"Allowed Database Types"}), @Comment({"- H2"}), @Comment({"- FLAT"}), @Comment({"- MYSQL"}), @Comment({"- REDIS"})})
   public ConfigDatabase databaseCredentials = new ConfigDatabase();
   public String messageSystem = "iToast";
   public Config.LobbyItemConfig lobbyItemConfig = new Config.LobbyItemConfig();
   public ALocation mainSpawn = new ALocation(0.0, 0.0, 0.0, 0.0F, 0.0F, "world");
   public HumanTime inviteDuration = new HumanTime(30, TimeType.SECONDS);
   @Comment({"Minimum players required to start a match (default 2)"})
   public int minPlayers = 2;
   public Config.GameItemConfig gameItemConfig = new Config.GameItemConfig();
   public Config.GameEndConfig gameEndConfig = new Config.GameEndConfig();

   public ConfigDatabase getDatabaseCredentials() {
      return this.databaseCredentials;
   }

   public String getMessageSystem() {
      return this.messageSystem;
   }

   public Config.LobbyItemConfig getLobbyItemConfig() {
      return this.lobbyItemConfig;
   }

   public ALocation getMainSpawn() {
      return this.mainSpawn;
   }

   public HumanTime getInviteDuration() {
      return this.inviteDuration;
   }

   public int getMinPlayers() {
      return this.minPlayers > 0 ? this.minPlayers : 2;
   }

   public Config.GameItemConfig getGameItemConfig() {
      return this.gameItemConfig;
   }

   public Config.GameEndConfig getGameEndConfig() {
      return this.gameEndConfig;
   }

   public void setDatabaseCredentials(ConfigDatabase databaseCredentials) {
      this.databaseCredentials = databaseCredentials;
   }

   public void setMessageSystem(String messageSystem) {
      this.messageSystem = messageSystem;
   }

   public void setLobbyItemConfig(Config.LobbyItemConfig lobbyItemConfig) {
      this.lobbyItemConfig = lobbyItemConfig;
   }

   public void setMainSpawn(ALocation mainSpawn) {
      this.mainSpawn = mainSpawn;
   }

   public void setInviteDuration(HumanTime inviteDuration) {
      this.inviteDuration = inviteDuration;
   }

   public void setMinPlayers(int minPlayers) {
      this.minPlayers = minPlayers;
   }

   public void setGameItemConfig(Config.GameItemConfig gameItemConfig) {
      this.gameItemConfig = gameItemConfig;
   }

   public void setGameEndConfig(Config.GameEndConfig gameEndConfig) {
      this.gameEndConfig = gameEndConfig;
   }

   public class GameEndConfig extends OkaeriConfig {
      @CustomKey("fireWorksPer5Ticks")
      public int fireWorksPer5Ticks = 2;
      @CustomKey("fireworksEnabled")
      public boolean fireworksEnabled = true;
      @CustomKey("particlesEnabled")
      public boolean particlesEnabled = true;
   }

   public static class GameItemConfig extends OkaeriConfig {
      public Item bowItem = Item.builder()
         .amount(1)
         .displayName("&9&lOne Shoot Bow")
         .lore(List.of("&9&lEPIC"))
         .enabled(true)
         .enchanted(true)
         .material(XMaterial.BOW)
         .build();
      public Item arrowItem = Item.builder()
         .amount(1)
         .displayName("&9&lOne Shoot Arrow")
         .lore(List.of("&9&lEPIC"))
         .enabled(true)
         .enchanted(true)
         .material(XMaterial.ARROW)
         .build();
   }

   public static class LobbyItemConfig extends OkaeriConfig {
      public Config.LobbyItemConfig.LobbyItem leaveItem = new Config.LobbyItemConfig.LobbyItem() {
         {
            this.material = XMaterial.RED_BED;
            this.amount = 1;
            this.displayName = "&cLeave Game";
            this.slot = 8;
            this.enabled = true;
         }
      };

      public static class LobbyItem extends OkaeriConfig {
         public XMaterial material;
         public int customModelData = 0;
         public int amount;
         public String displayName;
         public int slot;
         public boolean enabled;

         public XMaterial getMaterial() {
            return this.material;
         }

         public int getCustomModelData() {
            return this.customModelData;
         }

         public int getAmount() {
            return this.amount;
         }

         public String getDisplayName() {
            return this.displayName;
         }

         public int getSlot() {
            return this.slot;
         }

         public boolean isEnabled() {
            return this.enabled;
         }
      }
   }
}
