package mindustry.arcModule.ui;

import arc.Core;
import arc.Events;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.ui.Styles;

public class LabelController {
    Table t = new Table();
    ScrollPane sp = new ScrollPane(t);
    String showing = "广告位招租";
    Label l = new Label(showing);
    float lastWidth = 0;
    boolean playing = false;
    Runnable onEnd = () -> {};
    public LabelController() {
        Events.on(EventType.ClientLoadEvent.class, e -> init());
    }
    public void init() {
        t.setBackground(Styles.black5);
        t.add(l);
        sp.visible = false;
        sp.color.a(0);
        sp.touchable = Touchable.disabled;
        sp.update(this::pos);
        Core.scene.add(sp);
        Vars.netClient.addPacketHandler("arcFloatText", this::start);
        Vars.netClient.addPacketHandler("arcFloatTextEnd", s -> this.end());
    }
    public void pos() {
        float width = Core.graphics.getWidth(), height = Core.graphics.getHeight() - Core.scene.marginTop - 75f;
        sp.setWidth(width * 0.8f);
        sp.setHeight(l.getHeight());
        sp.x = width * 0.1f;
        sp.y = height;
        sp.toFront();
        if(!playing) return;
        if(width != lastWidth) {
            l.setText(pad(showing));
            lastWidth = width;
        }
        sp.setScrollXForce(sp.getScrollX() + 1);
        if(sp.getScrollX() > l.getPrefWidth() - sp.getWidth()) end();
    }
    public void start(String str) {
        if(playing) return;
        showing = str;
        playing = sp.visible = true;
        l.setText(pad(showing));
        sp.setScrollX(0);
        pos();
        sp.actions(Actions.sequence(Actions.fadeIn(0.5f)));
    }
    public void end() {
        if(!playing) return;
        playing = false;
        sp.actions(Actions.sequence(Actions.fadeOut(0.5f), Actions.run(onEnd), Actions.visible(false)));
    }
    public void onEnd(Runnable r) {
        onEnd = r;
    }
    public String pad(String str) {
        StringBuilder sb = new StringBuilder();
        int count = (int) (sp.getWidth() / new Label(" ").getWidth() + 5);
        for(int i = 0; i < count; i++) sb.append(' ');
        return sb + str + sb;
    }
}
