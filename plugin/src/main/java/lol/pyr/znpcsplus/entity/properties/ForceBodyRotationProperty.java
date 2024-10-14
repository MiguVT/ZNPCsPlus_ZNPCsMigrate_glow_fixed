package lol.pyr.znpcsplus.entity.properties;

import com.github.retrooper.packetevents.protocol.entity.data.EntityData;
import lol.pyr.znpcsplus.ZNpcsPlusBootstrap;
import lol.pyr.znpcsplus.entity.PacketEntity;
import lol.pyr.znpcsplus.scheduling.TaskScheduler;
import org.bukkit.entity.Player;

import java.util.Map;

public class ForceBodyRotationProperty extends DummyProperty<Boolean> {
    private final ZNpcsPlusBootstrap plugin;
    private final TaskScheduler scheduler;

    public ForceBodyRotationProperty(ZNpcsPlusBootstrap plugin, TaskScheduler scheduler) {
        super("force_body_rotation", false);
        this.plugin = plugin;
        this.scheduler = scheduler;
    }

    @Override
    public void apply(Player player, PacketEntity entity, boolean isSpawned, Map<Integer, EntityData> properties) {
        if (entity.getProperty(this)) {
            scheduler.runLaterAsync(() -> entity.swingHand(player, false), 2L);
            scheduler.runLaterAsync(() -> entity.swingHand(player, false), 6L);
        }
    }
}
