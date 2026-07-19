package com.qualityplus.oneshoot.base.commands.provider;

import com.qualityplus.assistant.api.commands.LabelProvider;
import com.qualityplus.assistant.lib.eu.okaeri.injector.annotation.Inject;
import com.qualityplus.assistant.lib.eu.okaeri.platform.bukkit.annotation.Delayed;
import com.qualityplus.assistant.lib.eu.okaeri.platform.core.annotation.Component;
import com.qualityplus.oneshoot.api.box.Box;
import com.qualityplus.oneshoot.base.config.Messages;

/**
 * Registers TheAssistant LabelProvider as {@code oneshot} so it matches
 * plugin.yml and commands.yml (stock QualityPlus used {@code oneshoot}).
 */
@Component
public final class OneShootCommandProvider {

    public OneShootCommandProvider() {
    }

    @Delayed(time = 1)
    private void configureProvider(@Inject Box box) {
        Messages.PluginMessages pluginMessages = box.getFiles().getMessages().pluginMessages;
        LabelProvider.builder()
                .id("oneshot")
                .label("oneshot")
                .plugin(box.getPlugin())
                .useHelpMessage(pluginMessages.useHelp)
                .unknownCommandMessage(pluginMessages.unknownCommand)
                .onlyForPlayersMessage(pluginMessages.mustBeAPlayer)
                .noPermissionMessage(pluginMessages.noPermission)
                .build()
                .register();
    }
}
