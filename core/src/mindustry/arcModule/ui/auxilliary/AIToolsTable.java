package mindustry.arcModule.ui.auxilliary;

import arc.*;
import arc.scene.style.*;
import arc.scene.ui.layout.*;
import arc.util.*;
import mindustry.ai.types.*;
import mindustry.arcModule.*;
import mindustry.content.*;
import mindustry.entities.units.*;
import mindustry.game.*;
import mindustry.gen.*;
import mindustry.type.*;

import static mindustry.Vars.player;
import static mindustry.arcModule.ui.RStyles.*;
import static mindustry.content.UnitTypes.*;

public class AIToolsTable extends BaseToolsTable{
    private AIController selectAI;

    public AIToolsTable(){
        super(Icon.android);

        Events.run(EventType.Trigger.update, () -> {
            if(selectAI != null){
                selectAI.unit(player.unit());
                selectAI.updateUnit();
            }
        });
    }

    @Override
    public void setup(){
        aiButton(new arcMinerAI(), mono, "矿机AI");
        aiButton(new BuilderAI(), poly, "重建AI");
        aiButton(new RepairAI(), mega, "修复AI");
        aiButton(new DefenderAI(), oct, "保护AI");
    }

    private void aiButton(AIController ai, UnitType symbol, String describe){
        var button = button(new TextureRegionDrawable(symbol.uiIcon), clearLineNoneTogglei, 30, () -> selectAI(ai))
        .checked(b -> selectAI == ai).size(40).get();

        ElementUtils.tooltip(button, describe);
    }

    private void selectAI(AIController ai){
        selectAI = selectAI == ai ? null : ai;
    }

}
