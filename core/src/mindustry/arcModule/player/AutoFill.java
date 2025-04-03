package mindustry.arcModule.player;

import arc.Core;
import arc.Events;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.StorageBlock;

import java.util.concurrent.atomic.AtomicReference;

import static mindustry.Vars.*;

public class AutoFill {
    public final static AutoFill INSTANCE = new AutoFill();
    private long lastRunTime = System.currentTimeMillis();
    public long interval = 500;

    private AutoFill() {
        Events.run(EventType.Trigger.update, () -> {
            long timeMillis = System.currentTimeMillis();
            if (timeMillis > lastRunTime + interval && Core.settings.getBool("autoFill") && player.unit() != null && player.unit().hasItem() && state.rules.mode() == Gamemode.pvp) {
                ItemStack stack = player.unit().stack;
                Item item = stack.item;
                lastRunTime = timeMillis;

                boolean[] tried = new boolean[]{false};
                indexer.eachBlock(
                        player.team(), player.x, player.y, itemTransferRange,
                        (build)->build.acceptStack(player.unit().item(), player.unit().stack.amount, player.unit()) > 0 &&
                                (build.block instanceof BaseTurret || build.block instanceof GenericCrafter),
                        (build)-> {
                            Call.transferInventory(player, build);
                            tried[0] = true;
                        });

                if (tried[0]){
                    indexer.eachBlock(player.team(), player.x, player.y, itemTransferRange,
                            (build)-> build.block instanceof StorageBlock && build.items.get(item) > 0,
                            (build)-> Call.requestItem(player, build,item,player.unit().maxAccepted(item)));
                }
            }
        });
    }
}
