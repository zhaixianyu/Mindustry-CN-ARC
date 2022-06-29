package mindustry.arcModule.ui.auxilliary;

import arc.*;
import arc.scene.ui.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.*;
import mindustry.content.*;
import mindustry.core.*;
import mindustry.editor.*;
import mindustry.game.*;
import mindustry.game.EventType.*;
import mindustry.gen.*;
import mindustry.type.*;
import mindustry.ui.*;
import mindustry.ui.dialogs.*;

import static arc.Core.settings;
import static mindustry.Vars.*;
import static mindustry.arcModule.ui.RStyles.*;

public class WaveInfoTable extends BaseToolsTable{
    private float fontScl = 0.8f;

    private int waveOffset = 0;

    private Table waveInfo = new Table();

    private arcWaveInfoDialog arcWaveInfoDialog = new arcWaveInfoDialog();

    public WaveInfoTable(){
        super(Icon.waves);

        Events.on(WorldLoadEvent.class, e -> {
            waveOffset = 0;
            rebuildWaveInfo();
        });

        Events.on(WaveEvent.class, e -> {
            rebuildWaveInfo();
        });
    }

    @Override
    protected void setup(){
        button(Icon.waves, clearAccentNonei, () -> {
            arcWaveInfoDialog.show();
        }).size(40).tooltip("波次信息");

        table(buttons -> {
            buttons.defaults().size(40);

            buttons.button("<", clearLineNonet, () -> {
                waveOffset -= 1;
                if(state.wave + waveOffset - 1 < 0) waveOffset = -state.wave + 1;
                rebuildWaveInfo();
            });

            buttons.button("O", clearLineNonet, () -> {
                waveOffset = 0;
                rebuildWaveInfo();
            });

            buttons.button(">", clearLineNonet, () -> {
                waveOffset += 1;
                rebuildWaveInfo();
            });

            buttons.button("Go", clearLineNonet, () -> {
                state.wave += waveOffset;
                waveOffset = 0;
                rebuildWaveInfo();
            });

            buttons.button("♐", clearLineNonet, () -> {
                String message = arcShareWaveInfo(state.wave + waveOffset);
                int seperator = 145;
                for(int i = 0; i < message.length() / (float)seperator; i++){
                    Call.sendChatMessage(message.substring(i * seperator, Math.min(message.length(), (i + 1) * seperator)));
                }
            }).get().setDisabled(() -> !state.rules.waves && !settings.getBool("arcShareWaveInfo"));

        }).left().row();

        table(setWave -> {
            setWave.label(() -> "" + (state.wave + waveOffset)).get().setFontScale(fontScl);

            setWave.row();

            setWave.button(Icon.settingsSmall, clearAccentNonei, 30, () -> {
                Dialog lsSet = new BaseDialog("波次设定");
                lsSet.cont.add("设定查询波次").padRight(5f).left();
                TextField field = lsSet.cont.field(state.wave + waveOffset + "", text -> {
                    waveOffset = Integer.parseInt(text) - state.wave;
                }).size(320f, 54f).valid(Strings::canParsePositiveInt).maxTextLength(100).get();
                lsSet.cont.row();
                lsSet.cont.slider(1, new arcWaveInfoDialog().calWinWave(), 1, res -> {
                    waveOffset = (int)res - state.wave;
                    field.setText((int)res + "");
                });
                lsSet.addCloseButton();
                lsSet.show();
            });
        });

        add(waveInfo).left();
    }

    private void rebuildWaveInfo(){
        waveInfo.clear();

        int curInfoWave = state.wave - 1 + waveOffset;
        for(SpawnGroup group : state.rules.spawns){
            int amount = group.getSpawned(curInfoWave);

            if(amount > 0){
                float shield = group.getShield(curInfoWave);
                StatusEffect effect = group.effect;

                waveInfo.table(groupT -> {
                    groupT.image(group.type.uiIcon).size(20).row();

                    groupT.add("" + amount, fontScl).center().row();

                    groupT.add((shield > 0 ? UI.formatAmount((long)shield) : ""), fontScl).center().row();

                    if(effect != null && effect != StatusEffects.none){
                        groupT.image(effect.uiIcon).size(20);
                    }
                }).padLeft(4).top();

            }
        }
    }

    private String arcShareWaveInfo(int waves){
        if(!state.rules.waves) return " ";
        StringBuilder builder = new StringBuilder();
        builder.append("[ARC").append(arcVersion).append("]");
        builder.append("标记了第").append(waves).append("波");
        if(waves < state.wave){
            builder.append("。");
        }else{
            if(waves > state.wave){
                builder.append("，还有").append(waves - state.wave).append("波");
            }
            int timer = (int)(state.wavetime + (waves - state.wave) * state.rules.waveSpacing);
            builder.append("[").append(fixedTime(timer)).append("]。");
        }

        if(state.rules.attackMode){
            int sum = Math.max(state.teams.present.sum(t -> t.team != player.team() ? t.cores.size : 0), 1) + Vars.spawner.countSpawns();
            builder.append("其包含(×").append(sum).append(")");
        }else{
            builder.append("其包含(×").append(Vars.spawner.countSpawns()).append("):");
        }
        for(SpawnGroup group : state.rules.spawns){
            if(group.getSpawned(waves - 1) > 0){
                builder.append((char)Fonts.getUnicode(group.type.name)).append("(");
                if(group.effect != StatusEffects.invincible && group.effect != StatusEffects.none && group.effect != null){
                    builder.append((char)Fonts.getUnicode(group.effect.name)).append("|");
                }
                if(group.getShield(waves - 1) > 0){
                    builder.append(UI.whiteformatAmount((int)group.getShield(waves - 1))).append("|");
                }
                builder.append(group.getSpawned(waves - 1)).append(")");
            }
        }
        return builder.toString();
    }

    private String fixedTime(int timer){
        StringBuilder str = new StringBuilder();
        int m = timer / 60 / 60;
        int s = timer / 60 % 60;
        int ms = timer % 60;
        if(m > 0){
            str.append(m).append(": ");
            if(s < 10){
                str.append("0");
            }
            str.append(s).append("min");
        }else{
            str.append(s).append(".").append(ms).append('s');
        }
        return str.toString();
    }

}
