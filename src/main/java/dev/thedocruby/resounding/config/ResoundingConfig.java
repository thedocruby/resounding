package dev.thedocruby.resounding.config;

import dev.thedocruby.resounding.config.presets.ConfigPresets;
import dev.thedocruby.resounding.toolbox.MaterialData;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.cloth.clothconfig.shadowed.blue.endless.jankson.Comment;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static dev.thedocruby.resounding.config.BlueTapePack.ConfigManager.configVersion;

@SuppressWarnings("CanBeFinal")
@Config(name = "resounding")
@Config.Gui.Background("minecraft:textures/block/note_block.png")
public class ResoundingConfig implements ConfigData {

    @Comment("Whether to enable the Resounding sound engine.\n§7[•]§r Disabling this disables all Resounding effects.")
    public boolean enabled = true;

    @Comment("Change these to taste.")
    @ConfigEntry.Gui.CollapsibleObject
    public General general = new General();

    @Comment("These affect the quality and/or accuracy of the effects, at the cost of performance.")
    @ConfigEntry.Gui.CollapsibleObject
    public Quality quality = new Quality();

    @Comment("These add small features to enhance the immersion and/or physical accuracy of the mod.")
    @ConfigEntry.Gui.CollapsibleObject
    public Effects effects = new Effects();

    @Comment("These affect how sound interacts with different categories of blocks.")
    @ConfigEntry.Gui.CollapsibleObject
    public Materials materials = new Materials();

    @Comment("Minor tweaks and toggles that didn't fit in another category.")
    @ConfigEntry.Gui.CollapsibleObject
    public Misc misc = new Misc();

    @Comment("These should ONLY be used to diagnose issues.\nThey WILL cause major drops in performance and log spam if used for gameplay.\nDO NOT USE unless asked to by a developer.")
    @ConfigEntry.Gui.CollapsibleObject
    public Debug debug = new Debug();

    public static class General{
            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: remove this
            @Comment("Affects how quiet a sound gets based on distance. 0.1 - 1.0\nLower values mean distant sounds are louder.\n1.0 is the physically correct value.")
            public double attenuationFactor = 1.0;

        @Environment(EnvType.CLIENT) @ConfigEntry.BoundedDiscrete(max = 100, min = 0)
        @Comment("The global volume of simulated reverberations, in percent.\n§7[•]§r Range: 0 - 100\n§a[+]§r Performance Impact: Low")
        public int globalReverbGain = 50; // TODO: implement

        @Environment(EnvType.CLIENT)
        @Comment("The strength of the reverb effect.\n§7[•]§r Range: >= 0.0\n§7[•]§r Higher values make the echo last longer.\n§7[•]§r Lower values make the echos shorter.\n§a[+]§r Performance Impact: Low")
        public double globalReverbStrength = 9.21;

        @Environment(EnvType.CLIENT)
        @Comment("The smoothness of the reverb.\n§7[•]§r Range: 0.0 - 1.0\n§7[•]§r Affects how uniform the reverb is.\n§7[•]§r Low values cause a distinct fluttering or bouncing echo.\n§7[•]§r High values make this effect less distinct by smoothing out the reverb.\n§a[+]§r Performance Impact: Low")
        public double globalReverbSmoothness = 0.618;

        @Environment(EnvType.CLIENT)
        @Comment("The brightness of reverberation.\n§7[•]§r Range: > 0 \n§7[•]§r Higher values result in more high frequencies in reverberation.\n§7[•]§r Lower values give a more muffled sound to the reverb.\n§7[•]§r 1.0 is neutral.\n§a[+]§r Performance Impact: Low")
        public double globalReverbBrightness = 0.618;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: Occlusion
            public double globalAbsorptionBrightness = 0.618;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: Occlusion. Remove?
            @Comment("The global amount of sound that will be absorbed when traveling through blocks. 0.1 - 4.0")
            public double globalBlockAbsorption = 1.0;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: remove this
            @Comment("The global amount of sound reflectance energy of all blocks.\nLower values result in more conservative reverb simulation with shorter reverb tails.\nHigher values result in more generous reverb simulation with higher reverb tails.\n0.1 - 4.0")
            public double globalBlockReflectance = 1.0;
    }

    public static class Quality{

        @Environment(EnvType.CLIENT) @ConfigEntry.BoundedDiscrete(max = 32, min = 4)
        @Comment("The resolution quality of the reverb effect.\n§7[•]§r Range: 4 - 32\n§a[+]§r Higher values create a fuller, more colorful, more immersive reverb effect.\n§c[ ! ]§r Performance Impact: High\n§e[-]§r Increases memory usage")
        public int reverbResolution = 16;

