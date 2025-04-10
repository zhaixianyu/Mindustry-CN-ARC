package mindustry.arcModule;

import arc.Core;
import arc.graphics.Color;
import arc.input.KeyCode;
import arc.scene.event.InputEvent;
import arc.scene.event.InputListener;
import arc.scene.ui.*;
import arc.scene.ui.layout.Table;
import arc.struct.ObjectMap;
import arc.struct.OrderedMap;
import arc.struct.Seq;
import arc.util.Align;
import mindustry.gen.Icon;
import mindustry.graphics.Pal;
import mindustry.ui.Styles;


import static arc.Core.*;


public class SimpleKeystrokes {
    public static final SimpleKeystrokes INSTANCE = new SimpleKeystrokes();
    public Table table;
    public OrderedMap<String, KeystrokesConfig> buttonConfigs = new OrderedMap<>();

    public SimpleKeystrokes() {
        table = new Table();
    }

    public void initKey(String configName, Seq<KeyCode> keyCodes, Runnable run) {
        buttonConfigs.put(configName, new KeystrokesConfig(keyCodes, run));
    }

    public void reBindKey(String configName, Seq<KeyCode> keyCodes) {
        buttonConfigs.get(configName).keyCodeSeq = keyCodes;
        Core.settings.put(configName, keyCode2String(keyCodes));
    }

    public static String keyCode2String(Seq<KeyCode> keyCodes) {
        if (keyCodes.size == 0) {
            return "[]";
        } else {
            StringBuilder buffer = new StringBuilder(32);
            buffer.append('[');
            buffer.append(keyCodes.first().name());

            for (int i = 1; i < keyCodes.size; ++i) {
                buffer.append(",");
                buffer.append(keyCodes.get(i).name());
            }
            buffer.append(']');
            return buffer.toString();
        }
    }

    public static Seq<KeyCode> string2KeyCode(String string) {
        Seq<KeyCode> keyCodes = new Seq<>();
        if (string.startsWith("[") && string.endsWith("]")) {
            String trimmed = string.substring(1, string.length() - 1);
            String[] array = trimmed.split(",");
            for (String s : array) {
                String key = s.trim();
                if (s.isEmpty()) continue;
                KeyCode keyCode;
                try {
                    keyCode = KeyCode.valueOf(key);
                } catch (IllegalArgumentException ignored) {
                    return keyCodes;
                }
                keyCodes.add(keyCode);
            }
        }
        return keyCodes;
    }

    public void update() {
        buttonConfigs.each((b, k) -> {
            k.tryActivated();
        });
    }

    public static class KeystrokesConfig {
        public Seq<KeyCode> keyCodeSeq;
        public boolean activated;
        public Seq<KeyCode> record;
        public Runnable run;

        public KeystrokesConfig(Seq<KeyCode> keyCodeSeq, Runnable run) {
            this.keyCodeSeq = keyCodeSeq;
            this.run = run;
            this.activated = false;
            this.record = new Seq<>(keyCodeSeq.size);
        }

        public boolean tryActivated() {
            int length = keyCodeSeq.size;
            if (length == 0 || run == null) return false;

            //记录按下的顺序
            keyCodeSeq.each(k -> {
                boolean b = input.keyDown(k);
                if (b && !record.contains(k)) {
                    record.add(k);
                } else if (!b && record.contains(k)) {
                    activated = false;
                    record.remove(k);
                }
            });


            for (int i = 0; i < keyCodeSeq.size && record.size == length; i++) {
                KeyCode k = keyCodeSeq.get(i);
                if (!k.equals(record.get(i))) {
                    break;
                }
                if (i == length - 1 && !activated) {
                    activated = true;
                    run.run();
                    return true;
                }
            }
            return false;
        }
    }

    public static class SetKeyGui {
        public static final SetKeyGui INSTANCE = new SetKeyGui();
        protected float scroll;

