package com.qualityplus.assistant.hologram;

import com.google.common.collect.Lists;
import com.qualityplus.assistant.util.StringUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Vector;

/**
 * Hologram implementation using TextDisplay entities (Paper 1.19.4+ / 26.x)
 * instead of ArmorStands.
 */
public final class TheHologram {
    private List<TextDisplay> displays;
    private Location location;
    private List<String> txt;

    private TheHologram(List<String> txt, Location location) {
        this.txt = txt;
        this.location = location;
        this.displays = this.createDisplays();
    }

    public static TheHologram create(List<String> txt, Location location) {
        return new TheHologram(txt, location);
    }

    public void move(Location location) {
        this.remove();
        this.location = location;
        this.displays = this.createDisplays();
    }

    public TheHologram rename(List<String> txt) {
        this.remove();
        this.txt = txt;
        this.displays = this.createDisplays();
        return this;
    }

    public void remove() {
        this.displays.stream().filter(Objects::nonNull).forEach(Entity::remove);
        this.displays.clear();
    }

    private List<TextDisplay> createDisplays() {
        List<TextDisplay> displays = new ArrayList<>();
        double lineHeight = 0.25;
        double initial = this.txt.size() * lineHeight;
        Location initialLocation = this.location.clone().add(0.0, initial, 0.0);
        int size = 0;

        for (String line : Lists.reverse(this.txt)) {
            double newY = initial - lineHeight * size;
            Location spawnAt = initialLocation.clone().subtract(new Vector(0.0, newY, 0.0));
            TextDisplay display = this.location.getWorld().spawn(spawnAt, TextDisplay.class, textDisplay -> {
                textDisplay.setBillboard(Display.Billboard.CENTER);
                textDisplay.setSeeThrough(true);
                textDisplay.setShadowed(false);
                textDisplay.setDefaultBackground(false);
                textDisplay.setBackgroundColor(Color.fromARGB(0, 0, 0, 0));
                textDisplay.setAlignment(TextDisplay.TextAlignment.CENTER);
                textDisplay.setLineWidth(200);
                textDisplay.setText(StringUtils.color(line));
                textDisplay.setPersistent(false);
                textDisplay.setGravity(false);
                textDisplay.setInvulnerable(true);
            });
            displays.add(display);
            size++;
        }

        return displays;
    }

    /** @deprecated use {@link #getTextDisplays()} — kept for binary compatibility with callers */
    @Deprecated
    public List getArmorStands() {
        return this.displays;
    }

    public List<TextDisplay> getTextDisplays() {
        return this.displays;
    }

    public List<String> getTxt() {
        return this.txt;
    }
}
