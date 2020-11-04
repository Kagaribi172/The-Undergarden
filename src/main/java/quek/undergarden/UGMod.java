package quek.undergarden;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ComposterBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.client.world.DimensionRenderInfo;
import net.minecraft.data.DataGenerator;
import net.minecraft.dispenser.*;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionBrewing;
import net.minecraft.potion.Potions;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.FlatChunkGenerator;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import quek.undergarden.client.ClientStuff;
import quek.undergarden.client.UGDimensionRenderInfo;
import quek.undergarden.data.*;
import quek.undergarden.entity.projectile.BlisterbombEntity;
import quek.undergarden.entity.projectile.GooBallEntity;
import quek.undergarden.entity.projectile.RottenBlisterberryEntity;
import quek.undergarden.entity.projectile.SlingshotAmmoEntity;
import quek.undergarden.item.UGSpawnEggItem;
import quek.undergarden.registry.*;

import java.util.HashMap;
import java.util.Map;

@Mod(UGMod.MODID)
public class UGMod {
	
	public static final String MODID = "undergarden";

	public UGMod() {
		IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

		bus.addListener(this::setup);
		bus.addListener(this::clientSetup);
		bus.addListener(this::gatherData);

		IEventBus forgeBus = MinecraftForge.EVENT_BUS;
		forgeBus.addListener(EventPriority.NORMAL, UGStructures::addDimensionalSpacing);

		UGEntityTypes.ENTITIES.register(bus);
		UGBlocks.BLOCKS.register(bus);
		UGItems.ITEMS.register(bus);
		UGFeatures.FEATURES.register(bus);
		UGCarvers.CARVERS.register(bus);
		UGEffects.EFFECTS.register(bus);
		UGPotions.POTIONS.register(bus);
		UGFluids.FLUIDS.register(bus);
		UGParticleTypes.PARTICLES.register(bus);
		UGTileEntities.TEs.register(bus);
		UGStructures.STRUCTURES.register(bus);
	}

	public void setup(FMLCommonSetupEvent event) {
		UGEntityTypes.spawnPlacements();
		UGEntityTypes.entityAttributes();
		UGFeatures.registerConfiguredFeatures();
		UGCarvers.registerConfiguredCarvers();
		UGStructures.registerStructures();
		UGStructures.registerConfiguredStructures();
		UGCriteria.register();

		AxeItem.BLOCK_STRIPPING_MAP = ImmutableMap.<Block, Block>builder()
				.putAll(AxeItem.BLOCK_STRIPPING_MAP)
				.put(UGBlocks.smogstem_log.get(), UGBlocks.stripped_smogstem_log.get())
				.put(UGBlocks.smogstem_wood.get(), UGBlocks.stripped_smogstem_wood.get())
				.put(UGBlocks.wigglewood_log.get(), UGBlocks.stripped_wigglewood_log.get())
				.put(UGBlocks.wigglewood_wood.get(), UGBlocks.stripped_wigglewood_wood.get())
				.put(UGBlocks.grongle_stem.get(), UGBlocks.stripped_grongle_stem.get())
				.put(UGBlocks.grongle_hyphae.get(), UGBlocks.stripped_grongle_hyphae.get())
				.build();

		HoeItem.HOE_LOOKUP = ImmutableMap.<Block, BlockState>builder()
				.putAll(HoeItem.HOE_LOOKUP)
				.put(UGBlocks.deepturf_block.get(), UGBlocks.deepsoil_farmland.get().getDefaultState())
				.put(UGBlocks.deepsoil.get(), UGBlocks.deepsoil_farmland.get().getDefaultState())
				.put(UGBlocks.coarse_deepsoil.get(), UGBlocks.deepsoil.get().getDefaultState())
				.build();

		IDispenseItemBehavior bucketBehavior = new DefaultDispenseItemBehavior() {
			private final DefaultDispenseItemBehavior defaultBehavior = new DefaultDispenseItemBehavior();

			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				BucketItem bucketitem = (BucketItem)stack.getItem();
				BlockPos blockpos = source.getBlockPos().offset(source.getBlockState().get(DispenserBlock.FACING));
				World world = source.getWorld();
				if (bucketitem.tryPlaceContainedLiquid(null, world, blockpos, null)) {
					bucketitem.onLiquidPlaced(world, stack, blockpos);
					return new ItemStack(Items.BUCKET);
				} else {
					return this.defaultBehavior.dispense(source, stack);
				}
			}
		};

		DefaultDispenseItemBehavior eggBehavior = new DefaultDispenseItemBehavior() {
			public ItemStack dispenseStack(IBlockSource source, ItemStack stack) {
				Direction direction = source.getBlockState().get(DispenserBlock.FACING);
				EntityType<?> type = ((UGSpawnEggItem)stack.getItem()).getType(stack.getTag());
				type.spawn(source.getWorld(), stack, null, source.getBlockPos().offset(direction), SpawnReason.DISPENSER, direction != Direction.UP, false);
				stack.shrink(1);
				return stack;
			}
		};

		DispenserBlock.registerDispenseBehavior(UGItems.virulent_mix_bucket.get(), bucketBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.gwibling_bucket.get(), bucketBehavior);

