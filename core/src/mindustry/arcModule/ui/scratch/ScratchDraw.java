package mindustry.arcModule.ui.scratch;

import arc.Core;
import arc.graphics.Color;
import arc.graphics.Pixmap;
import arc.graphics.Texture;
import arc.graphics.g2d.*;
import arc.math.Mathf;
import arc.util.Align;
import arc.util.Tmp;

public class ScratchDraw {
    public static final Color selectedColor = Color.gold;
    public static TextureRegion circle = null;
    private static final Color c1 = new Color();
    private static TextureRegion arrow;

    public static void init() {
        circle = new TextureAtlas.AtlasRegion(Core.atlas.find("circle"));
        circle.setHeight(circle.height / 2 - 4);
        Pixmap p = new Pixmap(16, 15);
        int b = Color.black.rgba8888(), w = Color.white.rgba8888();
        for (int i = 0; i < 7; ++i) {
            p.set(6 - i, i + 1, b);
            p.set(i, i + 7, b);
        }
        for (int i = 1; i < 7; ++i) {
            p.drawLine(i, 8 - i, i, i + 6, w);
        }
        p.drawLine(7, 0, 7, 4, b);
        p.drawLine(7, 10, 7, 14, b);
        p.drawLine(8, 4, 15, 4, b);
        p.drawLine(8, 10, 15, 10, b);
        p.drawLine(15, 5, 15, 9, b);
        p.fillRect(7, 5, 8, 5, w);
        arrow = new TextureRegion(new Texture(p));
        p.dispose();
    }

    public static void drawRect(float x, float y, float width, float height, Color c) {
        Draw.color(c);
        Fill.crect(x, y, width, height);
        Draw.color(Tmp.c1.set(c).lerp(Color.black, 0.3f));
        Lines.line(x + width, y, x + width, y + height);
        Lines.line(x, y, x + width, y);
        Lines.line(x, y, x, y + height);
        Lines.line(x, y + height, x + width, y + height);
    }

    public static void drawOutline(float x, float y, float width, float height, int dir) {
        Draw.color(Tmp.c1.set(Color.black).a(0.2f));
        Lines.stroke(1);
        if ((dir & Align.right) != 0) Lines.line(x + width, y, x + width, y + height);
        if ((dir & Align.bottom) != 0) Lines.line(x, y, x + width, y);
        if ((dir & Align.left) != 0) Lines.line(x, y, x, y + height);
        if ((dir & Align.top) != 0) Lines.line(x, y + height, x + width, y + height);
    }

    public static void drawInput(float x, float y, float w, float h, Color c, boolean selected) {
        Color col = selected ? selectedColor : c1.set(c).lerp(Color.black, 0.3f);
        Lines.stroke(selected ? 2 : 1);
        y = Mathf.floor(y);
        float halfH = h / 2;
        Draw.color(c);
        Fill.circle(x + halfH, y + halfH, halfH);
        Fill.circle(x + w - halfH, y + halfH, halfH);
        Draw.color(col);
        Lines.circle(x + halfH, y + halfH, halfH);
        Lines.circle(x + w - halfH, y + halfH, halfH);
        Draw.color(c);
        if (w < h + halfH) {
            Fill.circle(x + halfH, y + halfH, halfH - 1);
            Fill.circle(x + w - halfH, y + halfH, halfH - 1);
        }
        Fill.rect(x + w / 2, y + halfH, w - 2 * halfH, h);
        Draw.color(col);
        float ty = selected ? y : y + 0.5f;
        Lines.line(x + halfH, ty, x + w - halfH, ty);
        Lines.line(x + halfH, y + h, x + w - halfH, y + h);
    }

