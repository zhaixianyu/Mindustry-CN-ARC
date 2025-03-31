package mindustry.arcModule.player;

import arc.Core;
import arc.Events;
import mindustry.game.EventType;

import static mindustry.Vars.player;

public class AutoFill {
    public final static AutoFill INSTANCE = new AutoFill();
    private long lastRunTime = System.currentTimeMillis();
    public long interval = 500;

    private AutoFill() {
        Events.run(EventType.Trigger.update, () -> {
            long timeMillis = System.currentTimeMillis();

            if (timeMillis > lastRunTime + interval && Core.settings.getBool("autoFill")) {
                lastRunTime = timeMillis;
                player.dropItems();
            }
        });
    }
}