		DispenserBlock.registerDispenseBehavior(UGItems.dweller_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.gwibling_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.rotdweller_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.rotling_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.rotwalker_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.rotbeast_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.brute_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.scintling_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.gloomper_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.stoneborn_spawn_egg.get(), eggBehavior);
		DispenserBlock.registerDispenseBehavior(UGItems.masticator_spawn_egg.get(), eggBehavior);

		DispenserBlock.registerDispenseBehavior(UGItems.depthrock_pebble.get(), new ProjectileDispenseBehavior() {
			protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
				return Util.make(new SlingshotAmmoEntity(worldIn, position.getX(), position.getY(), position.getZ()), (entity) -> entity.setItem(stackIn));
			}
		});

		DispenserBlock.registerDispenseBehavior(UGItems.goo_ball.get(), new ProjectileDispenseBehavior() {
			protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
				return Util.make(new GooBallEntity(worldIn, position.getX(), position.getY(), position.getZ()), (entity) -> entity.setItem(stackIn));
			}
		});

		DispenserBlock.registerDispenseBehavior(UGItems.rotten_blisterberry.get(), new ProjectileDispenseBehavior() {
			protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
				return Util.make(new RottenBlisterberryEntity(worldIn, position.getX(), position.getY(), position.getZ()), (entity) -> entity.setItem(stackIn));
			}
		});

		DispenserBlock.registerDispenseBehavior(UGItems.blisterbomb.get(), new ProjectileDispenseBehavior() {
			protected ProjectileEntity getProjectileEntity(World worldIn, IPosition position, ItemStack stackIn) {
				return Util.make(new BlisterbombEntity(worldIn, position.getX(), position.getY(), position.getZ()), (entity) -> entity.setItem(stackIn));
			}
		});

		PotionBrewing.addMix(Potions.AWKWARD, UGBlocks.blood_mushroom_globule.get().asItem(), UGPotions.brittleness.get());
		PotionBrewing.addMix(UGPotions.brittleness.get(), Items.REDSTONE, UGPotions.long_brittleness.get());
		PotionBrewing.addMix(UGPotions.brittleness.get(), Items.GLOWSTONE_DUST, UGPotions.strong_brittleness.get());

		PotionBrewing.addMix(Potions.AWKWARD, UGBlocks.gloomgourd.get().asItem(), UGPotions.virulent_resistance.get());
		PotionBrewing.addMix(UGPotions.virulent_resistance.get(), Items.REDSTONE, UGPotions.long_virulent_resistance.get());

		ComposterBlock.registerCompostable(0.1F, UGItems.droopvine_item.get());
		ComposterBlock.registerCompostable(0.1F, UGItems.underbeans.get());
		ComposterBlock.registerCompostable(0.2F, UGItems.blisterberry.get());
		ComposterBlock.registerCompostable(0.3F, UGItems.gloomgourd_seeds.get());
		ComposterBlock.registerCompostable(0.3F, UGItems.glowing_kelp.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.smogstem_leaves.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.wigglewood_leaves.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.smogstem_sapling.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.wigglewood_sapling.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.gronglet.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.deepturf.get());
		ComposterBlock.registerCompostable(0.3F, UGBlocks.shimmerweed.get());
		ComposterBlock.registerCompostable(0.5F, UGBlocks.tall_deepturf.get());
		ComposterBlock.registerCompostable(0.5F, UGBlocks.ditchbulb_plant.get());
		ComposterBlock.registerCompostable(0.5F, UGItems.ditchbulb.get());
		ComposterBlock.registerCompostable(0.5F, UGBlocks.tall_shimmerweed.get());
		ComposterBlock.registerCompostable(0.65F, UGBlocks.indigo_mushroom.get());
		ComposterBlock.registerCompostable(0.65F, UGBlocks.veil_mushroom.get());
		ComposterBlock.registerCompostable(0.65F, UGBlocks.ink_mushroom.get());
		ComposterBlock.registerCompostable(0.65F, UGBlocks.indigo_mushroom.get());
		ComposterBlock.registerCompostable(0.65F, UGBlocks.gloomgourd.get());
		ComposterBlock.registerCompostable(0.65F, UGBlocks.carved_gloomgourd.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.indigo_mushroom_cap.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.indigo_mushroom_stalk.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.veil_mushroom_cap.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.veil_mushroom_stalk.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.ink_mushroom_cap.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.blood_mushroom_cap.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.blood_mushroom_globule.get());
		ComposterBlock.registerCompostable(0.85F, UGBlocks.blood_mushroom_stalk.get());
	}

	public void clientSetup(FMLClientSetupEvent event) {
		ClientStuff.registerBlockRenderers();
		ClientStuff.registerEntityRenderers();
		ClientStuff.registerBlockColors();
		ClientStuff.registerItemColors();

		DimensionRenderInfo.field_239208_a_.put(new ResourceLocation(MODID, "undergarden"), new UGDimensionRenderInfo());
		//TODO: OthersideDRI
	}

	public void gatherData(GatherDataEvent event) {
		DataGenerator generator = event.getGenerator();

		if(event.includeClient()) {
			generator.addProvider(new UGBlockStates(generator, event.getExistingFileHelper()));
			generator.addProvider(new UGItemModels(generator, event.getExistingFileHelper()));
			generator.addProvider(new UGLang(generator));
		}
		if(event.includeServer()) {
			generator.addProvider(new UGRecipes(generator));
			generator.addProvider(new UGLootTables(generator));
		}
	}
}
