package com.pietro.mcecapes;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;

/**
 * Loads capes (built-in ones + any .png dropped in .minecraft/config/mcecapes/)
 * and remembers which one is selected.
 *
 * To use the real MCE cape texture: put your own "mce.png" in
 * .minecraft/config/mcecapes/ - it overrides the built-in placeholder.
 */
public class CapeManager {

    /** Names of the capes bundled inside the jar (assets/mcecapes/textures/capes/NAME.png). */
    public static final String[] BUILTIN = {"mce", "aurora", "crimson", "midnight"};
    public static final String DEFAULT = "mce";

    private static final Map<String, ResourceLocation> CAPES = new LinkedHashMap<String, ResourceLocation>();
    private static File dir;
    private static File settingsFile;
    private static String active = DEFAULT; // null = no custom cape
    private static boolean loaded = false;

    public static void init(File configDir) {
        dir = new File(configDir, "mcecapes");
        if (!dir.exists() && !dir.mkdirs()) {
            MCECapes.LOG.warn("Could not create " + dir);
        }
        settingsFile = new File(dir, "settings.properties");
        loadSettings();
    }

    public static File getDir() {
        return dir;
    }

    // ------------------------------------------------------------------ settings

    private static void loadSettings() {
        if (settingsFile == null || !settingsFile.exists()) {
            return;
        }
        Properties p = new Properties();
        InputStream in = null;
        try {
            in = new FileInputStream(settingsFile);
            p.load(in);
            String a = p.getProperty("active", DEFAULT).trim().toLowerCase(Locale.ROOT);
            active = (a.isEmpty() || a.equals("off")) ? null : a;
        } catch (IOException e) {
            MCECapes.LOG.warn("Could not read settings", e);
        } finally {
            closeQuietly(in);
        }
    }

    private static void saveSettings() {
        if (settingsFile == null) {
            return;
        }
        Properties p = new Properties();
        p.setProperty("active", active == null ? "off" : active);
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(settingsFile);
            p.store(out, "MCE Capes - selected cape (client only)");
        } catch (IOException e) {
            MCECapes.LOG.warn("Could not save settings", e);
        } finally {
            closeQuietly(out);
        }
    }

    // ------------------------------------------------------------------ selection

    /** @return texture of the selected cape, or null when none is selected. */
    public static ResourceLocation getActiveTexture() {
        if (active == null) {
            return null;
        }
        ensureLoaded();
        return CAPES.get(active);
    }

    public static String getActiveName() {
        return active;
    }

    public static List<String> getNames() {
        ensureLoaded();
        return new ArrayList<String>(CAPES.keySet());
    }

    /** @return true if the cape exists and is now selected. */
    public static boolean select(String name) {
        ensureLoaded();
        name = name.toLowerCase(Locale.ROOT);
        if (!CAPES.containsKey(name)) {
            return false;
        }
        active = name;
        saveSettings();
        return true;
    }

    public static void disable() {
        active = null;
        saveSettings();
    }

    // ------------------------------------------------------------------ loading

    private static void ensureLoaded() {
        if (!loaded) {
            reload();
        }
    }

    /** (Re)loads every cape texture. Must run on the client (render) thread. */
    public static void reload() {
        Minecraft mc = Minecraft.getMinecraft();
        for (ResourceLocation old : CAPES.values()) {
            mc.getTextureManager().deleteTexture(old);
        }
        CAPES.clear();

        // 1) built-in capes (a file with the same name in the config folder wins)
        for (String name : BUILTIN) {
            BufferedImage img = null;
            File override = dir == null ? null : new File(dir, name + ".png");
            if (override != null && override.isFile()) {
                img = readFile(override);
            }
            if (img == null) {
                img = readBuiltin(name);
            }
            register(name, img);
        }

        // 2) any extra .png in the config folder
        if (dir != null) {
            File[] files = dir.listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File d, String n) {
                    return n.toLowerCase(Locale.ROOT).endsWith(".png");
                }
            });
            if (files != null) {
                Arrays.sort(files);
                for (File f : files) {
                    String n = f.getName();
                    n = n.substring(0, n.length() - 4).toLowerCase(Locale.ROOT).replace(' ', '_');
                    if (!CAPES.containsKey(n)) {
                        register(n, readFile(f));
                    }
                }
            }
        }
        loaded = true;
        MCECapes.LOG.info("Loaded capes: " + CAPES.keySet());
    }

    private static void register(String name, BufferedImage img) {
        if (img == null) {
            return;
        }
        BufferedImage fixed = toCapeFormat(img);
        ResourceLocation loc = Minecraft.getMinecraft().getTextureManager()
                .getDynamicTextureLocation("mcecapes_" + name, new DynamicTexture(fixed));
        CAPES.put(name, loc);
    }

    private static BufferedImage readBuiltin(String name) {
        InputStream in = CapeManager.class.getResourceAsStream(
                "/assets/" + MCECapes.MODID + "/textures/capes/" + name + ".png");
        if (in == null) {
            return null;
        }
        try {
            return ImageIO.read(in);
        } catch (IOException e) {
            MCECapes.LOG.warn("Bad built-in cape " + name, e);
            return null;
        } finally {
            closeQuietly(in);
        }
    }

    private static BufferedImage readFile(File f) {
        try {
            return ImageIO.read(f);
        } catch (IOException e) {
            MCECapes.LOG.warn("Could not read cape " + f, e);
            return null;
        }
    }

    /**
     * The cape model expects a 2:1 texture (64x32, or HD multiples). Old-style
     * 22x17 capes and odd sizes are padded onto a correct canvas.
     */
    private static BufferedImage toCapeFormat(BufferedImage img) {
        int w = img.getWidth();
        int h = img.getHeight();
        if (w == 2 * h && w % 64 == 0) {
            return img;
        }
        int unit = Math.max((w + 63) / 64, (h + 31) / 32);
        if (unit < 1) {
            unit = 1;
        }
        BufferedImage out = new BufferedImage(64 * unit, 32 * unit, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return out;
    }

    private static void closeQuietly(java.io.Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException ignored) {
            }
        }
    }
}
