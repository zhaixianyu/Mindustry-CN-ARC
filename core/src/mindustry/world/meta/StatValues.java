package mindustry.world.meta;

import arc.*;
import arc.func.*;
import arc.graphics.g2d.*;
import arc.math.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.struct.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.ctype.*;
import mindustry.entities.bullet.*;
import mindustry.gen.*;
import mindustry.maps.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.world.*;
import mindustry.world.blocks.defense.turrets.*;
import mindustry.world.blocks.environment.*;
import mindustry.world.blocks.production.BurstDrill;
import mindustry.world.blocks.production.Drill;
import mindustry.world.blocks.production.Separator;

import static mindustry.Vars.*;

/** Utilities for displaying certain stats in a table. */
public class StatValues{

    public static StatValue string(String value, Object... args){
        String result = Strings.format(value, args);
        return table -> table.add(result);
    }

    public static StatValue bool(boolean value){
        return table ->  table.add(!value ? "@no" : "@yes");
    }

    public static String fixValue(float value){
        int precision = Math.abs((int)value - value) <= 0.001f ? 0 : Math.abs((int)(value * 10) - value * 10) <= 0.001f ? 1 : 2;
        return Strings.fixed(value, precision);
    }

    public static StatValue squared(float value, StatUnit unit){
        return table -> {
            String fixed = fixValue(value);
            table.add(fixed + "x" + fixed);
            table.add((unit.space ? " " : "") + unit.localized());
        };
    }

    public static StatValue number(float value, StatUnit unit, boolean merge){
        return table -> {
            String l1 = (unit.icon == null ? "" : unit.icon + " ") + fixValue(value), l2 = (unit.space ? " " : "") + unit.localized();

            if(merge){
                table.add(l1 + l2);
            }else{
                table.add(l1);
                table.add(l2);
            }
        };
    }

    public static StatValue number(float value, StatUnit unit){
        return number(value, unit, false);
    }

    public static StatValue liquid(Liquid liquid, float amount, boolean perSecond){
        return table -> table.add(new LiquidDisplay(liquid, amount, perSecond));
    }

    public static StatValue liquids(Boolf<Liquid> filter, float amount, boolean perSecond){
        return table -> {
            Seq<Liquid> list = content.liquids().select(i -> filter.get(i) && i.unlockedNow() && !i.isHidden());

            for(int i = 0; i < list.size; i++){
                table.add(new LiquidDisplay(list.get(i), amount, perSecond)).padRight(5);

                if(i != list.size - 1){
                    table.add("/");
                }
            }
        };
    }

    public static StatValue liquids(float timePeriod, LiquidStack... stacks){
        return liquids(timePeriod, true, stacks);
    }

    public static StatValue liquids(float timePeriod, boolean perSecond, LiquidStack... stacks){
        return table -> {
            for(var stack : stacks){
                table.add(new LiquidDisplay(stack.liquid, stack.amount * (60f / timePeriod), perSecond)).padRight(5);
            }
        };
    }

    public static StatValue items(ItemStack... stacks){
        return items(true, stacks);
    }

    public static StatValue items(boolean displayName, ItemStack... stacks){
        return table -> {
            for(ItemStack stack : stacks){
                table.add(new ItemDisplay(stack.item, stack.amount, displayName)).padRight(5);
            }
        };
    }

    public static StatValue items(float timePeriod, ItemStack... stacks){
        return table -> {
            for(ItemStack stack : stacks){
                table.add(new ItemDisplay(stack.item, stack.amount, timePeriod, true)).padRight(5);
            }
        };
    }

    public static StatValue items(Boolf<Item> filter){
        return items(-1, filter);
    }

    public static StatValue items(float timePeriod, Boolf<Item> filter){
        return table -> {
            Seq<Item> list = content.items().select(i -> filter.get(i) && i.unlockedNow() && !i.isHidden());

            for(int i = 0; i < list.size; i++){
                Item item = list.get(i);

                table.add(timePeriod <= 0 ? new ItemDisplay(item) : new ItemDisplay(item, 1, timePeriod, true)).padRight(5);

                if(i != list.size - 1){
                    table.add("/");
                }
            }
        };
    }

    public static StatValue content(UnlockableContent content){
        return table -> {
            table.add(new Image(content.uiIcon)).size(iconSmall).padRight(3);
            table.add(content.localizedName).padRight(3);
        };
    }

