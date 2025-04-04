package mindustry.arcModule.player;

import arc.Core;
import arc.Events;
import mindustry.arcModule.ARCEvents;
import mindustry.game.EventType;
import mindustry.game.Gamemode;
import mindustry.gen.Call;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.blocks.defense.turrets.BaseTurret;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.StorageBlock;

import static mindustry.Vars.*;

public class AutoFill {
    public final static AutoFill INSTANCE = new AutoFill();
    private long lastRunTime = System.currentTimeMillis();
    public long interval = 500;
    public Item item = null;

    private AutoFill() {
        Events.run(EventType.Trigger.update, () -> {
            long timeMillis = System.currentTimeMillis();
            if (timeMillis > lastRunTime + interval && Core.settings.getBool("autoFill") && player.unit() != null && state.rules.mode() != Gamemode.pvp) {
                if (player.unit().hasItem()) {
                    ItemStack stack = player.unit().stack;
                    Item item = stack.item;
                    lastRunTime = timeMillis;
                    boolean[] tried = new boolean[]{false};
                    indexer.eachBlock(
                            player.team(), player.x, player.y, itemTransferRange,
                            (build) -> build.acceptStack(player.unit().item(), player.unit().stack.amount, player.unit()) > 0 &&
                                    (build.block instanceof BaseTurret || build.block instanceof GenericCrafter),
                            (build) -> {
                                Call.transferInventory(player, build);
                                tried[0] = true;
                            });
                    if (tried[0]) {
                        this.item = item;
                        pickItem();
                    }
                } else {
                    pickItem();
                }
            }
        });
        Events.run(EventType.WorldLoadEvent.class, this::clearItemRecord);
    }

    public void interactionTooFastWarning() {
        lastRunTime = System.currentTimeMillis() + 5000;
    }

    public void clearItemRecord() {
        item = null;
    }

    private void pickItem() {
        if (item == null) return;

        indexer.eachBlock(player.team(), player.x, player.y, itemTransferRange,
                (build) -> build.block instanceof StorageBlock && build.items.get(item) > 0,
                (build) -> Call.requestItem(player, build, item, player.unit().maxAccepted(item)));
    }
}
