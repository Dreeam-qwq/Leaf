package org.dreeam.leaf.async.tracker;

import ca.spottedleaf.moonrise.common.list.ReferenceList;
import ca.spottedleaf.moonrise.common.misc.NearbyPlayers;
import ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel;
import ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup;
import ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerEntity;
import ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerTrackedEntity;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.FullChunkStatus;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class MultithreadedTracker {

    private static final Logger LOGGER = LogManager.getLogger("MultithreadedTracker");
    public static class MultithreadedTrackerThread extends Thread {
        @Override
        public void run() {
            super.run();
        }
    }
    private static final Executor trackerExecutor = new ThreadPoolExecutor(
            1,
            org.dreeam.leaf.config.modules.async.MultithreadedTracker.asyncEntityTrackerMaxThreads,
            org.dreeam.leaf.config.modules.async.MultithreadedTracker.asyncEntityTrackerKeepalive, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadFactoryBuilder()
                    .setThreadFactory(
                            r -> new MultithreadedTrackerThread() {
                                @Override
                                public void run() {
                                    r.run();
                                }
                            }
                    )
                    .setNameFormat("Leaf Async Tracker Thread - %d")
                    .setPriority(Thread.NORM_PRIORITY - 2)
                    .build());

    private MultithreadedTracker() {
    }

    public static Executor getTrackerExecutor() {
        return trackerExecutor;
    }

    public static void tick(ChunkSystemServerLevel level) {
        try {
            if (!org.dreeam.leaf.config.modules.async.MultithreadedTracker.compatModeEnabled) {
                tickAsync(level);
            } else {
                tickAsyncWithCompatMode(level);
            }
        } catch (Exception e) {
            LOGGER.error("Error occurred while executing async task.", e);
        }
    }

    private static void tickAsync(ChunkSystemServerLevel level) {
        final NearbyPlayers nearbyPlayers = level.moonrise$getNearbyPlayers();
        final ServerEntityLookup entityLookup = (ServerEntityLookup) level.moonrise$getEntityLookup();

        final ReferenceList<Entity> trackerEntities = entityLookup.trackerEntities;
        final Entity[] trackerEntitiesRaw = trackerEntities.getRawDataUnchecked();

        // Move tracking to off-main
        trackerExecutor.execute(() -> {
            for (final Entity entity : trackerEntitiesRaw) {
                if (entity == null) continue;

                final ChunkMap.TrackedEntity tracker = ((EntityTrackerEntity) entity).moonrise$getTrackedEntity();

                if (tracker == null) continue;

                ((EntityTrackerTrackedEntity) tracker).moonrise$tick(nearbyPlayers.getChunk(entity.chunkPosition()));
                tracker.serverEntity.sendChanges();
            }
        });
    }

    private static void tickAsyncWithCompatMode(ChunkSystemServerLevel level) {
        final NearbyPlayers nearbyPlayers = level.moonrise$getNearbyPlayers();
        final ServerEntityLookup entityLookup = (ServerEntityLookup) level.moonrise$getEntityLookup();

        final ReferenceList<Entity> trackerEntities = entityLookup.trackerEntities;
        final Entity[] trackerEntitiesRaw = trackerEntities.getRawDataUnchecked();
        final Runnable[] sendChangesTasks = new Runnable[trackerEntitiesRaw.length];
        int index = 0;

        for (final Entity entity : trackerEntitiesRaw) {
            if (entity == null) continue;

            final ChunkMap.TrackedEntity tracker = ((EntityTrackerEntity) entity).moonrise$getTrackedEntity();

            if (tracker == null) continue;

            ((EntityTrackerTrackedEntity) tracker).moonrise$tick(nearbyPlayers.getChunk(entity.chunkPosition()));
            sendChangesTasks[index++] = () -> tracker.serverEntity.sendChanges(); // Collect send changes to task array
        }

        // batch submit tasks
        trackerExecutor.execute(() -> {
            for (final Runnable sendChanges : sendChangesTasks) {
                if (sendChanges == null) continue;

                sendChanges.run();
            }
        });
    }

    // Original ChunkMap#newTrackerTick of Paper
    // Just for diff usage for future update
    private static void tickOriginal(ServerLevel level) {
        final ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup entityLookup = (ca.spottedleaf.moonrise.patches.chunk_system.level.entity.server.ServerEntityLookup) ((ca.spottedleaf.moonrise.patches.chunk_system.level.ChunkSystemServerLevel) level).moonrise$getEntityLookup();

        final ca.spottedleaf.moonrise.common.list.ReferenceList<net.minecraft.world.entity.Entity> trackerEntities = entityLookup.trackerEntities;
        final Entity[] trackerEntitiesRaw = trackerEntities.getRawDataUnchecked();
        for (int i = 0, len = trackerEntities.size(); i < len; ++i) {
            final Entity entity = trackerEntitiesRaw[i];
            final ChunkMap.TrackedEntity tracker = ((ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerEntity) entity).moonrise$getTrackedEntity();
            if (tracker == null) {
                continue;
            }
            ((ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerTrackedEntity) tracker).moonrise$tick(((ca.spottedleaf.moonrise.patches.chunk_system.entity.ChunkSystemEntity) entity).moonrise$getChunkData().nearbyPlayers);
            if (((ca.spottedleaf.moonrise.patches.entity_tracker.EntityTrackerTrackedEntity) tracker).moonrise$hasPlayers()
                || ((ca.spottedleaf.moonrise.patches.chunk_system.entity.ChunkSystemEntity) entity).moonrise$getChunkStatus().isOrAfter(FullChunkStatus.ENTITY_TICKING)) {
                tracker.serverEntity.sendChanges();
            }
        }
    }
}
