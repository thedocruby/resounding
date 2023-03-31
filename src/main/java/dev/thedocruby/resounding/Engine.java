package dev.thedocruby.resounding;

// imports {
// internal {
import dev.thedocruby.resounding.openal.Context;
import dev.thedocruby.resounding.raycast.*;
import dev.thedocruby.resounding.toolbox.*;
// }
// fabric {
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
// }
// minecraft {
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.SoundInstance;
import net.minecraft.client.sound.SoundListener;
import net.minecraft.sound.SoundCategory;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.util.math.*;
// }
// logger {
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
// }
// utils {
import org.apache.commons.lang3.ArrayUtils;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
// }
// * static * {
import static dev.thedocruby.resounding.config.PrecomputedConfig.*;
// }
// }

@SuppressWarnings({"CommentedOutCode"})
// TODO: do more Javadoc
public class Engine {
	// static definitions {
	private Engine() { }

	public static Context root;

	public static EnvType env = null;
	public static MinecraftClient mc;
	public static boolean isOff = true;
	public static final Logger LOGGER = LogManager.getLogger("Resounding");


	// init vars {
	private static Set<Vec3d> rays;
	private static SoundCategory lastSoundCategory;
	private static String lastSoundName;
	private static SoundListener lastSoundListener;
	private static Vec3d playerPos;
	private static Vec3d listenerPos;
	private static WorldChunk soundChunk;
	private static Vec3d soundPos;
	private static BlockPos soundBlockPos;
	private static boolean auxOnly;
	private static boolean isSpam;
	private static int sourceID;
	//private static boolean doDirEval; // TODO: DirEval
	// }
	// }
	
	public static void setRoot(Context context) {root=context;}
	/* utility function */
	public static <T> double logBase(T x, T b) {
		return Math.log((Double) x) / Math.log((Double) b);
	}

	@Environment(EnvType.CLIENT)
	public static void updateRays() {
		final double gRatio = 1.618033988;
		final double epsilon;

		{ // calculate rays + mem.context
			final int r = pC.nRays;
			if      (r >= 600000) { epsilon = 214;  }
			else if (r >= 400000) { epsilon = 75;   }
			else if (r >= 11000)  { epsilon = 27;   }
			else if (r >= 890)    { epsilon = 10;   }
			else if (r >= 177)    { epsilon = 3.33; }
			else if (r >= 24)     { epsilon = 1.33; }
			else                  { epsilon = 0.33; }

			// create queue and calculate vector // TODO verify functionality relative to comment
			rays = IntStream.range(0, r).parallel().unordered().mapToObj(i -> {
				// trig stuff
				final double theta = 2 * Math.PI * i / gRatio;
				final double phi = Math.acos(1 - 2*(i + epsilon) / (r - 1 + 2*epsilon));

				{ // calculate once + mem.context
					final double sP = Math.sin(phi);

					return new Vec3d(
							Math.cos(theta) * sP,
							Math.sin(theta) * sP,
							Math.cos(phi)
					);
				}
			}).collect(Collectors.toSet());
		}
	}

	@Environment(EnvType.CLIENT)
	public static void recordLastSound(@NotNull SoundInstance sound, SoundListener listener) {
		lastSoundCategory = sound.getCategory();
		lastSoundName = sound.getId().getPath();
		lastSoundListener = listener;
	}

	// wraps playSound() in order to process SVC audio chunks
	@Contract("_, _, _, _, _, _ -> _")
	@Environment(EnvType.CLIENT)
	public static void svc_playSound(Context context, double posX, double posY, double posZ, int sourceIDIn, boolean auxOnlyIn) {
		lastSoundName = "voice-chat";
		playSound(context, posX, posY, posZ, sourceIDIn, auxOnlyIn);
	}

