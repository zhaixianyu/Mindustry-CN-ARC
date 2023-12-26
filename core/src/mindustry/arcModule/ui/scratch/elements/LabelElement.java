package mindustry.arcModule.ui.scratch.elements;

import arc.scene.ui.Label;
import arc.scene.ui.layout.Cell;
import mindustry.arcModule.ui.scratch.ScratchTable;
import mindustry.arcModule.ui.scratch.ScratchType;

public class LabelElement extends ScratchElement {
    private final Label l;

    {
        hittable = false;
    }

    public LabelElement(String str) {
        add(l = new Label(str));
    }

    @Override
    public void cell(Cell<ScratchTable> c) {
        if (parent.getChildren().size == 1) c.padLeft(addPadding);
    }

    @Override
    public ScratchType getType() {
        return ScratchType.none;
    }

    @Override
    public ScratchElement copy() {
        return new LabelElement(l.getText().toString());
    }
}