    public static void drawInputBorderless(float x, float y, float w, float h, Color c) {
        Lines.stroke(1);
        y = Mathf.floor(y);
        float halfH = h / 2;
        Draw.color(c);
        Fill.circle(x + halfH, y + halfH, halfH);
        Fill.circle(x + w - halfH, y + halfH, halfH);
        Draw.color(c);
        Fill.rect(x + w / 2, y + halfH, w - 2 * halfH, h);
        float ty =  y + 0.5f;
        Lines.line(x + halfH, ty, x + w - halfH, ty);
        Lines.line(x + halfH, y + h, x + w - halfH, y + h);
    }

    public static void drawInputSelected(float x, float y, float w, float h) {
        float halfH = h / 2;
        Draw.color(selectedColor);
        Lines.stroke(3);
        Lines.circle(x + halfH, y + halfH, halfH);
        Lines.circle(x + w - halfH, y + halfH, halfH);
        Lines.line(x + halfH, y + 1, x + w - halfH, y + 1);
        Lines.line(x + halfH, y + h, x + w - halfH, y + h);
    }

    public static void drawCond(float x, float y, float w, float h, Color c, boolean noBorder, boolean selected) {
        Draw.color(c);
        float halfH = h / 2;
        Fill.tri(x + halfH, y, x, y + halfH, x + halfH, y + h);
        Fill.tri(x + w - halfH, y, x + w, y + halfH, x + w - halfH, y + h);
        Fill.rect(x + w / 2, y + halfH, w - 2 * halfH, h);
        if (noBorder) return;
        Draw.color(selected ? selectedColor : Tmp.c1.set(c).lerp(Color.black, 0.3f));
        Lines.stroke(selected ? 2 : 1);
        condPoint(x, y, w, h, halfH);
        Lines.endLine(true);
    }

    public static void drawCondSelected(float x, float y, float w, float h) {
        Draw.color(selectedColor);
        float halfH = h / 2;
        Lines.stroke(2);
        condPoint(x, y, w, h, halfH);
        Lines.endLine();
        Lines.line(x + halfH + 1, y - 1, x + w - halfH - 1, y - 1);
    }

    private static void condPoint(float x, float y, float w, float h, float halfH) {
        Lines.beginLine();
        Lines.linePoint(x + halfH, y);
        Lines.linePoint(x, y + halfH);
        Lines.linePoint(x + halfH, y + h);
        Lines.linePoint(x + w - halfH, y + h);
        Lines.linePoint(x + w, y + halfH);
        Lines.linePoint(x + w - halfH, y);
    }

    public static void drawBlock(float x, float y, float w, float h, Color c, boolean noBorder) {
        drawBlockHeader(x, y, w, h, c);
        drawBlockInner(x, y, w, h);
        if (!noBorder) drawBlockBorder(x, y, w, h, c);
    }

    public static void drawBlockInner(float x, float y, float w, float h) {
        Fill.polyBegin();
        Fill.polyPoint(x, y + h - 7);
        Fill.polyPoint(x + w, y + h - 7);
        drawInner(x, y, w);
        Fill.polyEnd();
    }

    public static void drawBlockHeader(float x, float y, float w, float h, Color c) {
        Draw.color(c);
        Fill.quad(x, y + h, x + 10, y + h, x + 15, y + h - 7, x, y + h - 7);
        Fill.quad(x + 30, y + h - 7, x + 35, y + h, x + w, y + h, x + w, y + h - 7);
    }

    public static void drawBlockBorder(float x, float y, float w, float h, Color c) {
        Lines.stroke(1);
        Draw.color(Tmp.c1.set(c).lerp(Color.black, 0.3f));
        Lines.beginLine();
        drawBlockBorderTop(x, y, w, h);
        drawBlockBorderBottom(x, y);
        Lines.linePoint(x, y + h);
        Lines.endLine();
    }

    public static void drawBlockBorderTop(float x, float y, float w, float h) {
        Lines.linePoint(x, y + h);
        Lines.linePoint(x + 10, y + h);
        Lines.linePoint(x + 15, y + h - 7);
        Lines.linePoint(x + 30, y + h - 7);
        Lines.linePoint(x + 35, y + h);
        Lines.linePoint(x + w, y + h);
        Lines.linePoint(x + w, y);
    }

