package io.github.brookite.verseplus.datagen;

import com.google.common.hash.Hashing;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

public final class GrassTrapPitStructureProvider implements DataProvider {
    private static final int DATA_VERSION = 4903;
    private static final int WIDTH = 3;
    private static final int HEIGHT = 11;
    private static final int FLOOR_Y = 0;
    private static final int SPIKES_Y = 1;
    private static final int TRAPS_Y = 10;

    private static final int DRIPSTONE_BLOCK_STATE = 0;
    private static final int POINTED_DRIPSTONE_STATE = 1;
    private static final int AIR_STATE = 2;
    private static final int GRASS_TRAP_STATE = 3;

    private final Path outputPath;
    private final CompletableFuture<HolderLookup.Provider> registries;

    public GrassTrapPitStructureProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> registries
    ) {
        outputPath = output.getOutputFolder(PackOutput.Target.DATA_PACK)
                .resolve("verseplus/structure/grass_trap_pit.nbt");
        this.registries = registries;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput output) {
        return registries.thenAccept(provider -> {
            try {
                CompoundTag structure = createStructure();
                validateStructure(provider, structure);

                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                NbtIo.writeCompressed(structure, bytes);
                byte[] structureBytes = bytes.toByteArray();
                output.writeIfNeeded(outputPath, structureBytes, Hashing.sha256().hashBytes(structureBytes));
            } catch (IOException exception) {
                throw new CompletionException(exception);
            }
        });
    }

    @Override
    public String getName() {
        return "Grass trap pit structure";
    }

    private static CompoundTag createStructure() {
        CompoundTag structure = new CompoundTag();
        structure.putInt("DataVersion", DATA_VERSION);
        structure.put("size", intList(WIDTH, HEIGHT, WIDTH));
        structure.put("palette", createPalette());
        structure.put("blocks", createBlocks());
        structure.put("entities", new ListTag());
        return structure;
    }

    private static void validateStructure(HolderLookup.Provider registries, CompoundTag structure) {
        StructureTemplate template = new StructureTemplate();
        template.load(registries.lookupOrThrow(Registries.BLOCK), structure);
        Vec3i expectedSize = new Vec3i(WIDTH, HEIGHT, WIDTH);
        if (!template.getSize().equals(expectedSize)) {
            throw new IllegalStateException(
                    "Generated grass trap pit has size " + template.getSize() + " instead of " + expectedSize
            );
        }
    }

    private static ListTag createPalette() {
        ListTag palette = new ListTag();
        palette.add(blockState("minecraft:dripstone_block"));

        CompoundTag pointedDripstoneProperties = new CompoundTag();
        pointedDripstoneProperties.putString("thickness", "tip");
        pointedDripstoneProperties.putString("vertical_direction", "up");
        pointedDripstoneProperties.putString("waterlogged", "false");
        palette.add(blockState("minecraft:pointed_dripstone", pointedDripstoneProperties));

        palette.add(blockState("minecraft:air"));
        palette.add(blockState("verseplus:grass_trap"));
        return palette;
    }

    private static ListTag createBlocks() {
        ListTag blocks = new ListTag();
        for (int x = 0; x < WIDTH; x++) {
            for (int z = 0; z < WIDTH; z++) {
                blocks.add(structureBlock(x, FLOOR_Y, z, DRIPSTONE_BLOCK_STATE));
                blocks.add(structureBlock(x, SPIKES_Y, z, POINTED_DRIPSTONE_STATE));
                for (int y = SPIKES_Y + 1; y < TRAPS_Y; y++) {
                    blocks.add(structureBlock(x, y, z, AIR_STATE));
                }
                blocks.add(structureBlock(x, TRAPS_Y, z, GRASS_TRAP_STATE));
            }
        }
        return blocks;
    }

    private static CompoundTag blockState(String name) {
        CompoundTag state = new CompoundTag();
        state.putString("Name", name);
        return state;
    }

    private static CompoundTag blockState(String name, CompoundTag properties) {
        CompoundTag state = blockState(name);
        state.put("Properties", properties);
        return state;
    }

    private static CompoundTag structureBlock(int x, int y, int z, int state) {
        CompoundTag block = new CompoundTag();
        block.put("pos", intList(x, y, z));
        block.putInt("state", state);
        return block;
    }

    private static ListTag intList(int first, int second, int third) {
        ListTag values = new ListTag();
        values.add(IntTag.valueOf(first));
        values.add(IntTag.valueOf(second));
        values.add(IntTag.valueOf(third));
        return values;
    }
}
