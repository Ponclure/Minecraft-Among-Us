package com.github.ponclure.blockus.collection;

import com.github.ponclure.blockus.BlockUsPlugin;
import com.github.ponclure.blockus.game.Game;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class VisionFieldMap extends ConcurrentHashMap<UUID, Set<UUID>> {

    private final Game game;
    private final HashSet<UUID> imposters;
    private final double distSquaredImposter;
    private final double distSquaredCrewmate;

    public VisionFieldMap(Game game) {
        this.game = game;
        this.imposters = new HashSet(game.getImposters().stream().map(uuid -> uuid.getUuid()).collect(Collectors.toSet()));
        this.distSquaredImposter = Math.pow(game.getSettings().getImpostorVision(), 2);
        this.distSquaredCrewmate = Math.pow(game.getSettings().getCrewmateVision(), 2);
        for (UUID uuid : game.getParticipants().keySet()) {
            Set<UUID> uuids = new HashSet<>();
            super.put(uuid, uuids);
        }
        Bukkit.getScheduler().scheduleSyncRepeatingTask(BlockUsPlugin.getBlockUs().plugin(), new BukkitRunnable() {
            @Override
            public void run() {
                if (!game.isValid()) {
                    cancel();
                }
                for (UUID uuid : VisionFieldMap.this.keySet()) {
                    for (UUID visible : VisionFieldMap.this.get(uuid)) {
                        Player currentPlayer = Bukkit.getPlayer(uuid);
                        Player visiblePlayer = Bukkit.getPlayer(visible);
                        if (imposters.contains(uuid)) {
                            if (currentPlayer.getLocation().distanceSquared(visiblePlayer.getLocation()) > distSquaredImposter) {
                                currentPlayer.hidePlayer(BlockUsPlugin.getBlockUs().plugin(), visiblePlayer);
                            }
                        } else {
                            if (currentPlayer.getLocation().distanceSquared(visiblePlayer.getLocation()) > distSquaredCrewmate) {
                                currentPlayer.hidePlayer(BlockUsPlugin.getBlockUs().plugin(), visiblePlayer);
                            }
                        }
                    }
                }
            }
        }, 20L, 20L);
    }

    public void put(UUID uuid, List<Entity> entities) {
        Player player = Bukkit.getPlayer(uuid);
        entities.forEach(entity -> {
            if (entity instanceof Player) {
                super.get(uuid).add(entity.getUniqueId());
                player.showPlayer(BlockUsPlugin.getBlockUs().plugin(), (Player) entity);
            }
        });
    }

    public void addUUID(UUID uuid, UUID visible) {
        super.get(uuid).add(visible);
        Bukkit.getPlayer(uuid).showPlayer(BlockUsPlugin.getBlockUs().plugin(), Bukkit.getPlayer(visible));
    }

    public void removeUUID(UUID uuid, UUID notVisible) {
        super.get(uuid).remove(notVisible);
        Bukkit.getPlayer(uuid).hidePlayer(BlockUsPlugin.getBlockUs().plugin(), Bukkit.getPlayer(notVisible));
    }

    public void forceDefaults() {
        for (UUID uuid : super.keySet()) {
            super.get(uuid).forEach(x -> removeUUID(uuid, x));
        }
    }

}