    public static StatValue blockEfficiency(Block floor, float multiplier, boolean startZero){
        return table -> table.stack(
            new Image(floor.uiIcon).setScaling(Scaling.fit),
            new Table(t -> t.top().right().add((multiplier < 0 ? "[scarlet]" : startZero ? "[accent]" : "[accent]+") + (int)((multiplier) * 100) + "%").style(Styles.outlineLabel))
        ).maxSize(64f);
    }

    public static StatValue blocks(Attribute attr, boolean floating, float scale, boolean startZero){
        return blocks(attr, floating, scale, startZero, true);
    }

    public static StatValue blocks(Attribute attr, boolean floating, float scale, boolean startZero, boolean checkFloors){
        return table -> table.table(c -> {
            Runnable[] rebuild = {null};
            Map[] lastMap = {null};

            rebuild[0] = () -> {
                c.clearChildren();
                c.left();

                if(state.isGame()){
                    var blocks = Vars.content.blocks()
                    .select(block -> (!checkFloors || block instanceof Floor) && block.attributes.get(attr) != 0 && !((block instanceof Floor f && f.isDeep()) && !floating))
                    .with(s -> s.sort(f -> f.attributes.get(attr)));

                    if(blocks.any()){
                        int i = 0;
                        for(var block : blocks){

                            blockEfficiency(block, block.attributes.get(attr) * scale, startZero).display(c);
                            if(++i % 5 == 0){
                                c.row();
                            }
                        }
                    }else{
                        c.add("@none.inmap");
                    }
                }else{
                    c.add("@stat.showinmap");
                }
            };

            rebuild[0].run();

            //rebuild when map changes.
            c.update(() -> {
                Map current = state.isGame() ? state.map : null;

                if(current != lastMap[0]){
                    rebuild[0].run();
                    lastMap[0] = current;
                }
            });
        });
    }
    public static StatValue content(Seq<UnlockableContent> list){
        return content(list, i -> true);
    }

    public static <T extends UnlockableContent> StatValue content(Seq<T> list, Boolf<T> check){
        return table -> table.table(l -> {
            l.left();

            boolean any = false;
            for(int i = 0; i < list.size; i++){
                var item = list.get(i);

                if(!check.get(item)) continue;
                any = true;

                if(item.uiIcon.found()) l.image(item.uiIcon).size(iconSmall).padRight(2).padLeft(2).padTop(3).padBottom(3);
                l.add(item.localizedName).left().padLeft(1).padRight(4).colspan(item.uiIcon.found() ? 1 : 2);
                if(i % 5 == 4){
                    l.row();
                }
            }

            if(!any){
                l.add("@none.inmap");
            }
        });
    }

    public static StatValue drillBlock(Drill drill){
        Seq<Block> list = content.blocks().select(b -> b instanceof Floor f && !f.wallOre && f.itemDrop != null && f.itemDrop.hardness <= drill.tier && f.itemDrop != drill.blockedItem);
        list.sort(t->t.itemDrop.hardness);
        if(drill instanceof BurstDrill){return table -> table.table(l -> {
            l.left();
            for(int i = 0; i < list.size; i++){
                var item = list.get(i);

                l.image(item.uiIcon).size(iconSmall).padRight(2).padLeft(2).padTop(3).padBottom(3);
                l.add(item.localizedName).left().padLeft(1).padRight(4);
                if(i % 5 == 4){
                    l.row();
                }
            }
        });
        }
        else{
        return table -> table.table(l -> {
            l.left();
            StringBuilder blockInfo = new StringBuilder();
            for(int i = 0; i < list.size; i++){
                var block = list.get(i);
                blockInfo.append(block.emoji()).append(" ").append(block.localizedName);

                if(i == list.size-1||list.get(i+1).itemDrop.hardness!=list.get(i).itemDrop.hardness){
                    float eff = 60f / (drill.drillTime + drill.hardnessDrillMultiplier * list.get(i).itemDrop.hardness) * drill.size * drill.size;
                    blockInfo.append("    [stat]<").append(Strings.autoFixed(eff,2)).append("|[cyan]");
                    blockInfo.append(Strings.autoFixed(eff * drill.liquidBoostIntensity * drill.liquidBoostIntensity,2)).append("[stat]>");
                    l.add(blockInfo.toString()).left().row();
                    blockInfo = new StringBuilder();
                }
                else blockInfo.append("  ");

            }
        });}
    }

    public static StatValue arcSeparator(Separator separator){
        return table -> {
            for(ItemStack stack : separator.results){
                table.add(new ItemDisplay(stack.item, stack.amount, true)).padRight(5);
                table.add("  ");
            }
        };
    }