        public void openAllConfigUi() {
            Dialog mainDialog = new Dialog("按钮快捷键");
            addCloseButton(mainDialog);
            mainDialog.setFillParent(true);
            mainDialog.title.setAlignment(Align.center);
            mainDialog.titleTable.row();
            mainDialog.titleTable.add(new Image()).growX().height(3f).pad(4f).get().setColor(Pal.accent);
            mainDialog.cont.clear();

            SimpleKeystrokes simpleKeystrokes = SimpleKeystrokes.INSTANCE;
            ObjectMap<String, KeystrokesConfig> buttonConfigs = simpleKeystrokes.buttonConfigs;

            Dialog table = new Dialog();
            ScrollPane pane = new ScrollPane(table);
            pane.setFadeScrollBars(false);
            pane.update(() -> scroll = pane.getScrollY());
            for (ObjectMap.Entry<String, KeystrokesConfig> entry : buttonConfigs) {

                table.add().height(10);
                table.row();
                table.image().color(Color.gray).fillX().height(3).pad(6).colspan(4).padTop(0).padBottom(10).row();

                String name = entry.key;
                table.add(name, Color.white).left().padRight(20).padLeft(8);
                table.label(() -> SimpleKeystrokes.keyCode2String(buttonConfigs.get(name).keyCodeSeq)).color(Pal.accent).left().minWidth(90).padRight(20);
                addButton(table, name);
                table.row();

            }
            table.button("清空全部已绑定按钮快捷键", () -> simpleKeystrokes.buttonConfigs.forEach((configEntry) -> simpleKeystrokes.reBindKey(configEntry.key, configEntry.value.keyCodeSeq))).colspan(4).padTop(4).fill();
            mainDialog.cont.row();
            mainDialog.cont.add(pane).growX().colspan(simpleKeystrokes.buttonConfigs.size);
            mainDialog.show();
        }

        public void addCloseButton(Dialog dialog) {
            dialog.buttons.button("@back", Icon.left, dialog::hide).size(210f, 64f);

            dialog.keyDown(key -> {
                if (key == KeyCode.escape || key == KeyCode.back) dialog.hide();
            });
        }

        public void addButton(Dialog dialog, String buttonName) {
            dialog.button("重新绑定", Styles.defaultt, () -> {
                openResetDialog(buttonName);
            }).width(100f);
            dialog.button("取消绑定", Styles.defaultt, () -> {
                SimpleKeystrokes.INSTANCE.reBindKey(buttonName, new Seq<>());
            }).width(100f);
        }

        //右键点击按钮创建设置快捷键ui
        public void openDialog(String name) {
            Dialog mainDialog = new Dialog();
            Dialog dialog = new Dialog();
            addCloseButton(mainDialog);
            mainDialog.setFillParent(true);
            mainDialog.cont.add(dialog);

            dialog.row();
            dialog.label(() -> name + " 当前快捷键为 " + SimpleKeystrokes.INSTANCE.buttonConfigs.get(name).keyCodeSeq.toString()).left().padRight(20).padLeft(8);
            addButton(dialog, name);
            dialog.row();
            dialog.button("查看全部按钮快捷键", Styles.defaultt, () -> {
                mainDialog.hide();
                openAllConfigUi();
            }).colspan(4).padTop(4).fill();
            dialog.row();
            mainDialog.show();
        }

        public void openResetDialog(String buttonName) {
            Dialog rebindDialog = new Dialog("请设置快捷键，可以是组合键.");
            inputKey(buttonName, rebindDialog);
            rebindDialog.show();
        }

        public void inputKey(String name, Dialog rebindDialog) {
            InputListener inputListener = new InputListener() {
                Seq<KeyCode> downs = new Seq<>();
                Seq<KeyCode> ups = new Seq<>();

                @Override
                public boolean keyDown(InputEvent event, KeyCode keycode) {
                    if (!downs.contains(keycode)) downs.add(keycode);
                    return false;
                }

                @Override
                public boolean keyUp(InputEvent event, KeyCode keycode) {
                    ups.add(keycode);
                    if (!downs.isEmpty() && ups.containsAll(downs)) {
                        SimpleKeystrokes.INSTANCE.reBindKey(name, downs);
                        downs = new Seq<>();
                        ups = new Seq<>();
                        rebindDialog.hide();
                    }
                    return false;
                }
            };
            rebindDialog.addListener(inputListener);
        }
    }
}