	@Environment(EnvType.CLIENT)
	public static void playSound(Context context, double posX, double posY, double posZ, int sourceIDIn, boolean auxOnlyIn) { // The heart of the Resounding audio pipeline
		assert !Engine.isOff;
		long startTime = 0;
		if (pC.pLog) startTime = System.nanoTime();
		auxOnly = auxOnlyIn;
		sourceID = sourceIDIn;

		// TODO integrate with audio tagging
		if (mc.player == null || mc.world == null || Cache.uiPattern.matcher(lastSoundName).matches() || Cache.ignorePattern.matcher(lastSoundName).matches()) {
			if (pC.dLog) {
				LOGGER.info("Skipped playing sound \"{}\": Not a world sound.", lastSoundName);
			} /* else {
				LOGGER.debug("Skipped playing sound \"{}\": Not a world sound.", lastSoundName);
			} */ // disabled for performance
			return;
		}

		// isBlock = blockPattern.matcher(lastSoundName).matches(); // && !stepPattern.matcher(lastSoundName).matches(); //  TODO: Occlusion, step sounds
		if (lastSoundCategory == SoundCategory.RECORDS){posX+=0.5;posY+=0.5;posZ+=0.5;/*isBlock = true;*/} // TODO: Occlusion
		if (Cache.stepPattern.matcher(lastSoundName).matches()) {posY+=0.2;} // TODO: step sounds
		// doNineRay = pC.nineRay && (lastSoundCategory == SoundCategory.BLOCKS || isBlock); // TODO: Occlusion
		{ // get pose - mem.saver
			Vec3d playerPosOld = mc.player.getPos();
			playerPos = new Vec3d(playerPosOld.x, playerPosOld.y + mc.player.getEyeHeight(mc.player.getPose()), playerPosOld.z);
		}
		listenerPos = lastSoundListener.getPos();
		soundPos = new Vec3d(posX, posY, posZ);
		int viewDist = mc.options.getViewDistance();
		double maxDist = Math.min(
				Math.min(
					Math.min(mc.options.simulationDistance, viewDist),
					pC.soundSimulationDistance
				) * 16, // chunk
				pC.maxTraceDist / 2); // diameter -> radius
		soundChunk = mc.world.getChunk(((int)Math.floor(soundPos.x))>>4,((int)Math.floor(soundPos.z))>>4);
		soundBlockPos = new BlockPos(soundPos.x, soundPos.y, soundPos.z);
		boolean isGentle = Cache.gentlePattern.matcher(lastSoundName).matches();

		String message = "";
		final EnvData env;
		// in theory, no debug explanation should be needed
		if ( Math.max(playerPos.distanceTo(soundPos), listenerPos.distanceTo(soundPos)) > maxDist // too quiet
		|| (/*pC.skipRainOcclusionTracing */ Cache.spamPattern.matcher(lastSoundName).matches()       ) // disabled sounds
		|| (  pC.recordsDisable           && lastSoundCategory == SoundCategory.RECORDS         ) // disabled sounds
		) {
			if (pC.dLog) LOGGER.info("Environment not sampled for sound \"{}\"", lastSoundName);
			env = new EnvData(Collections.emptySet(), Collections.emptySet());
		} else {
			if (pC.dLog) {
				LOGGER.info(
						"Sound {"
					+	"\n  Player:   " + playerPos
					+	"\n  Listener: " + listenerPos
					+	"\n  Source:   " + soundPos
					+	"\n  ID:       " + sourceID
					+	"\n  Name:     " + lastSoundCategory + "." + lastSoundName
					+   "\n  }"
				);
			}
			env = evalEnv();
		}
		// CORE PIPELINE
		try { setEnv(context, processEnv(env), isGentle); }
		catch (Exception e) { e.printStackTrace(); }

		if (pC.pLog)
			LOGGER.info("Total calculation time for sound {}: {} milliseconds",
					lastSoundName,
					(System.nanoTime() - startTime)
							/ 1000000D);
	}

	@Environment(EnvType.CLIENT)
	private static double getBlockReflectivity(final @NotNull BlockState blockState) {
		return pC.reflMap.getOrDefault(
				blockState.getBlock().getTranslationKey(),
				pC.reflMap.getOrDefault(
					Cache.groupToName.getOrDefault(
						Cache.redirectMap.getOrDefault(
							blockState.getSoundGroup(),
							blockState.getSoundGroup()),
						"DEFAULT"),
					pC.defaultRefl)
				);
	}

	/* TODO: Occlusion
	@Environment(EnvType.CLIENT)
	private static double getBlockOcclusionD(final BlockState blockState) {
		BlockSoundGroup soundType = blockState.getSoundGroup();
		String blockName = blockState.getBlock().getTranslationKey();
		if (pC.blockWhiteSet.contains(blockName)) return pC.blockWhiteMap.get(blockName).absorption;

		double r = pC.absorptionMap.getOrDefault(soundType, Double.NaN);
		return Double.isNaN(r) ? pC.defaultAbsorption : r;
	}
	*/

