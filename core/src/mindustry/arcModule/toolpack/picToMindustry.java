package mindustry.arcModule.toolpack;

import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.scene.ui.Dialog;
import arc.struct.ObjectMap;
import arc.struct.Seq;
import arc.struct.StringMap;
import mindustry.Vars;
import mindustry.game.Schematic;
import mindustry.ui.Styles;
import mindustry.ui.dialogs.BaseDialog;
import mindustry.world.blocks.logic.CanvasBlock;
import mindustry.world.blocks.logic.CanvasBlock.*;

import static mindustry.Vars.ui;
import static mindustry.content.Blocks.canvas;

public class picToMindustry {

    Pixmap image, Cimage;
    Integer closest = null;

    public picToMindustry() {
        ptDialog().show();
    }

    public Dialog ptDialog() {
        Dialog pt = new BaseDialog("arc-图片转换器");
        pt.cont.table(t -> {
            t.add("选择并导入图片，可将其转成画板、像素画或是逻辑画").row();
            t.add("已完成：转画板，其他功能制作中").row();
            t.button("Select Image", () -> {
                Vars.platform.showFileChooser(false, "png", file -> {
                    try {
                        byte[] bytes = file.readBytes();
                        image = new Pixmap(bytes);
                        Cimage = image.copy();
                        create_rbg();
                    } catch (Throwable e) {
                        ui.arcInfo("Failed to load source image\n" + e);
                    }
                });
            }).size(240, 50);
            t.row();
            t.table(tt->{
                tt.button(canvas.emoji(), Styles.cleart,()->{
                    canvasGenerator();
                }).size(100,50);
            });
        });
        pt.addCloseButton();
        return pt;
    }

    private float diff_hsv(int[] a,int[] b){
        float dh = Math.abs(a[0] - b[0]) * 2,
            ds = Math.abs(a[1] - b[1]),
            dv = Math.abs(a[2] - b[2]);
        return dh + ds + dv;
    }

    private void create_hsv(){
        ObjectMap<Integer,int[]> canvasMap = new ObjectMap<>();
        Color tmp = new Color();

        for (int i : CanvasBlock.palette) {
            canvasMap.put(CanvasBlock.palette[i],Color.RGBtoHSV(tmp.set(CanvasBlock.palette[i])));
        }

        for (var x = 0; x < image.width; x++) {
            for (var y = 0; y < image.height; y++) {
                int raw = image.get(x, y);
                int[] pixel = Color.RGBtoHSV(tmp.set(raw));

                float egg = 10000;
                canvasMap.each((a,b)-> {
                    float h = diff_hsv(pixel, b);
                    if (h < egg) {
                        closest = a;
                    }
                });
                Cimage.set(x, y, closest);
            }
        }
    }

    private float diff_rbg(Integer a,Integer b){
        int ar = (a >> 16) & 0xFF,
                    ag = (a >> 8) & 0xFF,
                    ab = a & 0xFF;
            // get in
        int br = (b >> 16) & 0xFF,
                    bg = (b >> 8) & 0xFF,
                    bb = b & 0xFF;
        int dr = Math.abs(ar - br),
                    dg = Math.abs(ag - bg),
                    db = Math.abs(ab - bb);
        return dr + dg + db;
    }

    private void create_rbg(){
        for (var x = 0; x < image.width; x++) {
            for (var y = 0; y < image.height; y++) {
                Integer pixel = image.get(x, y);

                float egg = 1000;
                for (var other : CanvasBlock.palette) {
                    float h = diff_rbg(pixel, other);
                    if (h < egg) {
                        closest = other;
                        egg = h;
                    }
                }
                Cimage.set(x, y, closest);
            }
        }
    }

    private void canvasGenerator(){
        int width = Cimage.width / CanvasBlock.canvasSize, height = Cimage.height / CanvasBlock.canvasSize;
        var tiles = new Seq();
            for (var y = 0; y < height; y++) {
                for (var x = 0; x < width; x++) {
                    // add canvas to the schematic
                    CanvasBuild build = (CanvasBuild) canvas.newBuilding();
                    // get max 12x12 region of the image
                    Pixmap region = Cimage.crop(x * CanvasBlock.canvasSize, (height - y - 1) * CanvasBlock.canvasSize, CanvasBlock.canvasSize, CanvasBlock.canvasSize);
                    // convert pixel data of the region
                    byte[] bytes = build.packPixmap(region);
                    var stile = new Schematic.Stile(canvas, x * 2, y * 2, bytes, (byte) 0);
                    tiles.add(stile);
                }
            }
        StringMap tags = new StringMap();
        tags.put("name", "!!name me");
        var schem = new Schematic(tiles, tags, width, height);
        // Import it
        Vars.schematics.add(schem);
        // Select it
        Vars.ui.schematics.hide();
        Vars.control.input.useSchematic(schem);
    }
}
