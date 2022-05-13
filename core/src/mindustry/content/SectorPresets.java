package mindustry.content;

import arc.math.geom.*;
import mindustry.game.MapObjectives.*;
import mindustry.game.*;
import mindustry.graphics.*;
import mindustry.type.*;

import static mindustry.content.Planets.*;

public class SectorPresets{
    public static SectorPreset
    groundZero,
    craters, biomassFacility, frozenForest, ruinousShores, windsweptIslands, stainedMountains, tarFields,
    fungalPass, extractionOutpost, saltFlats, overgrowth,
    impact0078, desolateRift, nuclearComplex, planetaryTerminal,
    coastline, navalFortress,

    onset, two, three, four
    ;

    public static void load(){
        //region serpulo

        groundZero = new SectorPreset("groundZero", serpulo, 15){{
            alwaysUnlocked = true;
            addStartingItems = true;
            captureWave = 10;
            difficulty = 1;
            startWaveTimeMultiplier = 3f;
        }};

        saltFlats = new SectorPreset("saltFlats", serpulo, 101){{
            difficulty = 5;
        }};

        frozenForest = new SectorPreset("frozenForest", serpulo, 86){{
            captureWave = 15;
            difficulty = 2;
        }};

        biomassFacility = new SectorPreset("biomassFacility", serpulo, 81){{
            captureWave = 20;
            difficulty = 3;
        }};

        craters = new SectorPreset("craters", serpulo, 18){{
            captureWave = 20;
            difficulty = 2;
        }};

        ruinousShores = new SectorPreset("ruinousShores", serpulo, 213){{
            captureWave = 30;
            difficulty = 3;
        }};

        windsweptIslands = new SectorPreset("windsweptIslands", serpulo, 246){{
            captureWave = 30;
            difficulty = 4;
        }};

        stainedMountains = new SectorPreset("stainedMountains", serpulo, 20){{
            captureWave = 30;
            difficulty = 3;
        }};

        extractionOutpost = new SectorPreset("extractionOutpost", serpulo, 165){{
            difficulty = 5;
        }};

        coastline = new SectorPreset("coastline", serpulo, 108){{
            captureWave = 30;
            difficulty = 5;
        }};

        navalFortress = new SectorPreset("navalFortress", serpulo, 216){{
            difficulty = 9;
        }};

        fungalPass = new SectorPreset("fungalPass", serpulo, 21){{
            difficulty = 4;
        }};

        overgrowth = new SectorPreset("overgrowth", serpulo, 134){{
            difficulty = 5;
        }};

        tarFields = new SectorPreset("tarFields", serpulo, 23){{
            captureWave = 40;
            difficulty = 5;
        }};

        impact0078 = new SectorPreset("impact0078", serpulo, 227){{
            captureWave = 45;
            difficulty = 7;
        }};

        desolateRift = new SectorPreset("desolateRift", serpulo, 123){{
            captureWave = 18;
            difficulty = 8;
        }};

        nuclearComplex = new SectorPreset("nuclearComplex", serpulo, 130){{
            captureWave = 50;
            difficulty = 7;
        }};

        planetaryTerminal = new SectorPreset("planetaryTerminal", serpulo, 93){{
            difficulty = 10;
        }};

        //endregion
        //region erekir

        onset = new SectorPreset("onset", erekir, 10){{
            addStartingItems = true;
            alwaysUnlocked = true;
            difficulty = 1;

            rules = r -> {
                r.objectives.addAll(
                    new ItemObjective(Items.beryllium, 15).withMarkers(
                        new ShapeTextMarker("点击墙壁上的[accent]矿物[]以手动开采。", 290f * 8f, 106f * 8f)
                    ),
                    new BuildCountObjective(Blocks.turbineCondenser, 1).withMarkers(
                        new ShapeTextMarker("打开科技树。\n研究[accent]涡轮冷凝器[]，并放置在喷口上。\n它可以产生[accent]电力[]。", 289f * 8f, 116f * 8f, 8f * 2.6f, 0f, 9f)
                    ),
                    new BuildCountObjective(Blocks.plasmaBore, 1).withMarkers(
                        new ShapeTextMarker("研究并放置[accent]光束钻头[]。 \n它可以自动挖掘墙上的资源。", 293.5f * 8f, 113.5f * 8f, 4f * 2.6f, 45f, 60f)
                    ),
                    new BuildCountObjective(Blocks.beamNode, 1).withMarkers(
                        new ShapeTextMarker("为了给光束钻头提供[accent]电力[]，研究并放置一个[accent]光束节点[]。\n它可以从涡轮冷凝器向光束钻头传输电力。", 294f * 8f, 116f * 8f)
                    ),
                    new CoreItemObjective(Items.beryllium, 5).withMarkers(
                        new TextMarker("研究并放置[accent]物品管道[]来把光束钻头挖掘的矿物运输至核心中。", 285f * 8f, 108f * 8f)
                    ),
                    new CoreItemObjective(Items.beryllium, 200).withMarkers(
                        new TextMarker("扩大挖掘规模。\n放置更多的光束钻头，并用电力节点与物品管道来使它们正常工作。\n挖掘200铍。", 280f * 8f, 118f * 8f)
                    ),
                    new CoreItemObjective(Items.graphite, 100).withMarkers(
                        new TextMarker("要建造更高级的建筑，需要[accent]石墨[]。\n使用光束钻头挖掘石墨。", 261f * 8f, 108f * 8f)
                    ),
                    new ResearchObjective(Blocks.siliconArcFurnace).withMarkers(
                        new TextMarker("开始研究[accent]工厂[]。\n研究[accent]墙体粉碎机[]与[accent]电弧冶硅炉[]。", 268f * 8f, 101f * 8f)
                    ),
                    new CoreItemObjective(Items.silicon, 50).withMarkers(
                        new TextMarker("电弧冶硅炉需要输入[accent]沙[]与[accent]石墨[]来冶炼[accent]硅[]。\n它也需要[accent]电力[]。", 268f * 8f, 101f * 8f),
                        new TextMarker("使用[accent]墙体粉碎机[]挖掘沙。", 262f * 8f, 88f * 8f)
                    ),
                    new BuildCountObjective(Blocks.tankFabricator, 1).withMarkers(
                        new TextMarker("使用[accent]单位[]探索地图，进行防御，发动攻击。\n 研究并放置一个[accent]坦克制造厂[]。", 258f * 8f, 116f * 8f)
                    ),
                    new UnitCountObjective(UnitTypes.stell, 1).withMarkers(
                        new TextMarker("制造单位。\n点击\"?\"以显示制造单位所需资源。", 258f * 8f, 116f * 8f)
                    ),
                    new CommandModeObjective().withMarkers(
                        new TextMarker("按住[accent]shift[]键进入[accent]指挥模式[]。\n[accent]按住鼠标左键框选[]单位\n[accent]右键[]指挥选中的单位移动或攻击。", 258f * 8f, 116f * 8f)
                    ),
                    new BuildCountObjective(Blocks.breach, 1).withMarkers(
                        new TextMarker("使用单位防御很有效，但是有效使用[accent]炮塔[]可以提供更好的防御力。\n 放置一个[accent]撕裂[]。\n炮塔需要[accent]弹药[]供应。", 258f * 8f, 114f * 8f)
                    ),
                    new BuildCountObjective(Blocks.berylliumWall, 6).withMarkers(
                        new TextMarker("[accent]墙[]可以防止炮塔受到伤害。\n在炮塔周围放置一些[accent]铍墙[]。", 276f * 8f, 133f * 8f)
                    ),
                    new TimerObjective("@objective.enemiesapproaching",30 * 60).withMarkers(
                        new TextMarker("敌人即将来袭，准备好进行防御。", 276f * 8f, 133f * 8f)
                    ).withFlags("defStart"),
                    new DestroyUnitsObjective(2).withFlags("defDone"),
                    new DestroyBlockObjective(Blocks.coreBastion , 288, 198, Team.malis).withMarkers(
                        new TextMarker("敌军基地十分脆弱。发动反攻。", 276f * 8f, 133f * 8f)
                    ),
                    new BuildCountObjective(Blocks.coreBastion, 1).withMarkers(
                        new ShapeTextMarker("你可以在[accent]核心地块[]上建造新的核心。\n新核心的功能类似于前沿基地，且与其他核心共享资源仓库。\n放置一个核心。", 287.5f * 8f, 197.5f * 8f, 9f * 2.6f, 0f, 12f)
                    ),
                    new TimerObjective("[accent]设立防御：[lightgray] {0}", 120 * 60).withMarkers(
                        new TextMarker("敌军将在2分钟内发现你。\n设立防御，挖掘矿物，并建造生产设施。", 288f * 8f, 202f * 8f)
                    ).withFlags("openMap")
                );
            };
        }};

        two = new SectorPreset("two", erekir, 88){{
            difficulty = 3;

            rules = r -> {
                r.objectives.addAll(
                    new TimerObjective("[lightgray]侦测到敌人：[] [accent]{0}", 7 * 60 * 60).withMarkers(
                        new TextMarker("敌人将在7分钟内开始生产单位。", 276f * 8f, 164f * 8f)
                    ).withFlags("beginBuilding"),
                    new ProduceObjective(Items.tungsten).withMarkers(
                        new ShapeTextMarker("[accent]冲击钻头[]可以开采钨矿。\n冲击钻头需要[accent]水[]和[accent]电力[]才能工作。", 220f * 8f, 181f * 8f)
                    ),
                    new DestroyBlockObjective(Blocks.largeShieldProjector, 210, 278, Team.malis).withMarkers(
                        new TextMarker("敌人被护盾保护着。\n在该区块中探测到一台实验性的护盾破坏器。\n找到它，并输入钨启动它。", 276f * 8f, 164f * 8f),
                        new MinimapMarker(23f, 137f, Pal.accent)
                    )
                );
            };
        }};

        three = new SectorPreset("three", erekir, 36){{
            difficulty = 5;

            captureWave = 9;
        }};

        four = new SectorPreset("four", erekir, 29){{
            difficulty = 6;

            rules = r -> {
                float rad = 52f;
                r.objectives.addAll(
                new DestroyBlocksObjective(Blocks.coreBastion, Team.malis, Point2.pack(290,501), Point2.pack(158,496))
                .withFlags("nukeannounce"),
                new TimerObjective("@objective.nuclearlaunch", 8 * 60 * 60).withMarkers(
                new MinimapMarker(338, 377, rad, 14f, Pal.remove),
                new ShapeMarker(338 * 8, 377 * 8f){{
                    radius = rad * 8f;
                    fill = true;
                    color = Pal.remove.cpy().mul(0.8f).a(0.3f);
                    sides = 90;
                }},
                new ShapeMarker(338 * 8, 377 * 8f){{
                    radius = rad * 8f;
                    color = Pal.remove;
                    sides = 90;
                }}
                ).withFlags("nuke1")
                );
            };
        }};

        //endregion
    }
}
