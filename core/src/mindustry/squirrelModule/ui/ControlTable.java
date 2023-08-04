package mindustry.squirrelModule.ui;

import arc.graphics.Color;
import arc.input.KeyCode;
import arc.math.geom.Vec2;
import arc.scene.Element;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.event.Touchable;
import arc.scene.style.Drawable;
import arc.scene.ui.layout.Cell;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.util.Align;
import arc.util.Tmp;
import mindustry.squirrelModule.modules.Config;
import mindustry.squirrelModule.modules.SMisc;

import static mindustry.Vars.ui;

public class ControlTable extends Table {
    ObjectMap<String, ObjectMap<String, Config>> list;

    public ControlTable(ObjectMap<String, ObjectMap<String, Config>> map) {
        super();
        setFillParent(true);
        list = map;
    }

    public void buildClickHUD() {
        list.each((k, v) -> parent.addChild(new ClickHUD(k, v)));
    }

    private static class ClickHUD extends Table {
        boolean expand = false;

        public ClickHUD(String title, ObjectMap<String, Config> map) {
            super();
            Drawable gray = SStyles.tint(Color.valueOf("333333"));
            Drawable gray2 = SStyles.tint(Color.valueOf("555555"));
            table(t1 -> {
                t1.table(t2 -> {
                    t2.touchable = Touchable.enabled;
                    t2.add(title).center();
                    t2.setBackground(gray);
                    t2.addListener(new InputListener() {
                        float lastX, lastY;
                        boolean dragged;

                        @Override
                        public boolean touchDown(InputEvent event, float x, float y, int pointer, KeyCode button) {
                            dragged = false;
                            Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));
                            lastX = v.x;
                            lastY = v.y;
                            toFront();
                            return true;
                        }

                        @Override
                        public void touchDragged(InputEvent event, float x, float y, int pointer) {
                            dragged = true;
                            Vec2 v = localToParentCoordinates(Tmp.v1.set(x, y));
                            ClickHUD.this.x += v.x - lastX;
                            ClickHUD.this.y += v.y - lastY;
                            lastX = v.x;
                            lastY = v.y;
                        }

                        @Override
                        public void touchUp(InputEvent event, float x, float y, int pointer, KeyCode button) {
                            if (!dragged) expand = !expand;
                        }
                    });
                }).growX().height(35).row();
                t1.table(t2 -> map.each((k, v) -> {
                    boolean[] enabled = {false, false};
                    float[] rot = {0};
                    Cell<?>[] settings = {null};
                    final Config value = v;
                    final String name = k;
                    t2.table(t3 -> {
                        t3.table(t4 -> {
                            t4.touchable = Touchable.enabled;
                            t4.update(() -> rot[0] = ui.infoControl.getColor() + 180);
                            t4.add(name).update(l -> {
                                if (enabled[0]) {
                                    l.setText("[#444444]" + name);
                                    t4.setBackground(SStyles.tint(SMisc.color(rot[0])));
                                } else {
                                    l.setText(name);
                                    t4.setBackground(gray);
                                }
                                rot[0] = (rot[0] + InfoControl.lineAdd) % 180;
                            }).left().growX();
                            t4.clicked(() -> {
                                if (value.enabled) {
                                    value.func.onDisable();
                                    value.enabled = false;
                                    ui.infoControl.remove(name);
                                } else {
                                    value.func.onEnable();
                                    value.enabled = true;
                                    ui.infoControl.add(name);
                                }
                                enabled[0] = !enabled[0];
                            });
                        }).grow();
                        t3.table(t4 -> {
                            t4.touchable = Touchable.enabled;
                            t4.setBackground(gray2);
                            t4.add(":").size(13, 32).align(Align.center);
                            t4.clicked(() -> {
                                if (enabled[1]) {
                                    settings[0].setElement(null);
                                    enabled[1] = false;
                                } else {
                                    settings[0].setElement(new Table(t5 -> {
                                        t5.setBackground(gray2);
                                        for (Element e : value.element) {
                                            t5.add(e).growX().pad(5).row();
                                        }
                                    }));
                                    enabled[1] = true;
                                }
                            });
                        });
                    }).growX().height(32).row();
                    settings[0] = t2.add().growX();
                    t2.row();
                })).minWidth(200).visible(() -> expand);
            });
        }
    }
}