        @Environment(EnvType.CLIENT) @ConfigEntry.BoundedDiscrete(max = 768, min = 8)
        @Comment("The number of rays to trace to determine reverberation for each sound source.\n§7[•]§r Range: 8 - 768\n§7[•]§r More rays provides more consistent tracing results, but takes more time to calculate.\n§c[ ! ]§r Performance Impact: High")
        public int envEvalRays = 128;

        @Environment(EnvType.CLIENT) @ConfigEntry.BoundedDiscrete(max = 32, min = 2)
        @Comment("The number of extra ray bounces to trace to determine reverberation for each sound source.\n§7[•]§r Range: 4 - 32\n§7[•]§r More bounces provides more echo and sound ducting but takes more time to calculate.\n§7[•]§r Capped by max tracing distance.\n§c[ ! ]§r Performance Impact: High")
        public int envEvalRayBounces = 4;

        @ConfigEntry.BoundedDiscrete(max = 32, min = 1)
        @Comment("Maximum distance of rendered sounds from the player.\n§7[•]§r Range: 1 - 32\n§7[•]§r Minecraft won't allow most sounds to play if they are more than a chunk from the player;\n    Resounding makes that configurable by multiplying this parameter by the default distance.\n§e[-]§r Values too high can cause polyphony issues.\n§7[•]§r Increasing past the world simulation/render distance has no effect.\n§e[-]§r Performance Impact: Moderate\n§e[-]§r  Currently does not seem to work for random block sounds\n     e.g. flowing water, popping lava, whooshing portal")
        public int soundSimulationDistance = 8;

        @Environment(EnvType.CLIENT)
        @Comment("The maximum length of each traced ray, per each bounce, in chunks.\n§7[•]§r Range: 1.0 - 16.0\n§7[•]§r For the best balance of performance and quality, increase this:\n      - When you increase the sound simulation distance\n      - When you decrease the number of ray reflections.\n      - If you often find yourself in large enclosed spaces,\n        e.g. large 1.18 caves, or large open buildings.\n§e[-]§r Performance Impact: Moderate")
        public double rayLength = 4.0;

        @Environment(EnvType.CLIENT) @ConfigEntry.BoundedDiscrete(max = 40, min = 1) // TODO: 0 disables this effect
        @Comment("Reverb refresh interval (in ticks per refresh or 1/(20Hz)).\n§7[•]§r Range: 1 - 40\n§7[•]§r Decreasing this value causes the reverb effect of long sounds to update more frequently.\n§e[-]§r Performance Impact: Moderate")
        public int sourceRefreshRate = 4;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: Occlusion
            @Comment("The amount at which occlusion is capped. 10 * block_occlusion is the theoretical limit")
            public double maxBlockOcclusion = 10;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: Occlusion
            @Comment("Calculate direct occlusion as the minimum of 9 rays from vertices of a block")
            public boolean nineRayBlockOcclusion = true;
    }

    public static class Effects {
        @Environment(EnvType.CLIENT)
        @Comment("Represents how aggressively air absorbs high frequencies over distance.\n§7[•]§r Range: 0.0 - 10.0\n§7[•]§r A value of 1.0 is physically correct for air with normal humidity and temperature.\n§7[•]§r Higher values mean air will absorb more high frequencies with distance.\n§7[•]§r A value of 0.0 disables this effect.\n§a[+]§r Performance Impact: Low")
        public double airAbsorption = 1.0;

        @Environment(EnvType.CLIENT)
        @Comment("How much humidity contributes to the air absorption.\n§7[•]§r Range: 0.0 - 4.0\n§7[•]§r A value of 1.0 is physically correct.\n§7[•]§r Higher values mean air will absorb more high frequencies with distance,\n    depending on the local humidity.\n§7[•]§r A value of 0.0 disables this effect.\n§a[+]§r Performance Impact: Low")
        public double humidityAbsorption = 1.0;

        @Environment(EnvType.CLIENT)
        @Comment("How much rain drops contribute to the air absorption.\n§7[•]§r Range: 0.0 - 2.0\n§7[•]§r A value of 1.0 is approximately physically correct.\n§7[•]§r Higher values mean air will absorb more high frequencies with distance,\n    depending on the local rainfall.\n§7[•]§r A value of 0.0 disables this effect.\n§a[+]§r Performance Impact: Low")
        public double rainAbsorption = 1.0;

