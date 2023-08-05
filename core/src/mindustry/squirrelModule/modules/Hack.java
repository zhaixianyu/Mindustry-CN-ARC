package mindustry.squirrelModule.modules;

import arc.Core;
import arc.Events;
import arc.func.Cons;
import arc.func.Cons2;
import arc.math.Mathf;
import arc.scene.Element;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Blocks;
import mindustry.content.Items;
import mindustry.core.GameState;
import mindustry.game.EventType;
import mindustry.gen.Building;
import mindustry.gen.Call;
import mindustry.gen.Unit;
import mindustry.input.Binding;
import mindustry.squirrelModule.ui.MemorySlider;
import mindustry.type.Item;
import mindustry.type.ItemStack;
import mindustry.world.Block;
import mindustry.world.blocks.defense.turrets.ItemTurret;
import mindustry.world.blocks.production.GenericCrafter;
import mindustry.world.blocks.storage.CoreBlock;
import mindustry.world.blocks.units.Reconstructor;
import mindustry.world.blocks.units.UnitFactory;
import mindustry.world.consumers.ConsumeItems;

import static arc.Core.settings;
import static mindustry.Vars.*;

public class Hack {
    public static boolean noFog, useWindowedMenu;
    public static boolean randomUUID, randomUSID, simMobile;

    public static boolean immediatelyTurn, ignoreTurn, unitTrans, noKB, noHitbox, noSpawnKB, infDrag, immeMove;
    public static float KBMulti;
    public static boolean weaponImmeTurn, forceControl, holdFill, autoFill;
    public static int holdFillInterval, holdFillMinItem, autoFillInterval;
    public static long lastFillTime, lastAutoFillTime;
    static ObjectMap<Block, Item[]> fillIndexer = new ObjectMap<>();

