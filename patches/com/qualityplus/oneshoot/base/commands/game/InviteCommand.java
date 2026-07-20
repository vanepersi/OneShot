package com.qualityplus.oneshoot.base.commands.game;

import com.qualityplus.assistant.TheAssistantPlugin;
import com.qualityplus.assistant.api.commands.command.AssistantCommand;
import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.bukkit.annotation.Delayed;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Component;
import com.qualityplus.assistant.util.StringUtils;
import com.qualityplus.assistant.util.placeholder.Placeholder;
import com.qualityplus.assistant.util.time.HumanTime;
import com.qualityplus.assistant.util.time.Markable;
import com.qualityplus.oneshoot.api.box.Box;
import com.qualityplus.oneshoot.api.domain.OneShootInvite;
import com.qualityplus.oneshoot.util.OneShootInvites;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Invite flow using Paper Adventure click components (Bungee chat API is unreliable on 26.x).
 */
@Component
public final class InviteCommand extends AssistantCommand {
    @Inject
    private Box box;

    private static net.kyori.adventure.text.Component legacy(String colored) {
        return LegacyComponentSerializer.legacySection().deserialize(StringUtils.color(colored));
    }

    public boolean execute(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(StringUtils.color(this.box.getFiles().getMessages().pluginMessages.useSyntax.replace("%usage%", this.syntax)));
            return false;
        }

        Player playerSender = (Player) sender;
        String receiverName = args[1];
        Player receiver = Bukkit.getPlayerExact(receiverName);
        if (receiver == null) {
            receiver = Bukkit.getPlayer(receiverName);
        }
        if (receiver == null || !receiver.isOnline()) {
            playerSender.sendMessage(StringUtils.color(this.box.getFiles().getMessages().pluginMessages.invalidPlayer));
            return true;
        }
        if (receiver.getUniqueId().equals(playerSender.getUniqueId())) {
            playerSender.sendMessage(StringUtils.color("&cYou cannot invite yourself!"));
            return true;
        }

        Optional<OneShootInvite> existing = OneShootInvites.getInvite(playerSender.getUniqueId(), receiver.getUniqueId());
        if (existing.isPresent()) {
            playerSender.sendMessage(
                    StringUtils.processMulti(
                            this.box.getFiles().getMessages().gameMessages.inviteAlreadySent,
                            new Placeholder("player", receiver.getName()).alone()
                    )
            );
            return true;
        }

        HumanTime time = this.box.getFiles().getConfig().getInviteDuration();
        OneShootInvite oneShootInvite = OneShootInvite.builder()
                .sender(playerSender.getUniqueId())
                .receiver(receiver.getUniqueId())
                .markable(new Markable(time.getEffectiveTime(), System.currentTimeMillis()))
                .build();
        OneShootInvites.addInvite(oneShootInvite);

        playerSender.sendMessage(
                StringUtils.processMulti(
                        this.box.getFiles().getMessages().gameMessages.invitationSent,
                        new Placeholder("player", receiver.getName()).alone()
                )
        );

        String invitedToFight = this.box.getFiles().getMessages().gameMessages.receivedInviteToFight
                .replace("%player%", playerSender.getName());
        String hover = "&eClick to accept fight!";
        try {
            if (this.box.getFiles().getMessages().gameMessages.acceptMessage != null
                    && this.box.getFiles().getMessages().gameMessages.acceptMessage.getAboveMessage() != null) {
                hover = this.box.getFiles().getMessages().gameMessages.acceptMessage.getAboveMessage();
            }
        } catch (Exception ignored) {
            // keep default hover
        }

        receiver.sendMessage(legacy(invitedToFight));
        receiver.sendMessage(
                net.kyori.adventure.text.Component.text("   [ACCEPT]")
                        .color(NamedTextColor.GREEN)
                        .decorate(TextDecoration.BOLD)
                        .clickEvent(ClickEvent.runCommand("/oneshot accept " + playerSender.getName()))
                        .hoverEvent(HoverEvent.showText(legacy(hover)))
        );
        receiver.sendMessage(legacy("&7Or type: &f/oneshot accept " + playerSender.getName()));
        return true;
    }

    public List<String> onTabComplete(CommandSender commandSender, Command command, String label, String[] args) {
        if (args.length == 2) {
            String prefix = args[1] == null ? "" : args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(prefix))
                    .filter(name -> !(commandSender instanceof Player) || !name.equalsIgnoreCase(commandSender.getName()))
                    .collect(Collectors.toList());
        }
        return List.of();
    }

    @Delayed(time = 20)
    public void register(@Inject Box box) {
        TheAssistantPlugin.getAPI()
                .getCommandProvider()
                .registerCommand(this, e -> ((AssistantCommand) e.getCommand()).setDetails(box.getFiles().getCommands().inviteCommand));
    }
}