    public static StatValue drillUnit(UnitType unit){
        Seq<Block> list = content.blocks().select(b ->
                b.itemDrop != null &&
                        (b instanceof Floor f && (((f.wallOre && unit.mineWalls) || (!f.wallOre && unit.mineFloor))) ||
                                (!(b instanceof Floor) && unit.mineWalls)) &&
                        b.itemDrop.hardness <= unit.mineTier && (!b.playerUnmineable || Core.settings.getBool("doubletapmine")));
        list.sort(t->t.itemDrop.hardness);
        if(unit.mineHardnessScaling){
            return table -> table.table(l -> {
                l.left();
                StringBuilder blockInfo = new StringBuilder();
                for (int i = 0; i < list.size; i++) {
                    var block = list.get(i);
                    blockInfo.append(block.emoji()).append(" ").append(block.localizedName);

                    if (i == list.size - 1 || list.get(i + 1).itemDrop.hardness != list.get(i).itemDrop.hardness) {
                        float eff = 60f * unit.mineSpeed / (50f + list.get(i).itemDrop.hardness * 15f);
                        blockInfo.append("    [stat]<").append(Strings.autoFixed(eff, 2)).append(">");
                        l.add(blockInfo.toString()).left().row();
                        blockInfo = new StringBuilder();
                    } else blockInfo.append("  ");
                }
            });
        }
        else {
            return table -> table.table(l -> {
                l.left();
                StringBuilder blockInfo = new StringBuilder();
                for (int i = 0; i < list.size; i++) {
                    var item = list.get(i);
                    l.image(item.uiIcon).size(iconSmall).padRight(2).padLeft(2).padTop(3).padBottom(3);
                    l.add(item.localizedName).left().padLeft(1).padRight(4);
                    if(i % 5 == 4){
                        l.row();
                    }
                }
                float eff = 60f * unit.mineSpeed / (50f + 15f);
                blockInfo.append("    <").append(Strings.autoFixed(eff, 2)).append("[white]>");
                l.add(blockInfo.toString()).left();
            });
        }
    }

    public static StatValue blocks(Boolf<Block> pred){
        return content(content.blocks(), pred);
    }

    public static StatValue blocks(Seq<Block> list){
        return content(list.as());
    }

    public static StatValue statusEffects(Seq<StatusEffect> list){
        return content(list.as());
    }

    public static StatValue boosters(float reload, float maxUsed, float multiplier, boolean baseReload, Boolf<Liquid> filter){
        return table -> {
            table.row();
            table.table(c -> {
                for(Liquid liquid : content.liquids()){
                    if(!filter.get(liquid)) continue;

                    c.image(liquid.uiIcon).size(3 * 8).scaling(Scaling.fit).padRight(4).right().top();
                    c.add(liquid.localizedName).padRight(10).left().top();
                    c.table(Tex.underline, bt -> {
                        bt.left().defaults().padRight(3).left();

                        float reloadRate = (baseReload ? 1f : 0f) + maxUsed * multiplier * liquid.heatCapacity;
                        float standardReload = baseReload ? reload : reload / (maxUsed * multiplier * 0.4f);
                        float result = standardReload / (reload / reloadRate);
                        bt.add(Core.bundle.format("bullet.reload", Strings.autoFixed(result, 2)));
                    }).left().padTop(-9);
                    c.row();
                }
            }).colspan(table.getColumns());
            table.row();
        };
    }

    public static StatValue strengthBoosters(float multiplier, Boolf<Liquid> filter){
        return table -> {
            table.row();
            table.table(c -> {
                for(Liquid liquid : content.liquids()){
                    if(!filter.get(liquid)) continue;

                    c.image(liquid.uiIcon).size(3 * 8).scaling(Scaling.fit).padRight(4).right().top();
                    c.add(liquid.localizedName).padRight(10).left().top();
                    c.table(Tex.underline, bt -> {
                        bt.left().defaults().padRight(3).left();

                        float newRate = (1f + multiplier * liquid.heatCapacity);
                        bt.add(Core.bundle.format("bar.strength", Strings.autoFixed(newRate, 2)));
                    }).left().padTop(-9);
                    c.row();
                }
            }).colspan(table.getColumns());
            table.row();
        };
    }