	@Environment(EnvType.CLIENT)
	private static @NotNull ReflectedRayData throwReflRay(@NotNull Vec3d dir) {
		Collision rayHit = Patch.fixedRaycast(
				soundPos,
				soundPos.add(dir.multiply(pC.maxTraceDist)),
				mc.world,
				soundBlockPos,
				soundChunk
		);

		if (pC.debug) Renderer.addSoundBounceRay(soundPos, rayHit.getPos(), Formatting.GREEN.getColorValue());

		if (rayHit.isMissed()) {
			return new ReflectedRayData(
					0,
					1,
					0,
					1,
					new double[pC.nRayBounces],
					new double[pC.nRayBounces],
					new double[pC.nRayBounces],
					new double[pC.nRayBounces],
					new double[pC.nRayBounces],
					new double[pC.nRayBounces]
			);
		}

		BlockPos lastHitBlock = rayHit.getBlockPos();
		Vec3d lastHitPos = rayHit.getPos();
		Vec3i lastHitNormal = rayHit.getSide().getVector();
		Vec3d lastRayDir = dir;
		// TODO integrate with fresnel values, and handle refraction (things
		// like wool)
		double lastBlockReflectivity = getBlockReflectivity(rayHit.getBlockState());

		// TODO integrate with velocity logic, this' no simple task
		int size = 0;
		double missed = 0;
		double totalDistance = soundPos.distanceTo(lastHitPos);
		double totalReflectivity = lastBlockReflectivity;
		// TODO create class and interface, instead of many indexed values?
		double[] shared                  = new double[pC.nRayBounces];
		double[] distToPlayer            = new double[pC.nRayBounces];
		double[] bounceDistance          = new double[pC.nRayBounces];
		double[] totalBounceDistance     = new double[pC.nRayBounces];
		double[] bounceReflectivity      = new double[pC.nRayBounces];
		double[] totalBounceReflectivity = new double[pC.nRayBounces];

		// int lastY = Integer.MIN_VALUE; // section index (gets overridden)
		Vec3d position = soundPos;
		Vec3d angle = dir;
		double power = 128; // TODO fine-tune
		int bounces = 100;
		Cast cast = new Cast(null, soundChunk);
		// while power & bounces left
		while (power > 0 && bounces-- > 0) {
			// check if new chunk needed
			int posX = (int) position.x >> 4;
			int posY = (int) position.y >> 4;
			int posZ = (int) position.z >> 4;
			if (posX != last.getX() || posZ != last.getZ()) {
				cast.chunk = mc.world.getChunk(posX, posZ, ChunkStatus.FULL, false);
				cast.tree = overlay.get(new BlockPos(posX, posY, posZ));
			// or if new overlay section reached
			} else if (posY != last.getY()) {
				cast.tree = overlay.get(new BlockPos(posX, posY, posZ));
			}
			// assert angle != null; // should never happen -> power <= 0 -> null, yet breaks above
			Ray ray = cast.raycast(position,angle,power);
			// TODO save values from hit result
			// TODO handle bounces???
			if (pC.debug) Renderer.addSoundBounceRay(position, ray.position, Formatting.GREEN.getColorValue());
			position = ray.position;
			angle = ray.permeated;
			power = ray.permeation;
		}
			if (pC.dRays) Renderer.addSoundBounceRay(position, ray.position(), Formatting.GREEN.getColorValue());
			}

			// if surface hit, and velocity still high enough, trace bounce
			if (pC.debug) Renderer.addSoundBounceRay(lastHitPos, newRayHitPos, Formatting.BLUE.getColorValue());

			lastBlockReflectivity = newBlockReflectivity;
			lastHitPos = newRayHitPos;
			lastHitNormal = rayHit.getSide().getVector();
			lastRayDir = newRayDir;
			lastHitBlock = rayHit.getBlockPos();

			// TODO see comment above `size` initialization
			size = i;
			totalBounceDistance[i] = totalDistance;
			distToPlayer[i] = lastHitPos.distanceTo(listenerPos);
			bounceReflectivity[i] = lastBlockReflectivity;
			totalBounceReflectivity[i] = totalReflectivity;

