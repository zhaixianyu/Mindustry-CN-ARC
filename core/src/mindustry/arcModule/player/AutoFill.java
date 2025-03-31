package mindustry.arcModule.player;

import arc.Core;
import arc.Events;
import mindustry.game.EventType;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.storage.StorageBlock;

import static mindustry.Vars.*;

public class AutoFill {
    public final static AutoFill INSTANCE = new AutoFill();
    private long lastRunTime = System.currentTimeMillis();
    public long interval = 500;

    private AutoFill() {
        Events.run(EventType.Trigger.update, () -> {
            long timeMillis = System.currentTimeMillis();
            if (timeMillis > lastRunTime + interval && Core.settings.getBool("autoFill")) {
                ItemStack stack = player.unit().stack;
                Item item = player.unit().hasItem() ? stack.item : null;
                lastRunTime = timeMillis;
                player.dropItems();

                if (item != null && !player.unit().hasItem()){
                    indexer.eachBlock(player.team(), player.x, player.y, itemTransferRange,
                            (build)-> build.block instanceof StorageBlock && build.items.get(item) > 0,
                            (build)-> Call.requestItem(player, build,item,player.unit().maxAccepted(item)));
                }
            }
        });
    }
}
