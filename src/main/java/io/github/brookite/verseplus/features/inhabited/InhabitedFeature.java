package io.github.brookite.verseplus.features.inhabited;

import com.mojang.brigadier.CommandDispatcher;
import io.github.brookite.verseplus.VersePlus;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.visitors.CollectFields;
import net.minecraft.nbt.visitors.FieldSelector;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.storage.RegionFile;
import net.minecraft.world.level.chunk.storage.RegionStorageInfo;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.LevelResource;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Finds long-inhabited areas from saved chunk data without loading those chunks into the server. */
public final class InhabitedFeature {
    private static final long MINIMUM_INHABITED_TIME = 72_000L;
    private static final int CLUSTER_RADIUS_BLOCKS = 250;
    private static final long CLUSTER_RADIUS_SQUARED = (long) CLUSTER_RADIUS_BLOCKS * CLUSTER_RADIUS_BLOCKS;
    private static final long CONTAINER_SCORE_BONUS = 18_000L;
    private static final int MAX_RESULTS = 10;
    private static final int THROTTLE_MILLIS = 2;
    private static final Pattern REGION_FILE_NAME = Pattern.compile("r\\.(-?\\d+)\\.(-?\\d+)\\.mca");
    private static final Set<String> STORAGE_BLOCK_ENTITIES = Set.of(
            "minecraft:barrel",
            "minecraft:chest",
            "minecraft:shulker_box",
            "minecraft:trapped_chest"
    );
    private static final AtomicBoolean SCAN_RUNNING = new AtomicBoolean();
    private static final ExecutorService SCAN_EXECUTOR = Executors.newSingleThreadExecutor(runnable -> {
        Thread thread = new Thread(runnable, "VersePlus inhabited scanner");
        thread.setDaemon(true);
        thread.setPriority(Thread.MIN_PRIORITY);
        return thread;
    });

    private InhabitedFeature() {
    }

