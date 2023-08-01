package mindustry.arcModule.ui;

import arc.Core;
import arc.Events;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Time;
import mindustry.Vars;
import mindustry.game.EventType;
import mindustry.ui.Styles;

import java.util.ArrayList;

import static mindustry.gen.Tex.whiteui;

public class LabelController {
    Table t = new Table();
    ScrollPane sp = new ScrollPane(t);
    String showing = "广告位招租";
    Label l = new Label(showing, Styles.outlineLabel);
    float lastWidth = 0;
    boolean playing = false;
    ArrayList<String> buffer = new ArrayList<>();

    public LabelController() {
        Events.on(EventType.ClientLoadEvent.class, e -> init());
    }

    private void init() {
        t.setBackground(((TextureRegionDrawable) whiteui).tint(0, 0, 0, 0.4f));
        t.add(l);
        sp.setScrollBarPositions(false, false);
        sp.visible = false;
        sp.color.a(0);
        sp.touchable = Touchable.disabled;
        sp.update(this::pos);
        Core.scene.add(sp);
        Vars.netClient.addPacketHandler("arcFloatText", str -> start("[violet]来自服务器的消息：" + str));
    }

    private void pos() {
        float width = Core.graphics.getWidth(), height = Core.graphics.getHeight() - Core.scene.marginTop - 75f;
        sp.setWidth(width * 0.8f);
        t.setHeight(l.getHeight());
        sp.x = width * 0.1f;
        sp.y = height;
        sp.toFront();
        if (!playing) return;
        if (width != lastWidth) {
            l.setText(pad(showing));
            lastWidth = width;
        }
        float textWidth = l.getPrefWidth() - sp.getWidth();
        sp.setScrollXForce(sp.getScrollX() + Math.max(Math.min((textWidth - sp.getWidth()) / sp.getWidth(), 6f), Core.scene.getWidth() / 1000) * Time.delta * 2);
        if (sp.getScrollX() > textWidth) end();
    }

    public void start(String str) {
        if (playing) {
            buffer.add(str);
            return;
        }
        showing = str;
        prepareStart();
    }

    private void start() {
        showing = buffer.get(0);
        buffer.remove(0);
        prepareStart();
    }

    public void clear() {
        buffer.clear();
        end();
    }

    private void prepareStart() {
        playing = sp.visible = true;
        sp.actions(Actions.sequence(Actions.run(() -> {
            l.setText(pad(showing));
            sp.setScrollX(0);
            pos();
        }), Actions.fadeIn(0.5f)));
    }

    public void end() {
        if (buffer.size() != 0) {
            start();
            return;
        }
        if (!playing) return;
        playing = false;
        sp.actions(Actions.sequence(Actions.fadeOut(0.5f), Actions.run(() -> sp.visible = playing)));
    }

    private String pad(String str) {
        StringBuilder sb = new StringBuilder();
        int count = (int) (sp.getWidth() / new Label(" ").getWidth() + 5);
        for (int i = 0; i < count; i++) sb.append(' ');
        return sb + str + sb;
    }
}
