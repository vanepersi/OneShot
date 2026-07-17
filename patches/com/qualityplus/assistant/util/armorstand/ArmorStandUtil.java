package com.qualityplus.assistant.util.armorstand;

import com.qualityplus.assistant.TheAssistantPlugin;
import com.qualityplus.assistant.lib.org.jetbrains.annotations.NotNull;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.AxisAngle4f;
import org.joml.Vector3f;

/**
 * Display-entity helpers that replace the old ArmorStand-based utilities.
 * Methods named for ArmorStands are kept for binary compatibility with
 * QualityPlus plugins; they now spawn/manipulate ItemDisplay entities.
 */
public final class ArmorStandUtil {

    public static void rotate(ItemDisplay display, Location newLocation) {
        if (display == null || display.isDead()) {
            return;
        }
        Location location = display.getLocation().clone();
        Location finalLocation = location.clone().setDirection(newLocation.clone().subtract(location).toVector());
        Bukkit.getScheduler().runTask(TheAssistantPlugin.getAPI().getPlugin(), () -> display.teleport(finalLocation));
    }

    /** @deprecated use {@link #rotate(ItemDisplay, Location)} */
    @Deprecated
    public static void rotate(org.bukkit.entity.ArmorStand armorStand, Location newLocation) {
        // no-op legacy path — ArmorStands are no longer created by this util
    }

    public static boolean entityIsValid(ItemDisplay display) {
        return display != null && !display.isDead();
    }

    /** @deprecated use {@link #entityIsValid(ItemDisplay)} */
    @Deprecated
    public static boolean entityIsValid(org.bukkit.entity.ArmorStand armorStand) {
        return armorStand != null && !armorStand.isDead();
    }

    /**
     * Creates a default ItemDisplay at the given location (replaces ArmorStand.createDefault).
     */
    public static ItemDisplay createDefault(Location location) {
        return Optional.ofNullable(location.getWorld())
                .map(world -> getItemDisplay(world, location))
                .orElse(null);
    }

    private static ItemDisplay getItemDisplay(@NotNull World world, Location location) {
        return world.spawn(location, ItemDisplay.class, display -> {
            display.setBillboard(Display.Billboard.FIXED);
            display.setPersistent(false);
            display.setGravity(false);
            display.setInvulnerable(true);
            display.setTransformation(new Transformation(
                    new Vector3f(0f, 0f, 0f),
                    new AxisAngle4f(0f, 0f, 0f, 1f),
                    new Vector3f(1f, 1f, 1f),
                    new AxisAngle4f(0f, 0f, 0f, 1f)
            ));
        });
    }

    /** Convenience: spawn a single-line TextDisplay hologram line. */
    public static TextDisplay createText(Location location, String text) {
        return Optional.ofNullable(location.getWorld())
                .map(world -> world.spawn(location, TextDisplay.class, display -> {
                    display.setBillboard(Display.Billboard.CENTER);
                    display.setSeeThrough(true);
                    display.setShadowed(false);
                    display.setDefaultBackground(false);
                    display.setBackgroundColor(org.bukkit.Color.fromARGB(0, 0, 0, 0));
                    display.setText(text);
                    display.setPersistent(false);
                    display.setGravity(false);
                    display.setInvulnerable(true);
                }))
                .orElse(null);
    }

    private ArmorStandUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