			// TODO duplicated code??
			// Cast (one) final ray towards the player. If it's
			// unobstructed, then the sound source and the player
			// share airspace.

			finalRayHit = Patch.fixedRaycast(lastHitPos, listenerPos, mc.world, lastHitBlock, rayHit.chunk);

			color = Formatting.GRAY.getColorValue();
			if (finalRayHit.isMissed()) {
				color = Formatting.WHITE.getColorValue();
				shared[i] = 1;
			}
			if (pC.debug) Renderer.addSoundBounceRay(lastHitPos, finalRayHit.getPos(), color);
		}
		} */

		// TODO reorganize class structure for more logical order?
		return new ReflectedRayData(
				++size, missed, totalDistance,
				totalReflectivity, shared,
				distToPlayer, bounceDistance, totalBounceDistance,
				bounceReflectivity, totalBounceReflectivity);
	}

	@Environment(EnvType.CLIENT)
	private static @NotNull Set<OccludedRayData> throwOcclRay(@NotNull Vec3d sourcePos, @NotNull Vec3d sinkPos) { //Direct sound occlusion
		assert !Engine.isOff;

		// TODO: This still needs to be rewritten
		// TODO: fix reflection/absorption calc with fresnels
		// TODO: Allow data theft from other reflection data, and cast occlusion
		// based on conditions then (e.g. rays with the highest distance w/out
		// bouncing)

		/* Old code
		Vec3d normalToPlayer = playerPos.subtract(soundPos).normalize(); TODO: change to `listenerPos`
		double occlusionAccumulation = 0;
		//Cast a ray from the source towards the player
		Vec3d rayOrigin = soundPos;
		BlockPos lastBlockPos = soundBlockPos;
		final boolean _9ray = pC._9Ray && (lastSoundCategory == SoundCategory.BLOCKS || block);
		final int nOccRays = _9ray ? 9 : 1;
		// final List<Map.Entry<Vec3d, Double>> directions = new ArrayList<>(pC.nRays * pC.nRayBounces + nOccRays + 1); // TODO: DirEval
		double occlusionAccMin = Double.MAX_VALUE;
		// TODO: this can probably be spliteratored too
		for (int j = 0; j < nOccRays; j++) {
			if(j > 0){
				final int jj = j - 1;
				rayOrigin = new Vec3d(soundBlockPos.getX() + 0.001 + 0.998 * (jj % 2), soundBlockPos.getY() + 0.001 + 0.998 * ((jj >> 1) % 2), soundBlockPos.getZ() + 0.001 + 0.998 * ((jj >> 2) % 2));
				lastBlockPos = soundBlockPos;
				occlusionAccumulation = 0;

			}
			boolean oAValid = false;
			SPHitResult rayHit = fixedRaycast(rayOrigin, playerPos, mc.world, lastBlockPos, soundChunk);

			for (int i = 0; i < 10; i++) {

				lastBlockPos = rayHit.getBlockPos();
				//If we hit a block

				if (pC.debug) Renderer.addOcclusionRay(rayOrigin, rayHit.getPos(), Color.getHSBColor((float) (1F / 3F * (1F - Math.min(1F, occlusionAccumulation / 12F))), 1F, 1F).getRGB());
				if (rayHit.isMissed()) {
					if (pC.soundDirectionEvaluation) directions.add(Map.entry(rayOrigin.subtract(playerPos), // TODO: DirEval
							(_9ray?9:1) * Math.pow(soundPos.distanceTo(playerPos), 2.0)* pC.rcpTotRays
									/
							(Math.exp(-occlusionAccumulation * pC.globalBlockAbsorption)* pC.directRaysDirEvalMultiplier)
					));
					oAValid = true;
					break;
				}

				final Vec3d rayHitPos = rayHit.getPos();
				final BlockState blockHit = rayHit.getBlockState();
				double blockOcclusion = getBlockOcclusionD(blockHit);

				// Regardless to whether we hit from inside or outside

				if (pC.oLog) logOcclusion(blockHit.getBlock().getTranslationKey() + "    " + rayHitPos.x + ", " + rayHitPos.y + ", " + rayHitPos.z);

				rayOrigin = rayHitPos; //new Vec3d(rayHit.getPos().x + normalToPlayer.x * 0.1, rayHit.getPos().y + normalToPlayer.y * 0.1, rayHit.getPos().z + normalToPlayer.z * 0.1);

				rayHit = fixedRaycast(rayOrigin, playerPos, mc.world, lastBlockPos, rayHit.chunk);

				SPHitResult rayBack = fixedRaycast(rayHit.getPos(), rayOrigin, mc.world, rayHit.getBlockPos(), rayHit.chunk);

				if (rayBack.getBlockPos().equals(lastBlockPos)) {
					//Accumulate density
					occlusionAccumulation += blockOcclusion * (rayOrigin.distanceTo(rayBack.getPos()));
					if (occlusionAccumulation > pC.maxDirectOcclusionFromBlocks) break;
				}

				if (pC.oLog) logOcclusion("New trace position: " + rayOrigin.x + ", " + rayOrigin.y + ", " + rayOrigin.z);
			}
			if (oAValid) occlusionAccMin = Math.min(occlusionAccMin, occlusionAccumulation);
		}
		occlusionAccumulation = Math.min(occlusionAccMin, pC.maxDirectOcclusionFromBlocks);
		double directCutoff = Math.exp(-occlusionAccumulation * pC.globalBlockAbsorption);
		double directGain = auxOnly ? 0 : Math.pow(directCutoff, 0.01);

		if (pC.oLog) logOcclusion("direct cutoff: " + directCutoff + "  direct gain:" + directGain);
		if (isSpam) { return null; }
		*/

		return Collections.emptySet();
	}

	@Environment(EnvType.CLIENT)
	private static @NotNull EnvData evalEnv() {
		// Clear the block shape cache every tick, just in case the local block grid has changed
		// TODO: Do this more efficiently.
		//  In 1.18 there should be something I can mix into to clear only in ticks when the block grid changes
		//  I think i remember a technical change relating to this;
		//  It may be faster to skim through the snapshot changelog instead of digging through the code.
		//  TODO split cache into chunks, refresh only relevant chunks,
		//  implement forgetting-system
		if (Patch.lastUpd != timeT) {
			Patch.shapeCache.clear();
			Patch.lastUpd = timeT;
		}

		// TODO make irrelevant with splitting/power/max bounce limits
		Patch.maxY = mc.world.getTopY();
		Patch.minY = mc.world.getBottomY();
		Patch.maxX = (int) (playerPos.getX() + (viewDist * 16));
		Patch.minX = (int) (playerPos.getX() - (viewDist * 16));
		Patch.maxZ = (int) (playerPos.getZ() + (viewDist * 16));
		Patch.minZ = (int) (playerPos.getZ() - (viewDist * 16));

		// Throw rays around
		// TODO implement tagging system here
		// TODO? implement lambda function referencing to remove branches
		Consumer<String> logger = pC.log ? (pC.eLog ? LOGGER::info : LOGGER::debug) : (x) -> {};
		Set<CastResults> reflRays;
		logger.accept("Sampling environment with "+pC.nRays+" seed rays...");
		reflRays = rays.stream().parallel().unordered().map(Engine::throwReflRay).collect(Collectors.toSet());
		if (pC.eLog) {
			int rayCount = 0;
			for (CastResults reflRay : reflRays){
				rayCount += reflRay.size() * 2 + 1;
			}
			logger.accept("Total number of rays casted: "+rayCount);
		}

		// TODO: Occlusion. Also, add occlusion profiles.
		// Step rays from sound to listener
		Set<OccludedRayData> occlRays = throwOcclRay(soundPos, listenerPos);

		// Pass data to post
		EnvData data = new EnvData(reflRays, occlRays);
		logger.accept("Raw Environment data:\n"+data);
		return data;
	}

	@Contract("_ -> new")
	@Environment(EnvType.CLIENT)
	private static @NotNull SoundProfile processEnv(final EnvData data) {
		// TODO: DirEval is on hold while I rewrite, will be re-added later
		// Take weighted (on squared distance) average of the directions sound reflection came from
		//doDirEval = pC.soundDirectionEvaluation && (occlusionAccumulation > 0 || pC.notOccludedRedirect); // TODO: DirEval
		// dirEval:  TODO: this block can be converted to another multithreaded iterator
		/* {
			if (directions.isEmpty()) break dirEval;

			if (pC.pLog) log("Evaluating direction from " + sendSharedSum + " entries...");
			Vec3d sum = new Vec3d(0, 0, 0);
			double weight = 0;

			for (Map.Entry<Vec3d, Double> direction : directions) {
				final double w = direction.getValue();
				weight += w;
				sum = sum.add(direction.getKey().normalize().multiply(w));
			}
			sum = sum.multiply(1 / weight);
			setSoundPos(sourceID, sum.normalize().multiply(soundPos.distanceTo(playerPos)).add(playerPos));

			// Vec3d pos = sum.normalize().multiply(soundPos.distanceTo(playerPos)).add(playerPos);
			// mc.world.addParticle(ParticleTypes.END_ROD, false, pos.getX(), pos.getY(), pos.getZ(), 0,0,0);
		}*/

		// TODO move to separate effect
		boolean inWater = mc.player != null && mc.player.isSubmergedInWater();
		// TODO properly implement, move to atmospherics
		final double airAbsorptionHF = 1.0; // Air.getAbsorptionHF();
		double directGain = (auxOnly ? 0 : inWater ? pC.waterFilt : 1) * Math.pow(airAbsorptionHF, listenerPos.distanceTo(soundPos));

		if (data.reflRays().isEmpty()) {
			return new SoundProfile(sourceID, directGain, Math.pow(directGain, pC.globalAbsHFRcp), new double[pC.resolution + 1], new double[pC.resolution + 1]);
		}

		double bounceCount = 0.0D;
		double missedSum = 0.0D;
		for (CastResults reflRay : data.reflRays()) {
			bounceCount += reflRay.size();
			missedSum += reflRay.missed();
		}
		missedSum *= pC.rcpNRays;

		// TODO: Does this perform better in parallel? (test using Spark)
		double sharedSum = 0.0D;
		final double[] sendGain = new double[pC.resolution + 1];
		// TODO explain
		for (CastResults reflRay : data.reflRays()) {
			if (reflRay.missed() == 1.0D) continue;

			final int size = reflRay.size();
			final double[] smoothSharedEnergy = new double[pC.nRayBounces];
			final double[] smoothSharedDistance = new double[pC.nRayBounces];
			for (int i = 0; i < size; i++) {
				if (!pC.fastShared ) {
					if (reflRay.shared()[i] == 1) {
						smoothSharedEnergy[i] = 1;
						smoothSharedDistance[i] = reflRay.distToPlayer()[i];
					} else {
						int up; double traceUpRefl = 1; double traceUpDistance = 0;
						for (up = i + 1; up <= size; up++) {
							traceUpRefl *= up == size ? 0 : reflRay.bounceReflectivity()[up];
							if (up != size) traceUpDistance += reflRay.bounceDistance()[up];
							if (up != size && reflRay.shared()[up] == 1) { traceUpDistance += reflRay.distToPlayer()[up]; break; }
						}

						int dn; double traceDownRefl = 1; double traceDownDistance = 0;
						for (dn = i - 1; dn >= -1; dn--) {
							traceDownRefl *= dn == -1 ? 0 : reflRay.bounceReflectivity()[dn];
							if (dn != -1) traceDownDistance += reflRay.bounceDistance()[dn + 1];
							if (dn != -1 && reflRay.shared()[dn] == 1) { traceDownDistance += reflRay.distToPlayer()[dn]; break; }
						}

						if (Math.max(traceDownRefl, traceUpRefl) == traceUpRefl){
							smoothSharedEnergy[i] = traceUpRefl;
							smoothSharedDistance[i] = traceUpDistance;
						} else {
							smoothSharedEnergy[i] = traceDownRefl;
							smoothSharedDistance[i] = traceDownDistance;
						}
					}
				}

				sharedSum += reflRay.shared()[i];

				// TODO integrate with fresnels
				final double playerEnergy = MathHelper.clamp(
								reflRay.totalBounceEnergy()[i] * (pC.fastShared ? 1 : smoothSharedEnergy[i])
								* Math.pow(airAbsorptionHF, reflRay.totalBounceDistance()[i] + (pC.fastShared ? reflRay.distToPlayer()[i] : smoothSharedDistance[i]))
								/ Math.pow(reflRay.totalBounceDistance()[i] + (pC.fastShared ? reflRay.distToPlayer()[i] : smoothSharedDistance[i]), 2.0D * missedSum),
						0, 1);

				final double bounceEnergy = MathHelper.clamp(
								reflRay.totalBounceEnergy()[i]
								* Math.pow(airAbsorptionHF, reflRay.totalBounceDistance()[i])
								/ Math.pow(reflRay.totalBounceDistance()[i], 2.0D * missedSum),
						Double.MIN_VALUE, 1);

				final double bounceTime = reflRay.totalBounceDistance()[i] / speedOfSound;

				sendGain[MathHelper.clamp((int) (1/logBase(Math.max(Math.pow(bounceEnergy, pC.maxDecayTime / bounceTime * pC.energyFix), Double.MIN_VALUE), minEnergy) * pC.resolution), 0, pC.resolution)] += playerEnergy;
			}
		}
		sharedSum /= bounceCount;
		final double[] sendCutoff = new double[pC.resolution+1];
		for (int i = 0; i <= pC.resolution; i++) {
			sendGain[i] = MathHelper.clamp(sendGain[i] * (inWater ? pC.waterFilt : 1) * (pC.fastShared ? sharedSum : 1) * pC.resolution / bounceCount * pC.globalRvrbGain, 0, 1.0 - Double.MIN_NORMAL);
			sendCutoff[i] = Math.pow(sendGain[i], pC.globalRvrbHFRcp); // TODO: make sure this actually works.
		}

		// TODO: Occlusion calculation here, instead of extra ray

		double occlusion = 1;

		// double occlusion = Patch.fixedRaycast(soundPos, listenerPos, mc.world, soundBlockPos, soundChunk).isMissed() ? 1 : 0; // TODO: occlusion coeff from processing goes here IF fancy or fabulous occl

		directGain *= Math.pow(airAbsorptionHF, listenerPos.distanceTo(soundPos))
				/ Math.pow(listenerPos.distanceTo(soundPos), 2 * missedSum)
				* MathHelper.lerp(occlusion, sharedSum, 1);
		double directCutoff = Math.pow(directGain, pC.globalAbsHFRcp); // TODO: make sure this actually works.

		SoundProfile profile = new SoundProfile(sourceID, directGain, directCutoff, sendGain, sendCutoff);

		if (pC.log) LOGGER.info("Processed sound profile:\n{}", profile);

		return profile;
	}

	@Environment(EnvType.CLIENT)
	public static void setEnv(Context context, final @NotNull SoundProfile profile, boolean isGentle) {
		if (profile.sendGain().length != pC.resolution + 1 || profile.sendCutoff().length != pC.resolution + 1) {
			throw new IllegalArgumentException("Error: Reverb parameter count does not match reverb resolution!");
		}

		final SlotProfile finalSend = selectSlot(profile.sendGain(), profile.sendCutoff());

		if (pC.eLog || pC.dLog) {
			LOGGER.info("Final reverb settings:\n{}", finalSend);
		}

		context.update(finalSend, profile, isGentle);
	}


	@Contract("_, _ -> new")
	@Environment(EnvType.CLIENT)
	public static @NotNull SlotProfile selectSlot(double[] sendGain, double[] sendCutoff) {
		if (pC.fastPick) { // TODO: find cause of block.lava.ambient NaN
			int slot = 0;
			double max = sendGain[1];
			for (int i = 2; i <= pC.resolution; i++) if (sendGain[i] > max) {
				slot=i;
				max = sendGain[i];
			}

			final int iavg = slot;
			// Different fast selection method, can't decide which one is better.
			// TODO: Do something with this.
			/* if (false) {
				double sum = 0;
				double weightedSum = 0;
				for (int i = 1; i <= pC.resolution; i++) {
					sum += sendGain[i];
					weightedSum += i * sendGain[i];
				}
				iavg = (int) Math.round(MathHelper.clamp(weightedSum / sum, 0, pC.resolution));
			} */

			return iavg > 0 ? new SlotProfile(iavg, sendGain[iavg], sendCutoff[iavg]) : new SlotProfile(0, sendGain[0], sendCutoff[0]);
		}
		// TODO: Slot selection logic will go here. See https://www.desmos.com/calculator/v5bt1gdgki
		/*
		final double mk = m-k;
		double selected = factorial(m)/(factorial(k)-factorial(mk))*Math.pow(1-x,mk)*Math.min(1,Math.max(0,Math.pow(x,k)));
		 */
		return new SlotProfile(0, 0, 0);
	}

}
