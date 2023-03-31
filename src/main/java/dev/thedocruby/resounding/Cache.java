package dev.thedocruby.resounding;

import dev.thedocruby.resounding.toolbox.MaterialData;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Pair;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Map.entry;

public class Cache {
    // do these really belong here?
    public final static VoxelShape EMPTY = VoxelShapes.empty();
    public final static VoxelShape CUBE = VoxelShapes.fullCube();

    public final static Map<Block, Pair<Double,Double>> blockMap = new HashMap<>() {{
        put(null        , pair(1.00, 0.00));
        put(Blocks.STONE, pair(0.90, 0.40));
        put(Blocks.AIR  , pair(0.00, 0.98));
    }};
    // Map.ofEntries() {
    @Environment(EnvType.CLIENT) // TODO: Make sure this is used everywhere, add example text
    public static final Map<String, MaterialData> materialDefaults =
            Map.<String, MaterialData>ofEntries(
                    entry("Coral",              new MaterialData(null, 0.350, 0.250)),  // Coral              (coral_block)
                    entry("Gravel, Dirt",       new MaterialData(null, 0.500, 0.650)),  // Gravel, Dirt       (gravel, rooted_dirt)
                    entry("Amethyst",           new MaterialData(null, 0.850, 0.400)),  // Amethyst           (amethyst_block, small_amethyst_bud, medium_amethyst_bud, large_amethyst_bud, amethyst_cluster)
                    entry("Sand",               new MaterialData(null, 0.400, 0.600)),  // Sand               (sand)
                    entry("Candle Wax",         new MaterialData(null, 0.350, 0.400)),  // Candle Wax         (candle)
                    entry("Weeping Vines",      new MaterialData(null, 0.300, 0.300)),  // Weeping Vines      (weeping_vines, weeping_vines_low_pitch)
                    entry("Soul Sand",          new MaterialData(null, 0.050, 0.850)),  // Soul Sand          (soul_sand)
                    entry("Soul Soil",          new MaterialData(null, 0.100, 0.900)),  // Soul Soil          (soul_soil)
                    entry("Basalt",             new MaterialData(null, 0.800, 0.375)),  // Basalt             (basalt)
                    entry("Netherrack",         new MaterialData(null, 0.750, 0.450)),  // Netherrack         (netherrack, nether_ore, nether_gold_ore)
                    entry("Nether Brick",       new MaterialData(null, 0.880, 0.400)),  // Nether Brick       (nether_bricks)
                    entry("Honey",              new MaterialData(null, 0.120, 0.350)),  // Honey              (honey_block)
                    entry("Bone",               new MaterialData(null, 0.900, 0.300)),  // Bone               (bone_block)
                    entry("Nether Wart",        new MaterialData(null, 0.200, 0.800)),  // Nether Wart        (nether_wart, wart_block)
                    entry("Grass, Foliage",     new MaterialData(null, 0.240, 0.240)),  // Grass, Foliage     (grass, crop, bamboo_sapling, sweet_berry_bush)
                    entry("Metal",              new MaterialData(null, 0.950, 0.400)),  // Metal              (metal, copper, anvil)
                    entry("Aquatic Foliage",    new MaterialData(null, 0.550, 0.650)),  // Aquatic Foliage    (wet_grass, lily_pad)
                    entry("Glass, Ice",         new MaterialData(null, 0.900, 0.320)),  // Glass, Ice         (glass)
                    entry("Nether Foliage",     new MaterialData(null, 0.150, 0.500)),  // Nether Foliage     (roots, nether_sprouts)
                    entry("Shroomlight",        new MaterialData(null, 0.850, 0.300)),  // Shroomlight        (shroomlight)
                    entry("Chain",              new MaterialData(null, 0.800, 0.550)),  // Chain              (chain)
                    entry("Deepslate",          new MaterialData(null, 0.940, 0.600)),  // Deepslate          (deepslate)
                    entry("Wood",               new MaterialData(null, 0.675, 0.400)),  // Wood               (wood, ladder)
                    entry("Deepslate Tiles",    new MaterialData(null, 0.975, 0.525)),  // Deepslate Tiles    (deepslate_tiles)
                    entry("Stone, Blackstone",  new MaterialData(null, 0.900, 0.500)),  // Stone, Blackstone  (stone, calcite, gilded_blackstone)
                    entry("Slime",              new MaterialData(null, 0.880, 0.620)),  // Slime              (slime_block)
                    entry("Polished Deepslate", new MaterialData(null, 0.975, 0.600)),  // Polished Deepslate (polished_deepslate, deepslate_bricks)
                    entry("Snow",               new MaterialData(null, 0.250, 0.420)),  // Snow               (snow)
                    entry("Azalea Leaves",      new MaterialData(null, 0.300, 0.350)),  // Azalea Leaves      (azalea_leaves)
                    entry("Bamboo",             new MaterialData(null, 0.600, 0.300)),  // Bamboo             (bamboo, scaffolding)
                    entry("Mushroom Stems",     new MaterialData(null, 0.600, 0.650)),  // Mushroom Stems     (stem)
                    entry("Wool",               new MaterialData(null, 0.025, 0.950)),  // Wool               (wool)
                    entry("Dry Foliage",        new MaterialData(null, 0.250, 0.150)),  // Dry Foliage        (vine, hanging_roots, glow_lichen)
                    entry("Azalea Bush",        new MaterialData(null, 0.300, 0.450)),  // Azalea Bush        (azalea)
                    entry("Lush Cave Foliage",  new MaterialData(null, 0.350, 0.250)),  // Lush Foliage       (cave_vines, spore_blossom, small_dripleaf, big_dripleaf)
                    entry("Netherite",          new MaterialData(null, 0.995, 0.300)),  // Netherite          (netherite_block, lodestone)
                    entry("Ancient Debris",     new MaterialData(null, 0.450, 0.800)),  // Ancient Debris     (ancient_debris)
                    entry("Nether Fungus Stem", new MaterialData(null, 0.300, 0.650)),  // Nether Fungus Stem (nether_stem)
                    entry("Powder Snow",        new MaterialData(null, 0.180, 0.100)),  // Powder Snow        (powder_snow)
                    entry("Tuff",               new MaterialData(null, 0.750, 0.400)),  // Tuff               (tuff)
                    entry("Moss",               new MaterialData(null, 0.200, 0.400)),  // Moss               (moss, moss_carpet)
                    entry("Nylium",             new MaterialData(null, 0.400, 0.500)),  // Nylium             (nylium)
                    entry("Nether Mushroom",    new MaterialData(null, 0.250, 0.750)),  // Nether Mushroom    (fungus)
                    entry("Lanterns",           new MaterialData(null, 0.750, 0.350)),  // Lanterns           (lantern)
                    entry("Dripstone",          new MaterialData(null, 0.850, 0.320)),  // Dripstone          (dripstone_block, pointed_dripstone)
                    entry("Sculk Sensor",       new MaterialData(null, 0.150, 0.850)),  // Sculk Sensor       (sculk_sensor)
                    entry("DEFAULT",            new MaterialData(null, 0.500, 0.500))   // Default Material   ()
            ); // }
    // Map.ofEntries() {
    public static final Map<BlockSoundGroup, BlockSoundGroup> redirectMap =
            Map.ofEntries(  // first becomes second
                    entry(BlockSoundGroup.MOSS_CARPET, BlockSoundGroup.MOSS_BLOCK),
                    entry(BlockSoundGroup.AMETHYST_CLUSTER, BlockSoundGroup.AMETHYST_BLOCK),
                    entry(BlockSoundGroup.SMALL_AMETHYST_BUD, BlockSoundGroup.AMETHYST_BLOCK),
                    entry(BlockSoundGroup.MEDIUM_AMETHYST_BUD, BlockSoundGroup.AMETHYST_BLOCK),
                    entry(BlockSoundGroup.LARGE_AMETHYST_BUD, BlockSoundGroup.AMETHYST_BLOCK),
                    entry(BlockSoundGroup.POINTED_DRIPSTONE, BlockSoundGroup.DRIPSTONE_BLOCK),
                    entry(BlockSoundGroup.FLOWERING_AZALEA, BlockSoundGroup.AZALEA),
                    entry(BlockSoundGroup.DEEPSLATE_BRICKS, BlockSoundGroup.POLISHED_DEEPSLATE),
                    entry(BlockSoundGroup.COPPER, BlockSoundGroup.METAL),
                    entry(BlockSoundGroup.ANVIL, BlockSoundGroup.METAL),
                    entry(BlockSoundGroup.NETHER_SPROUTS, BlockSoundGroup.ROOTS),
                    entry(BlockSoundGroup.WEEPING_VINES_LOW_PITCH, BlockSoundGroup.WEEPING_VINES),
                    entry(BlockSoundGroup.LILY_PAD, BlockSoundGroup.WET_GRASS),
                    entry(BlockSoundGroup.NETHER_GOLD_ORE, BlockSoundGroup.NETHERRACK),
                    entry(BlockSoundGroup.NETHER_ORE, BlockSoundGroup.NETHERRACK),
                    entry(BlockSoundGroup.CALCITE, BlockSoundGroup.STONE),
                    entry(BlockSoundGroup.GILDED_BLACKSTONE, BlockSoundGroup.STONE),
                    entry(BlockSoundGroup.SMALL_DRIPLEAF, BlockSoundGroup.CAVE_VINES),
                    entry(BlockSoundGroup.BIG_DRIPLEAF, BlockSoundGroup.CAVE_VINES),
                    entry(BlockSoundGroup.SPORE_BLOSSOM, BlockSoundGroup.CAVE_VINES),
                    entry(BlockSoundGroup.GLOW_LICHEN, BlockSoundGroup.VINE),
                    entry(BlockSoundGroup.HANGING_ROOTS, BlockSoundGroup.VINE),
                    entry(BlockSoundGroup.ROOTED_DIRT, BlockSoundGroup.GRAVEL),
                    entry(BlockSoundGroup.WART_BLOCK, BlockSoundGroup.NETHER_WART),
                    entry(BlockSoundGroup.CROP, BlockSoundGroup.GRASS),
                    entry(BlockSoundGroup.BAMBOO_SAPLING, BlockSoundGroup.GRASS),
                    entry(BlockSoundGroup.SWEET_BERRY_BUSH, BlockSoundGroup.GRASS),
                    entry(BlockSoundGroup.SCAFFOLDING, BlockSoundGroup.BAMBOO),
                    entry(BlockSoundGroup.LODESTONE, BlockSoundGroup.NETHERITE),
                    entry(BlockSoundGroup.LADDER, BlockSoundGroup.WOOD)
            ); // }
    // Map.ofEntries() {
    public static final Map<BlockSoundGroup, String> groupToName =
            Map.ofEntries(
                    entry(BlockSoundGroup.CORAL			, "Coral"			),	// Coral			(coral_block)
                    entry(BlockSoundGroup.GRAVEL		, "Gravel, Dirt"		),	// Gravel, Dirt		(gravel, rooted_dirt)
                    entry(BlockSoundGroup.AMETHYST_BLOCK, "Amethyst"			),	// Amethyst			(amethyst_block, small_amethyst_bud, medium_amethyst_bud, large_amethyst_bud, amethyst_cluster)
                    entry(BlockSoundGroup.SAND			, "Sand"				),	// Sand				(sand)
                    entry(BlockSoundGroup.CANDLE		, "Candle Wax"		),	// Candle Wax		(candle)
                    entry(BlockSoundGroup.WEEPING_VINES	, "Weeping Vines"	),	// Weeping Vines	(weeping_vines, weeping_vines_low_pitch)
                    entry(BlockSoundGroup.SOUL_SAND		, "Soul Sand"		),	// Soul Sand		(soul_sand)
                    entry(BlockSoundGroup.SOUL_SOIL		, "Soul Soil"		),	// Soul Soil		(soul_soil)
                    entry(BlockSoundGroup.BASALT		, "Basalt"			),	// Basalt			(basalt)
                    entry(BlockSoundGroup.NETHERRACK	, "Netherrack"		),	// Netherrack		(netherrack, nether_ore, nether_gold_ore)
                    entry(BlockSoundGroup.NETHER_BRICKS	, "Nether Brick"		),	// Nether Brick		(nether_bricks)
                    entry(BlockSoundGroup.HONEY			, "Honey"			),	// Honey			(honey_block)
                    entry(BlockSoundGroup.BONE			, "Bone"				),	// Bone				(bone_block)
                    entry(BlockSoundGroup.NETHER_WART	, "Nether Wart"		),	// Nether Wart		(nether_wart, wart_block)
                    entry(BlockSoundGroup.GRASS			, "Grass, Foliage"	),	// Grass, Foliage	(grass, crop, bamboo_sapling, sweet_berry_bush)
                    entry(BlockSoundGroup.METAL			, "Metal"			),	// Metal			(metal, copper, anvil)
                    entry(BlockSoundGroup.WET_GRASS		, "Aquatic Foliage"	),	// Aquatic Foliage	(wet_grass, lily_pad)
                    entry(BlockSoundGroup.GLASS			, "Glass, Ice"		),	// Glass, Ice		(glass)
                    entry(BlockSoundGroup.ROOTS			, "Nether Foliage"	),	// Nether Foliage	(roots, nether_sprouts)
                    entry(BlockSoundGroup.SHROOMLIGHT	, "Shroomlight"		),	// Shroomlight		(shroomlight)
                    entry(BlockSoundGroup.CHAIN			, "Chain"			),	// Chain			(chain)
                    entry(BlockSoundGroup.DEEPSLATE		, "Deepslate"		),	// Deepslate		(deepslate)
                    entry(BlockSoundGroup.WOOD			, "Wood"				),	// Wood				(wood, ladder)
                    entry(BlockSoundGroup.DEEPSLATE_TILES,"Deepslate Tiles"	),	// Deepslate Tiles	(deepslate_tiles)
                    entry(BlockSoundGroup.STONE			, "Stone, Blackstone"),	// Stone, Blackstone(stone, calcite, gilded_blackstone)
                    entry(BlockSoundGroup.SLIME			, "Slime"			),	// Slime			(slime_block)
                    entry(BlockSoundGroup.POLISHED_DEEPSLATE,"Polished Deepslate"),// Polished Deepslate(polished_deepslate, deepslate_bricks)
                    entry(BlockSoundGroup.SNOW			, "Snow"				),	// Snow				(snow)
                    entry(BlockSoundGroup.AZALEA_LEAVES	, "Azalea Leaves"	),	// Azalea Leaves	(azalea_leaves)
                    entry(BlockSoundGroup.BAMBOO		, "Bamboo"			),	// Bamboo			(bamboo, scaffolding)
                    entry(BlockSoundGroup.STEM			, "Mushroom Stems"	),	// Mushroom Stems	(stem)
                    entry(BlockSoundGroup.WOOL			, "Wool"				),	// Wool				(wool)
                    entry(BlockSoundGroup.VINE			, "Dry Foliage"		),	// Dry Foliage		(vine, hanging_roots, glow_lichen)
                    entry(BlockSoundGroup.AZALEA		, "Azalea Bush"		),	// Azalea Bush		(azalea)
                    entry(BlockSoundGroup.CAVE_VINES	, "Lush Cave Foliage"),	// Lush Cave Foliage(cave_vines, spore_blossom, small_dripleaf, big_dripleaf)
                    entry(BlockSoundGroup.NETHERITE		, "Netherite"		),	// Netherite		(netherite_block, lodestone)
                    entry(BlockSoundGroup.ANCIENT_DEBRIS, "Ancient Debris"	),	// Ancient Debris	(ancient_debris)
                    entry(BlockSoundGroup.NETHER_STEM	,"Nether Fungus Stem"),	//Nether Fungus Stem(nether_stem)
                    entry(BlockSoundGroup.POWDER_SNOW	, "Powder Snow"		),	// Powder Snow		(powder_snow)
                    entry(BlockSoundGroup.TUFF			, "Tuff"				),	// Tuff				(tuff)
                    entry(BlockSoundGroup.MOSS_BLOCK	, "Moss"				),	// Moss				(moss_block, moss_carpet)
                    entry(BlockSoundGroup.NYLIUM		, "Nylium"			),	// Nylium			(nylium)
                    entry(BlockSoundGroup.FUNGUS		, "Nether Mushroom"	),	// Nether Mushroom	(fungus)
                    entry(BlockSoundGroup.LANTERN		, "Lanterns"			),	// Lanterns			(lantern)
                    entry(BlockSoundGroup.DRIPSTONE_BLOCK,"Dripstone"		),	// Dripstone		(dripstone_block, pointed_dripstone)
                    entry(BlockSoundGroup.SCULK_SENSOR	, "Sculk Sensor"		)	// Sculk Sensor		(sculk_sensor)
            ); // }
    public static final Map<String, BlockSoundGroup> nameToGroup = groupToName.keySet().stream().collect(Collectors.toMap(groupToName::get, k -> k));
    // pattern vars {
    // TODO tagging system
    public static final Pattern spamPattern   = Pattern.compile(".*(rain|lava).*"); // spammy sounds
    public static final Pattern stepPattern   = Pattern.compile(".*(step|pf_).*");  // includes presence_footseps
    public static final Pattern gentlePattern = Pattern.compile(".*(ambient|splash|swim|note|compounded).*");
    public static final Pattern ignorePattern = Pattern.compile(".*(music|voice).*");
    // TODO Occlusion
    //ublic static final Pattern blockPattern = Pattern.compile(".*block..*");
    public static final Pattern uiPattern     = Pattern.compile("ui\\..*");

    // coefficient for reflection & coefficient for block permeability (inverse of absorption)
    private static Pair<Double,Double> pair(Double ref, Double perm) { return new Pair<>(ref, perm); }

    // TODO calculate wth atmospherics effect
    // determined by temperature & humidity (global transmission coefficient -> alters permeability)
    public static double transmission = 1;
}
