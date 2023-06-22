package mindustry.arcModule.ui;

import arc.Core;
import arc.Events;
import arc.math.Rand;
import arc.scene.actions.Actions;
import arc.scene.event.Touchable;
import arc.scene.style.TextureRegionDrawable;
import arc.scene.ui.Label;
import arc.scene.ui.ScrollPane;
import arc.scene.ui.layout.Table;
import arc.util.Http;
import arc.util.Log;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.game.EventType;

import java.util.ArrayList;

import static mindustry.Vars.userContentURL;
import static mindustry.gen.Tex.whiteui;

public class LabelController {
    Table t = new Table();
    ScrollPane sp = new ScrollPane(t);
    String showing = "广告位招租";
    Label l = new Label(showing);
    float lastWidth = 0;
    boolean playing = false;
    ArrayList<String> buffer = new ArrayList<>();
    String[] labels = { "学术端!" };
    public LabelController() {
        Events.on(EventType.ClientLoadEvent.class, e -> init());
    }
    private void init() {
        t.setBackground(((TextureRegionDrawable) whiteui).tint(0, 0, 0, 0.4f));
        t.add(l);
        sp.visible = false;
        sp.color.a(0);
        sp.touchable = Touchable.disabled;
        sp.update(this::pos);
        Core.scene.add(sp);
        Vars.netClient.addPacketHandler("arcFloatText", this::start);
        Vars.netClient.addPacketHandler("arcFloatTextEnd", s -> this.end());
        loadLabels();
    }
    private void pos() {
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
        float textWidth = l.getPrefWidth() - sp.getWidth();
        sp.setScrollXForce(sp.getScrollX() + Math.max(Math.min((textWidth - sp.getWidth()) / sp.getWidth(), 4f), 1.5f));
        if(sp.getScrollX() > textWidth) end();
    }
    private void loadLabels(){
        Http.get(userContentURL + "/CN-ARC/Mindustry-CN-ARC/master/core/assets/floatLabels")
                .error(e -> {
                    Log.err("获取最新浮动标语失败!加载本地标语", e);
                    labels = Core.files.internal("floatLabels").readString("UTF-8").replace("\r", "").replace("\\n", "\n").split("\n");
                    Timer.schedule(this::randomLabel, 600, 600);
                })
                .submit(result -> {
                    labels = result.getResultAsString().replace("\r", "").replace("\\n", "\n").split("\n");
                    Timer.schedule(this::randomLabel, 600, 600);
                });
    }

    private void randomLabel(){
        start(labels[new Rand().random(0, labels.length - 1)]);
    }
    public void start(String str) {
        if(playing) {
            buffer.add(str);
            return;
        }
        showing = str;
        prepareStart();
    }
    private void start() {
        sp.actions();
        showing = buffer.get(0);
        buffer.remove(0);
        prepareStart();
    }
    private void prepareStart() {
        sp.actions(Actions.sequence(Actions.fadeIn(0.5f), Actions.visible(true)));
        playing = sp.visible = true;
        l.setText(pad(showing));
        sp.setScrollX(0);
        pos();
    }
    public void end() {
        if(buffer.size() != 0) {
            start();
            return;
        }
        if(!playing) return;
        playing = false;
        sp.actions(Actions.sequence(Actions.fadeOut(0.5f), Actions.visible(false)));
    }
    private String pad(String str) {
        StringBuilder sb = new StringBuilder();
        int count = (int) (sp.getWidth() / new Label(" ").getWidth() + 5);
        for(int i = 0; i < count; i++) sb.append(' ');
        return sb + str + sb;
    }
}