    public static void drawBlockBorderBottom(float x, float y) {
        Lines.linePoint(x + 35, y);
        Lines.linePoint(x + 30, y - 7);
        Lines.linePoint(x + 15, y - 7);
        Lines.linePoint(x + 10, y);
        Lines.linePoint(x, y);
    }

    public static void drawBorderBottom(float x, float y, float w) {
        Lines.line(x + w, y, x + 35, y);
        Lines.line(x + 35, y, x + 30, y - 7);
        Lines.line(x + 30, y - 7, x + 15, y - 7);
        Lines.line(x + 15, y - 7, x + 10, y);
        Lines.line(x + 10, y, x, y);
    }

    public static void drawTriggerBlock(float x, float y, float w, Color c, boolean noBorder) {
        int dark = 0;
        if (!noBorder) {
            dark = Tmp.c1.set(c).lerp(Color.black, 0.3f).rgba();
            Draw.color(dark);
            Draw.rect(circle, x + 31, y + 42, 64, 18);
        }
        Draw.color(c);
        Draw.rect(circle, x + 31, y + 41, 62, 17);
        ScratchDraw.drawBlockInner(x, y, w, 41);
        if (!noBorder) {
            Draw.color(dark);
            Lines.stroke(1);
            ScratchDraw.drawBorderBottom(x, y, w);
            Lines.line(x, y, x, y + 33);
            Lines.line(x + 64, y + 33, x + w, y + 33);
            Lines.line(x + w, y + 33, x + w, y);
        }
    }

    public static void drawPopup(float x, float y, float w, float h, Color c) {
        Draw.color(c);
        Fill.crect(x, y, w, h);
        Fill.tri(x + w / 2 + 9, y + h, x + w / 2, y + h + 9, x + w / 2 - 9, y + h);
        Draw.color(Tmp.c1.set(c).lerp(Color.black, 0.3f));
        Lines.stroke(1);
        Lines.beginLine();
        Lines.linePoint(x, y);
        Lines.linePoint(x + w, y);
        Lines.linePoint(x + w, y + h);
        Lines.linePoint(x + w / 2 + 9, y + h);
        Lines.linePoint(x + w / 2, y + h + 9);
        Lines.linePoint(x + w / 2 - 9, y + h);
        Lines.linePoint(x, y + h);
        Lines.endLine(true);
    }

    public static void drawArrow(float x, float y, Color c) {
        Draw.color(c);
        Draw.rect(arrow, x, y, arrow.width, arrow.height);
    }

    public static void drawFlatInner(float x, float y, float w, float h) {
        Fill.polyBegin();
        Fill.polyPoint(x, y + h);
        Fill.polyPoint(x + w, y + h);
        drawInner(x, y, w);
        Fill.polyEnd();
    }

    private static void drawInner(float x, float y, float w) {
        Fill.polyPoint(x + w, y);
        Fill.polyPoint(x + 35, y);
        Fill.polyPoint(x + 30, y - 7);
        Fill.polyPoint(x + 15, y - 7);
        Fill.polyPoint(x + 10, y);
        Fill.polyPoint(x, y);
    }

    public static void drawFlatBorder(float x, float y, float w, float h) {
        Lines.linePoint(x, y + h);
        Lines.linePoint(x + w, y + h);
        Lines.linePoint(x + w, y);
    }

    public static void drawFunctionBlock(float x, float y, float dx, float w, float dw, Color c, Color c2) {
        Draw.color(c);
        drawFlatInner(x, y, w, 56);
        drawBlock(x + dx, y + 10, dw, 36, c2, true);
        Lines.stroke(1);
        Draw.color(Tmp.c1.set(c).lerp(Color.black, 0.3f));
        Lines.beginLine();
        drawFlatBorder(x, y, w, 56);
        drawBlockBorderBottom(x, y);
        Lines.linePoint(x, y + 56);
        Lines.endLine();
    }
}