        @Environment(EnvType.CLIENT)
        @Comment("How much sound is filtered when the player is underwater.\n§7[•]§r Range: 0.0 - 1.0\n§7[•]§r 0.0 means no filter. 1.0 means fully filtered.\n§a[+]§r Performance Impact: Low")
        public double underwaterFilter = 0.75;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: DirEval
            @Comment("Whether to try calculating where the sound should come from based on reflections.\n§e[-]§r Performance Impact: Moderate")
            public boolean soundDirectionEvaluation = true;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: DirEval
            @Comment("How much the sound direction depends on reflected sounds.\n§7[•]§r Requires \"Re-calculate sound direction\" to be enabled.\n§7[•]§r 0.0 is no reflected sounds, 1.0 is 100% reflected sounds.\n§7[•]§r 0.5 is approximately physically accurate.\n§a[+]§r Performance Impact: Low")
            public double directRaysDirEvalMultiplier = 0.5;
    }

    public static class Materials {
        @Environment(EnvType.CLIENT)
        @Comment("Material properties for blocks.\n§7[•]§r Range: 0.0 - 1.0\n§a[+]§r Performance Impact: Low")
        public Map<String, MaterialData> materialProperties = PrecomputedConfig.materialDefaults;

        @Environment(EnvType.CLIENT)
        @Comment("Material properties for specific blocks. (e.g. block.minecraft.stone)\n§7[•]§r Overrides the \"By category\" setting.\n§a[+]§r Performance Impact: Low")
        public List<String> blockWhiteList = new ArrayList<>();
    }

    public static class Misc {
        @Environment(EnvType.CLIENT)
        @Comment("Disable occlusion of jukeboxes and note blocks.\n§7[•]§r Useful if you have an audio signaling system in your base\n    that you need to hear clearly through the walls.\n§a[+]§r Performance Impact: Low")
        public boolean recordsDisable = false;

            @ConfigEntry.Gui.Excluded @Environment(EnvType.CLIENT) // TODO: Not sure I need this anymore
            @Comment("How strongly the reverb quality is biased toward shorter tails.\n§7[•]§r Range: 1.0 - 5.0\n§a[+]§r This bias helps the reverb sound more accurate in smaller spaces.\n§c[ ! ]§r This setting shouldn't need to be changed,\n      and can cause horrible-sounding reverb if handled incorrectly.\n§7[•]§r However, If you know what you're doing, this value is somewhat similar to\n    the exponent used for warping the shadowmap of a shader\n    to increase the resolution around the player.\n§a[+]§r Performance Impact: Low")
            public double reverbBias = 1;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: DirEval, Occlusion
            @Comment("Skip redirecting non-occluded sounds (the ones you can see directly).\nCan be inaccurate in some situations, especially when \"Re-calculate sound direction\" is enabled.")
            public boolean notOccludedNoRedirect = false;

            @Environment(EnvType.CLIENT) @ConfigEntry.Gui.Excluded // TODO: Occlusion
            @Comment("If true, rain sound sources won't trace for sound occlusion.\nThis can help performance during rain.")
            public boolean skipRainOcclusionTracing = true;

        @Environment(EnvType.CLIENT)
        @Comment("A simpler technique for determining how much airspace is shared between the sound and the listener.\n§7[•]§r Less accurate, but it's faster to calculate.\n§e[-]§r Performance Impact: Moderate")
        public boolean simplerSharedAirspaceSimulation = false;
    }

    public static class Debug {
        @Comment("General debug logging")
        public boolean debugLogging = false;
            @ConfigEntry.Gui.Excluded // TODO: Occlusion
            @Comment("Occlusion tracing information logging.")
            public boolean occlusionLogging = false;
        @Comment("Environment evaluation information logging.\n§c[ ! ]§r WARNING!! For use in void world only!")
        public boolean environmentLogging = false;
        @Comment("Performance information logging.")
        public boolean performanceLogging = false;
        @Comment("Visualize all rays and bounces with particles.\n§c[ ! ]§r WARNING!! For use in void world only!")
        public boolean raytraceParticles = false;
    }

    @ConfigEntry.Gui.Excluded // TODO: update presets and config changer
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.BUTTON)
    @Comment("Soft presets. Some of these can be applied one after another to stack effects onto a base profile.")
    public ConfigPresets preset = ConfigPresets.LOAD_SUCCESS;

    @ConfigEntry.Gui.Excluded
    public String version = configVersion;
}