    public static void init() {
        if (!settings.getBool("squirrel")) System.exit(0);
        Manager manager = ui.infoControl.manager;

        manager.register("显示", "noFog", new Config("强制透雾", null, changed(e -> noFog = e)));
        Events.run(EventType.Trigger.draw, () -> {
            if (noFog) state.rules.fog = false;
        });
        manager.register("显示", "hideHUD", new Config("隐藏HUD", null, changed(e -> ui.infoControl.toggle(!e))));
        manager.register("显示", "useWindowedMenu", new Config("窗口菜单", null, changed(e -> useWindowedMenu = e)));

        manager.register("多人", "randomUUID", new Config("随机UUID", null, changed(e -> randomUUID = e)));
        manager.register("多人", "randomUSID", new Config("随机USID", null, changed(e -> randomUSID = e)));
        manager.register("多人", "simMobile", new Config("伪装手机", null, changed(e -> simMobile = e)));

        manager.register("移动", "immediatelyTurn", new Config("瞬间转向", null, changed(e -> immediatelyTurn = e)));
        manager.register("移动", "ignoreTurn", new Config("无视旋转", null, changed(e -> ignoreTurn = e)));
        manager.register("移动", "unitTrans", new Config("单位平移", null, changed(e -> unitTrans = e)));
        manager.register("移动", "noHitbox", new Config("无视碰撞", null, changed(e -> noHitbox = e)));
        manager.register("移动", "noSpawnKB", new Config("无视刷怪圈", null, changed(e -> noSpawnKB = e)));
        manager.register("移动", "infDrag", new Config("立即停止", new Element[]{}, changed(e -> infDrag = e)));
        manager.register("移动", "immeMove", new Config("立即移动", new Element[]{}, changed(e -> immeMove = e)));
        manager.register("移动", "noKB", new Config("减少击退", new Element[]{new Label("减少百分比"), new MemorySlider("noKB", 0f, 1f, 0.01f, 50f, false)}, changedOrConfigure(e -> noKB = e, c -> KBMulti = ((Slider) c.element[1]).getValue(), (c, cb) -> {
            String s = Mathf.ceil(KBMulti * 100) + "%";
            cb.get(s);
            ((Label) c.element[0]).setText("减少百分比 " + s);
        })));

        manager.register("交互", "weaponImmeTurn", new Config("武器瞬间转向", null, changed(e -> weaponImmeTurn = e)));
        manager.register("交互", "forceControl", new Config("强制控制", null, changed(e -> forceControl = e)));
        manager.register("交互", "holdFill", new Config("按住装填", new Element[]{new Label(""), new MemorySlider("holdFill", 10f, 500f, 1f, 100f, false), new Label(""), new MemorySlider("holdFill2", 0f, 1000f, 1f, 500f, false)}, new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                holdFill = enabled;
            }

            @Override
            public void onConfigure() {
                holdFillInterval = Mathf.ceil(((Slider) config.element[1]).getValue());
                ((Label) config.element[0]).setText("间隔 " + holdFillInterval + "ms");
                holdFillMinItem = Mathf.ceil(((Slider) config.element[3]).getValue());
                ((Label) config.element[2]).setText("核心物资下限 " + holdFillMinItem);
            }

            @Override
            public String text() {
                return holdFillInterval + "ms";
            }
        }));
        manager.register("交互", "autoFill", new Config("自动装超速", new Element[]{new Label(""), new MemorySlider("autoFill", 10f, 1000f, 1f, 100f, false)}, new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                autoFill = enabled;
            }

            @Override
            public void onConfigure() {
                autoFillInterval = Mathf.ceil(((Slider) config.element[1]).getValue());
                ((Label) config.element[0]).setText("检测间隔 " + holdFillInterval + "ms");
            }

            @Override
            public String text() {
                return autoFillInterval + "ms";
            }
        }));
        initFill();

        manager.register("杂项", "noArcPacket", new Config("停发版本", null, changed(e -> settings.put("arcAnonymity", e))));
    }

    public static HackFunc changed(Cons<Boolean> func) {
        return new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                func.get(enabled);
            }
        };
    }

    public static HackFunc changedOrConfigure(Cons<Boolean> func1, Cons<Config> func2) {
        return new HackFunc() {
            @Override
            public void onChanged(boolean enabled) {
                func1.get(enabled);
            }

            @Override
            public void onConfigure() {
                func2.get(config);
            }
        };
    }

    public static HackFunc changedOrConfigure(Cons<Boolean> func1, Cons<Config> func2, Cons2<Config, Cons<String>> text) {
        return new HackFunc() {
            String tmp = "";
            @Override
            public void onChanged(boolean enabled) {
                func1.get(enabled);
            }

            @Override
            public void onConfigure() {
                func2.get(config);
            }

            @Override
            public String text() {
                text.get(config, s -> tmp = s);
                return tmp;
            }
        };
    }

    private static void initFill() {
        fillIndexer.put(Blocks.cyclone, new Item[]{Items.surgeAlloy, Items.plastanium, Items.blastCompound, Items.metaglass});
        fillIndexer.put(Blocks.swarmer, new Item[]{Items.surgeAlloy, Items.blastCompound, Items.pyratite});
        fillIndexer.put(Blocks.fuse, new Item[]{Items.thorium, Items.titanium});
        fillIndexer.put(Blocks.ripple, new Item[]{Items.plastanium, Items.silicon, Items.graphite, Items.blastCompound, Items.pyratite});
        fillIndexer.put(Blocks.duo, new Item[]{Items.copper, Items.graphite, Items.silicon});
        fillIndexer.put(Blocks.hail, new Item[]{Items.silicon, Items.graphite, Items.pyratite});
        fillIndexer.put(Blocks.salvo, new Item[]{Items.thorium, Items.copper, Items.silicon});
        fillIndexer.put(Blocks.scatter, new Item[]{Items.metaglass, Items.lead, Items.scrap});
        fillIndexer.put(Blocks.foreshadow, new Item[]{Items.surgeAlloy});
        fillIndexer.put(Blocks.spectre, new Item[]{Items.thorium, Items.graphite, Items.pyratite});
        Events.run(EventType.Trigger.update, () -> {
            if (!holdFill) return;
            if (state.getState() != GameState.State.playing) return;
            if (Time.millis() - lastFillTime < holdFillInterval) return;
            lastFillTime = Time.millis();
            if (!Core.input.keyDown(Binding.select)) return;
            if (Core.input.mouseWorld().sub(Tmp.v1.set(player.x, player.y)).len() > itemTransferRange) return;
            Unit unit = player.unit();
            if (unit.type.itemCapacity == 0) return;
            CoreBlock.CoreBuild core = player.closestCore();
            if (core == null) return;
            if (!unit.hasItem() && Tmp.v1.set(player.x, player.y).sub(Tmp.v2.set(core.x, core.y)).len() > itemTransferRange) return;
            Building build = world.tileWorld(Core.input.mouseWorld().x, Core.input.mouseWorld().y).build;
            if (build == null) return;
            Block type = build.block;
            if (type instanceof ItemTurret) {
                Item[] items = fillIndexer.get(type);
                if (items == null) return;
                if (unit.stack.amount != 0 && Seq.with(items).indexOf(unit.stack.item) != -1 && build.acceptItem(null, unit.stack.item)) {
                    Call.transferInventory(player, build);
                    return;
                }
                for (Item i : items) {
                    if (!core.items.has(i, holdFillMinItem)) continue;
                    if (!build.acceptItem(null, i)) continue;
                    if (unit.stack.amount != 0) {
                        Call.transferInventory(player, core);
                    }
                    Call.requestItem(player, core, i, unit.type.itemCapacity);
                    Call.transferInventory(player, build);
                    break;
                }
            }
            if (type instanceof UnitFactory || type instanceof GenericCrafter || type instanceof Reconstructor) {
                ItemStack[] items;
                if (type instanceof UnitFactory) {
                    int plan = ((UnitFactory.UnitFactoryBuild) build).currentPlan;
                    if (plan == -1) return;
                    items = ((UnitFactory) type).plans.get(plan).requirements;
                } else {
                    items = ((ConsumeItems) type.consumeBuilder.find(c -> c instanceof ConsumeItems)).items;
                }
                if (items == null) return;
                for (ItemStack i : items) {
                    if (build.items.has(i.item, i.amount)) continue;
                    if (!core.items.has(i.item, holdFillMinItem)) continue;
                    if (unit.stack.amount != 0) {
                        Call.transferInventory(player, core);
                    }
                    Call.requestItem(player, core, i.item, unit.type.itemCapacity);
                    Call.transferInventory(player, build);
                }
            }
        });
        Events.run(EventType.Trigger.update, () -> {
            if (!autoFill) return;
            if (state.getState() != GameState.State.playing) return;
            if (Time.millis() - lastAutoFillTime < autoFillInterval) return;
            lastAutoFillTime = Time.millis();
            //TODO
        });
    }
}
