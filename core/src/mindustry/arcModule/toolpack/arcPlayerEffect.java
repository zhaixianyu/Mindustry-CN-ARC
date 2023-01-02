package mindustry.arcModule.toolpack;

import arc.Core;
import arc.func.Cons;
import arc.graphics.Color;
import arc.graphics.g2d.Draw;
import arc.graphics.g2d.Lines;
import arc.math.Mathf;
import arc.scene.event.Touchable;
import arc.scene.ui.Dialog;
import arc.scene.ui.Label;
import arc.scene.ui.Slider;
import arc.scene.ui.layout.Table;
import arc.util.Strings;
import arc.util.Time;
import arc.util.Tmp;
import mindustry.content.Fx;
import mindustry.entities.Effect;
import mindustry.gen.Player;
import mindustry.gen.Unit;
import mindustry.graphics.Drawf;
import mindustry.graphics.Layer;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;

import java.lang.reflect.Field;

import static mindustry.Vars.*;

public class arcPlayerEffect {
    public static Effect playerEffect = Fx.none;
    public static float effectChance = 0;

    public static float effectTime = 0;
    private static Effect refEffect = Fx.none;

    public static void arcPlayerEffectSetting() {
        Dialog efTable = new BaseDialog("玩家特效设置");
        arcSliderTable(efTable.cont, "特效概率", effectChance, 0, 1, 0.01f, s -> effectChance = s);
        arcSliderTable(efTable.cont, "特效时长", playerEffect.lifetime, 0, 300, 5f, s -> {
            playerEffect.lifetime = s;
            effectTime = s;
        });
        efTable.cont.pane(tt -> {
            for (Field field : Fx.class.getDeclaredFields()) {
                if (field.getGenericType() != Effect.class) continue;
                Effect thisEffect;
                try {
                    thisEffect = (Effect) field.get(null);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                tt.check(field.getName(), refEffect == thisEffect, a -> {
                    playerEffect = thisEffect;
                    refEffect = thisEffect;
                    playerEffect.lifetime = effectTime;
                }).row();
            }

        });
        efTable.addCloseButton();
        efTable.buttons.button("保存", arcPlayerEffect::saveConfig);
        efTable.buttons.button("读取", arcPlayerEffect::readConfig);
        efTable.show();
    }

    private static void saveConfig() {
        Core.settings.put("arcPlayerEffectPro", effectChance);
        Core.settings.put("arcPlayerEffectTime", effectTime);
        Core.settings.put("arcPlayerEffectType", refEffect.id);
        ui.arcInfo("[cyan]已保存设置");
    }

    private static void readConfig() {
        effectChance = Core.settings.getFloat("arcPlayerEffectPro");
        refEffect = Effect.get(Core.settings.getInt("arcPlayerEffectType"));
        playerEffect = refEffect;
        playerEffect.lifetime = Core.settings.getFloat("arcPlayerEffectTime");
        ui.arcInfo("[cyan]已读取设置");
    }

    public static Table arcSliderTable(Table table, String name, float def, float min, float max, float step, Cons<Float> cons) {
        Slider slider = new Slider(min, max, step, false);

        slider.setValue(def);

        Label value = new Label("", Styles.outlineLabel);
        Table content = new Table();
        content.add(name, Styles.outlineLabel).left().growX().wrap();
        content.add(value).padLeft(10f).right();
        content.margin(3f, 33f, 3f, 33f);
        content.touchable = Touchable.disabled;

        slider.changed(() -> {
            cons.get(slider.getValue());
            value.setText(Strings.autoFixed(slider.getValue(), 2));
        });

        slider.change();

        table.stack(slider, content).width(Math.min(Core.graphics.getWidth() / 1.2f, 460f)).left().padTop(4f).get();
        table.row();
        return table;
    }

    public static void drawPlayerEffect(Unit unit) {
        Color effectColor = unit.controller() == player ? getPlayerEffectColor() : unit.team.color;

        boolean drawCircle = (unit.controller() == player && Core.settings.getInt("superUnitEffect") != 0) || (unit.controller() instanceof Player && Core.settings.getInt("superUnitEffect") == 2);
        if (drawCircle) {
            // 射程圈
            float curStroke = (float) Core.settings.getInt("playerEffectCurStroke") / 10f;

            float sectorRad = 0.14f, rotateSpeed = 0.5f;
            int sectors = 5;

            Lines.stroke(Lines.getStroke() * curStroke);

            Draw.z(Layer.effect - 2f);
            Draw.color(effectColor);

            Tmp.v1.trns(unit.rotation - 90, unit.x, unit.y).add(unit.x, unit.y);

            if (curStroke > 0) {
                for (int i = 0; i < sectors; i++) {
                    float rot = unit.rotation + i * 360f / sectors + Time.time * rotateSpeed;
                    Lines.arc(unit.x, unit.y, unit.type.maxRange, sectorRad, rot, (int) (50 + unit.type.maxRange / 10));
                }
            }
        }
        // 武器圈
        if (Core.settings.getInt("unitTargetType") > 0) {
            Draw.z(Layer.effect);
            Draw.color(effectColor, 0.8f);
            Lines.stroke(1f);
            Lines.line(unit.x, unit.y, unit.aimX, unit.aimY);
            switch (Core.settings.getInt("unitTargetType")) {
                case 1:
                    Lines.dashCircle(unit.aimX, unit.aimY, 8);
                    break;
                case 2:
                    Drawf.target(unit.aimX, unit.aimY, 6f, 0.7f, effectColor);
                    break;
                case 3:
                    Drawf.target2(unit.aimX, unit.aimY, 6f, 0.7f, effectColor);
                    break;
                case 4:
                    Drawf.targetc(unit.aimX, unit.aimY, 6f, 0.7f, effectColor);
                    break;
                case 5:
                    Drawf.targetd(unit.aimX, unit.aimY, 6f, 0.7f, effectColor);
                    break;
            }
        }

        // 特效轨迹
        if (Mathf.chanceDelta(effectChance)) playerEffect.at(unit.x, unit.y, effectColor);
        Draw.reset();
    }

}