    public static void initialize() {
        CommandRegistrationCallback.EVENT.register(InhabitedFeature::registerCommand);
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> SCAN_EXECUTOR.shutdownNow());
    }

    private static void registerCommand(
            CommandDispatcher<CommandSourceStack> dispatcher,
            net.minecraft.commands.CommandBuildContext buildContext,
            Commands.CommandSelection selection
    ) {
        dispatcher.register(Commands.literal("inhabited")
                .requires(source -> source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                .executes(context -> startScan(context.getSource())));
    }

    private static int startScan(CommandSourceStack source) {
        if (!SCAN_RUNNING.compareAndSet(false, true)) {
            source.sendFailure(Component.translatable("commands.verseplus.inhabited.already_running"));
            return 0;
        }

        MinecraftServer server = source.getServer();
        List<DimensionScan> dimensions = snapshotDimensions(server);
        source.sendSuccess(() -> Component.translatable("commands.verseplus.inhabited.started"), false);
        SCAN_EXECUTOR.execute(() -> {
            ScanResult result;
            try {
                result = scan(dimensions);
            } catch (InterruptedIOException exception) {
                Thread.currentThread().interrupt();
                result = ScanResult.cancelledResult();
            } catch (Exception exception) {
                VersePlus.LOGGER.error("Failed to scan inhabited chunks", exception);
                result = ScanResult.failedResult();
            }

            ScanResult completedResult = result;
            server.execute(() -> {
                SCAN_RUNNING.set(false);
                sendResult(source, completedResult);
            });
        });
        return 1;
    }

    private static List<DimensionScan> snapshotDimensions(MinecraftServer server) {
        Path worldRoot = server.getWorldPath(LevelResource.ROOT);
        List<DimensionScan> dimensions = new ArrayList<>();
        for (var level : server.getAllLevels()) {
            Path regionDirectory = DimensionType.getStorageFolder(level.dimension(), worldRoot).resolve("region");
            dimensions.add(new DimensionScan(level.dimension(), level.dimension().identifier().toString(), regionDirectory));
        }
        return List.copyOf(dimensions);
    }

    private static ScanResult scan(List<DimensionScan> dimensions) throws IOException {
        List<ChunkCandidate> candidates = new ArrayList<>();
        int unreadableChunks = 0;
        for (DimensionScan dimension : dimensions) {
            if (!Files.isDirectory(dimension.regionDirectory())) {
                continue;
            }

            try (Stream<Path> files = Files.list(dimension.regionDirectory())) {
                for (Path regionPath : files
                        .filter(Files::isRegularFile)
                        .filter(path -> REGION_FILE_NAME.matcher(path.getFileName().toString()).matches())
                        .sorted()
                        .toList()) {
                    unreadableChunks += scanRegion(dimension, regionPath, candidates);
                }
            }
        }
        return new ScanResult(cluster(candidates), unreadableChunks, false, false);
    }

    private static int scanRegion(DimensionScan dimension, Path regionPath, List<ChunkCandidate> candidates) throws IOException {
        Matcher matcher = REGION_FILE_NAME.matcher(regionPath.getFileName().toString());
        if (!matcher.matches()) {
            return 0;
        }

        int regionX = Integer.parseInt(matcher.group(1));
        int regionZ = Integer.parseInt(matcher.group(2));
        int unreadableChunks = 0;
        RegionStorageInfo storageInfo = new RegionStorageInfo("inhabited-scan", dimension.key(), "chunk");
        try (RegionFile region = new RegionFile(storageInfo, regionPath, dimension.regionDirectory(), false)) {
            for (int localZ = 0; localZ < 32; localZ++) {
                for (int localX = 0; localX < 32; localX++) {
                    try {
                        ChunkCandidate candidate = readChunk(
                                region,
                                dimension.id(),
                                regionX * 32 + localX,
                                regionZ * 32 + localZ
                        );
                        if (candidate != null) {
                            candidates.add(candidate);
                        }
                    } catch (IOException exception) {
                        unreadableChunks++;
                        VersePlus.LOGGER.warn("Could not read chunk {}, {} from {}", regionX * 32 + localX, regionZ * 32 + localZ, regionPath);
                    }
                    throttle();
                }
            }
        }
        return unreadableChunks;
    }

    private static ChunkCandidate readChunk(RegionFile region, String dimension, int chunkX, int chunkZ) throws IOException {
        try (DataInputStream input = region.getChunkDataInputStream(new net.minecraft.world.level.ChunkPos(chunkX, chunkZ))) {
            if (input == null) {
                return null;
            }

            CollectFields fields = new CollectFields(
                    new FieldSelector(LongTag.TYPE, "InhabitedTime"),
                    new FieldSelector(ListTag.TYPE, "block_entities")
            );
            NbtIo.parse(input, fields, NbtAccounter.unlimitedHeap());
            Tag parsed = fields.getResult();
            if (!(parsed instanceof CompoundTag chunk)) {
                return null;
            }

            long inhabitedTime = chunk.getLongOr("InhabitedTime", 0L);
            if (inhabitedTime < MINIMUM_INHABITED_TIME) {
                return null;
            }
            int filledContainers = chunk.getList("block_entities")
                    .stream()
                    .flatMap(ListTag::compoundStream)
                    .mapToInt(blockEntity -> isFilledPlayerStorage(blockEntity) ? 1 : 0)
                    .sum();
            return new ChunkCandidate(dimension, chunkX, chunkZ, inhabitedTime, filledContainers);
        }
    }

    private static boolean isFilledPlayerStorage(CompoundTag blockEntity) {
        if (blockEntity.contains("LootTable") || !STORAGE_BLOCK_ENTITIES.contains(blockEntity.getString("id").orElse(""))) {
            return false;
        }
        return blockEntity.getList("Items").map(items -> !items.isEmpty()).orElse(false);
    }

    private static void throttle() throws InterruptedIOException {
        try {
            Thread.sleep(THROTTLE_MILLIS);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new InterruptedIOException("Inhabited scan interrupted");
        }
    }

    private static List<InhabitedArea> cluster(List<ChunkCandidate> candidates) {
        Map<String, List<ChunkCandidate>> byDimension = new HashMap<>();
        for (ChunkCandidate candidate : candidates) {
            byDimension.computeIfAbsent(candidate.dimension(), ignored -> new ArrayList<>()).add(candidate);
        }

        List<InhabitedArea> areas = new ArrayList<>();
        for (List<ChunkCandidate> inDimension : byDimension.values()) {
            areas.addAll(clusterDimension(inDimension));
        }
        areas.sort(Comparator.comparingLong(InhabitedArea::score).reversed());
        return List.copyOf(areas);
    }

    private static List<InhabitedArea> clusterDimension(List<ChunkCandidate> candidates) {
        DisjointSet groups = new DisjointSet(candidates.size());
        Map<GridCell, List<Integer>> cells = new HashMap<>();
        for (int index = 0; index < candidates.size(); index++) {
            ChunkCandidate candidate = candidates.get(index);
            GridCell cell = new GridCell(
                    Math.floorDiv(candidate.centerX(), CLUSTER_RADIUS_BLOCKS),
                    Math.floorDiv(candidate.centerZ(), CLUSTER_RADIUS_BLOCKS)
            );
            for (int cellX = cell.x() - 1; cellX <= cell.x() + 1; cellX++) {
                for (int cellZ = cell.z() - 1; cellZ <= cell.z() + 1; cellZ++) {
                    for (int otherIndex : cells.getOrDefault(new GridCell(cellX, cellZ), List.of())) {
                        if (withinClusterRadius(candidate, candidates.get(otherIndex))) {
                            groups.union(index, otherIndex);
                        }
                    }
                }
            }
            cells.computeIfAbsent(cell, ignored -> new ArrayList<>()).add(index);
        }

        Map<Integer, AreaAccumulator> accumulators = new HashMap<>();
        for (int index = 0; index < candidates.size(); index++) {
            int root = groups.find(index);
            accumulators.computeIfAbsent(root, ignored -> new AreaAccumulator()).add(candidates.get(index));
        }
        return accumulators.values().stream().map(AreaAccumulator::finish).toList();
    }

    private static boolean withinClusterRadius(ChunkCandidate first, ChunkCandidate second) {
        long deltaX = first.centerX() - second.centerX();
        long deltaZ = first.centerZ() - second.centerZ();
        return deltaX * deltaX + deltaZ * deltaZ <= CLUSTER_RADIUS_SQUARED;
    }

    private static void sendResult(CommandSourceStack source, ScanResult result) {
        if (result.cancelled()) {
            source.sendFailure(Component.translatable("commands.verseplus.inhabited.cancelled"));
            return;
        }
        if (result.failed()) {
            source.sendFailure(Component.translatable("commands.verseplus.inhabited.failed"));
            return;
        }
        if (result.areas().isEmpty()) {
            source.sendSuccess(() -> Component.translatable("commands.verseplus.inhabited.empty"), false);
            return;
        }

        source.sendSuccess(() -> Component.translatable(
                "commands.verseplus.inhabited.found",
                Math.min(result.areas().size(), MAX_RESULTS)
        ), false);
        for (int index = 0; index < Math.min(result.areas().size(), MAX_RESULTS); index++) {
            InhabitedArea area = result.areas().get(index);
            String averageHours = String.format(Locale.ROOT, "%.1f", area.averageInhabitedTime() / 72_000.0D);
            int rank = index + 1;
            source.sendSuccess(() -> Component.translatable(
                    "commands.verseplus.inhabited.entry",
                    rank,
                    area.dimension(),
                    area.centerX(),
                    area.centerZ(),
                    averageHours
            ), false);
        }
        if (result.unreadableChunks() > 0) {
            source.sendSuccess(() -> Component.translatable("commands.verseplus.inhabited.skipped"), false);
        }
    }

    private record DimensionScan(ResourceKey<Level> key, String id, Path regionDirectory) {
    }

    private record ChunkCandidate(String dimension, int chunkX, int chunkZ, long inhabitedTime, int filledContainers) {
        private int centerX() {
            return chunkX * 16 + 8;
        }

        private int centerZ() {
            return chunkZ * 16 + 8;
        }
    }

    private record InhabitedArea(String dimension, int centerX, int centerZ, long averageInhabitedTime, long score) {
    }

    private record ScanResult(List<InhabitedArea> areas, int unreadableChunks, boolean cancelled, boolean failed) {
        private static ScanResult cancelledResult() {
            return new ScanResult(List.of(), 0, true, false);
        }

        private static ScanResult failedResult() {
            return new ScanResult(List.of(), 0, false, true);
        }
    }

    private record GridCell(int x, int z) {
    }

    private static final class AreaAccumulator {
        private String dimension;
        private long totalInhabitedTime;
        private double weightedX;
        private double weightedZ;
        private int chunks;
        private int filledContainers;

        private void add(ChunkCandidate candidate) {
            dimension = candidate.dimension();
            totalInhabitedTime += candidate.inhabitedTime();
            weightedX += (double) candidate.centerX() * candidate.inhabitedTime();
            weightedZ += (double) candidate.centerZ() * candidate.inhabitedTime();
            chunks++;
            filledContainers += candidate.filledContainers();
        }

        private InhabitedArea finish() {
            long averageInhabitedTime = totalInhabitedTime / chunks;
            return new InhabitedArea(
                    dimension,
                    Math.toIntExact(Math.round((double) weightedX / totalInhabitedTime)),
                    Math.toIntExact(Math.round((double) weightedZ / totalInhabitedTime)),
                    averageInhabitedTime,
                    averageInhabitedTime + filledContainers * CONTAINER_SCORE_BONUS
            );
        }
    }

    private static final class DisjointSet {
        private final int[] parent;
        private final byte[] rank;

        private DisjointSet(int size) {
            parent = new int[size];
            rank = new byte[size];
            for (int index = 0; index < size; index++) {
                parent[index] = index;
            }
        }

        private int find(int value) {
            if (parent[value] != value) {
                parent[value] = find(parent[value]);
            }
            return parent[value];
        }

        private void union(int first, int second) {
            int firstRoot = find(first);
            int secondRoot = find(second);
            if (firstRoot == secondRoot) {
                return;
            }
            if (rank[firstRoot] < rank[secondRoot]) {
                parent[firstRoot] = secondRoot;
            } else if (rank[firstRoot] > rank[secondRoot]) {
                parent[secondRoot] = firstRoot;
            } else {
                parent[secondRoot] = firstRoot;
                rank[firstRoot]++;
            }
        }
    }
}