    public static StatValue weapons(UnitType unit, Seq<Weapon> weapons){
        return table -> {
            table.row();
            for(int i = 0; i < weapons.size;i ++){
                Weapon weapon = weapons.get(i);

                if(weapon.flipSprite || !weapon.hasStats(unit)){
                    //flipped weapons are not given stats
                    continue;
                }

                TextureRegion region = !weapon.name.equals("") && weapon.region.found() ? weapon.region : Core.atlas.find("clear");

                table.image(region).size(60).scaling(Scaling.bounded).right().top();

                table.table(Tex.underline, w -> {
                    w.left().defaults().padRight(3).left();

                    weapon.addStats(unit, w);
                }).padTop(-9).left();
                table.row();
            }
        };
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map){
        return ammo(map, 0, false);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, boolean showUnit){
        return ammo(map, 0, showUnit);
    }

    public static <T extends UnlockableContent> StatValue ammo(ObjectMap<T, BulletType> map, int indent, boolean showUnit){
        return table -> {

            table.row();

            var orderedKeys = map.keys().toSeq();
            orderedKeys.sort();

            for(T t : orderedKeys){
                boolean compact = t instanceof UnitType && !showUnit || indent > 0;

                BulletType type = map.get(t);

                if(type.spawnUnit != null && type.spawnUnit.weapons.size > 0){
                    ammo(ObjectMap.of(t, type.spawnUnit.weapons.first().bullet), indent, false).display(table);
                    return;
                }

                //no point in displaying unit icon twice
                if(!compact && !(t instanceof Turret)){
                    table.image(icon(t)).size(3 * 8).padRight(4).right().top();
                    table.add(t.localizedName).padRight(10).left().top();
                }

                table.table(bt -> {
                    bt.left().defaults().padRight(3).left();

                    if (type instanceof LightningBulletType) {
                        lightning(0, type.damage, type.lightningLength, type.lightningLengthRand).display(bt);
                    }
                    else if(type.damage > 0 && (type.collides || type.splashDamage <= 0)){
                        if(type.continuousDamage() > 0){
                            bt.add(Core.bundle.format("bullet.damage", type.continuousDamage()) + StatUnit.perSecond.localized());
                        }else{
                            bt.add(Core.bundle.format("bullet.damage", type.damage));
                        }
                    }

                    if(type.buildingDamageMultiplier != 1){
                        sep(bt, Core.bundle.format("bullet.buildingdamage", (int)(type.buildingDamageMultiplier * 100)));
                    }

                    if(type.rangeChange != 0 && !compact){
                        sep(bt, Core.bundle.format("bullet.range", (type.rangeChange > 0 ? "+" : "-") + Strings.autoFixed(type.rangeChange / tilesize, 1)));
                    }

                    if(type.splashDamage > 0){
                        sep(bt, Core.bundle.format("bullet.splashdamage", (int)type.splashDamage, Strings.fixed(type.splashDamageRadius / tilesize, 1)));
                    }

                    if(!compact && !Mathf.equal(type.ammoMultiplier, 1f) && type.displayAmmoMultiplier && (!(t instanceof Turret turret) || turret.displayAmmoMultiplier)){
                        sep(bt, Core.bundle.format("bullet.multiplier", (int)type.ammoMultiplier));
                    }

                    if(!compact && !Mathf.equal(type.reloadMultiplier, 1f)){
                        sep(bt, Core.bundle.format("bullet.reload", Strings.autoFixed(type.reloadMultiplier, 2)));
                    }

                    if(type.knockback > 0){
                        sep(bt, Core.bundle.format("bullet.knockback", Strings.autoFixed(type.knockback, 2)));
                    }

                    if(type.healPercent > 0f){
                        sep(bt, Core.bundle.format("bullet.healpercent", Strings.autoFixed(type.healPercent, 2)));
                    }

                    if(type.healAmount > 0f){
                        sep(bt, Core.bundle.format("bullet.healamount", Strings.autoFixed(type.healAmount, 2)));
                    }

                    if((type.pierce || type.pierceCap != -1) && !(type instanceof PointLaserBulletType)){
                        boolean laserPierce = type instanceof LaserBulletType || type instanceof ContinuousLaserBulletType || type instanceof ShrapnelBulletType;
                        boolean pierceBuilding = laserPierce || type instanceof ContinuousFlameBulletType || type instanceof RailBulletType || type.pierceBuilding;
                        boolean pierceUnit = type.pierce;
                        boolean infinitePierce = type.pierceCap == -1;
                        String str = "[stat]";
                        if (infinitePierce && !(type instanceof RailBulletType)) {
                            str += "无限";
                        }
                        str += "穿透";
                        if (pierceBuilding && pierceUnit) {
                            str += "建筑与单位";
                        }
                        else {
                            str += pierceBuilding ? "建筑" : "单位";
                        }
                        if (!infinitePierce && !(type instanceof RailBulletType)) {
                            str += type.pierceCap + "次";
                        }

                        sep(bt, str);
                        if (laserPierce) {
                            sep(bt, " [stat]会被塑钢墙阻挡");
                        }
                        if (type instanceof RailBulletType rail) {
                            sep(bt, " [stat]" + Strings.autoFixed(rail.pierceDamageFactor * 100f, 1) + "%[lightgray]衰减");
                        }
                    }

                    if(type.incendAmount > 0){
                        sep(bt, "@bullet.incendiary");
                    }

                    if(type.homingPower > 0.01f){
                        sep(bt, "[stat]追踪[lightgray]~[]"+Strings.autoFixed(type.homingPower * 50 * Time.toSeconds, 1)+"°/s[lightgray]~[]"+Strings.fixed(type.homingRange / tilesize,1)+"[lightgray]格");
                    }

                    if(!(type instanceof LightningBulletType) && type.lightning > 0){
                        lightning(type.lightning, type.lightningDamage < 0 ? type.damage : type.lightningDamage, type.lightningLength, type.lightningLengthRand).display(bt);
                    }

                    if(type.pierceArmor){
                        sep(bt, "@bullet.armorpierce");
                    }

                    if(type.status != StatusEffects.none){
                        sep(bt, (type.status.minfo.mod == null ? type.status.emoji() : "") + "[stat]" + type.status.localizedName + (type.status.reactive? "":"[lightgray]~[]" + Strings.autoFixed(type.statusDuration/60f,2)+"[lightgray]s"));
                    }

                    if (type.suppressionRange > -1f) {
                        sep(bt, "[lightgray]压制场~[stat]" + type.suppressionRange / 8f + "[]格~[stat]" + type.suppressionDuration / 60f + "[]秒");
                    }

                    if(type.fragBullet != null){
                        sep(bt, Core.bundle.format("bullet.frags", type.fragBullets));
                        bt.row();

                        ammo(ObjectMap.of(t, type.fragBullet), indent + 1, false).display(bt);
                    }

                    if (type.intervalBullet != null) {
                        sep(bt, "[lightgray]每[stat]" + Strings.autoFixed(type.bulletInterval / 60f, 2) + "[]秒生成[stat]" + type.intervalBullets + "[]x子弹：");
                        bt.row();
                        ammo(ObjectMap.of(t, type.intervalBullet), indent + 1, false).display(bt);
                    }

                    Seq<BulletType> spawn = type.spawnBullets.copy();
                    while (spawn.any()) {//显示所有spawnBullets
                        BulletType bullet = spawn.first();
                        Boolf<BulletType> pred = b -> bullet.damage == b.damage && bullet.splashDamage == b.splashDamage;
                        //通过pred的的子弹被认为和当前子弹是一样的，合并显示
                        sep(bt, "[stat]" + spawn.count(pred) + "x[lightgray]生成子弹：");
                        bt.row();
                        ammo(ObjectMap.of(t, bullet), indent + 1, false).display(bt);
                        spawn.removeAll(pred);//删除已经显示的子弹
                    }
                }).padTop(compact ? 0 : -9).padLeft(indent * 12).left().get().background(compact ? null : Tex.underline);

                table.row();
            }
        };
    }

    public static StatValue lightning(int shots, float damage, int length, int lengthRand) {
        return table -> {
            String str = "[lightgray]";
            if (shots > 0) {
                str += String.format("[stat]%d[]x", shots);
            }
            str += String.format("闪电~[stat]%s[]伤害~", Strings.autoFixed(damage, 1));
            if (lengthRand > 0) {
                str += String.format("[stat]%d~%d[]长度", length, length + lengthRand);
            }
            else {
                str += String.format("[stat]%d[]长度", length);
            }
            sep(table, str);
        };
    }

    public static StatValue turretReload(Turret turret) {
        return table -> {
            table.add(turret.shoot.totalShots() + " x " + Strings.autoFixed(60f / turret.reload, 1) + "/s");
        };
    }

    //for AmmoListValue
    private static void sep(Table table, String text){
        table.row();
        table.add(text);
    }

    private static TextureRegion icon(UnlockableContent t){
        return t.uiIcon;
    }
}
