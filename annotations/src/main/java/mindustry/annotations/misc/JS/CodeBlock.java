package mindustry.annotations.misc.JS;

import java.util.ArrayList;

public class CodeBlock {
    ArrayList<CodeBlock> list;
    public String thisCode;
    Boolean isBlock;
    int spaces;
    Boolean lastCond = true;
    CodeBlock(String code) {
        thisCode = code;
        isBlock = false;
    }

    CodeBlock(int spaces) {
        list = new ArrayList<>();
        isBlock = true;
        this.spaces = spaces;
    }

    public CodeBlock add(String str) {
        list.add(new CodeBlock(str));
        return this;
    }
    public CodeBlock add(CodeBlock code) {
        list.add(code);
        return this;
    }
    public CodeBlock addCond(String code) {
        add("if("+code+"){");
        CodeBlock cb = new CodeBlock(spaces + 4);
        list.add(cb);
        return cb;
    }
    public String build() {
        StringBuilder sb = new StringBuilder();
        for(CodeBlock code : list) {
            if(code.isBlock) {
                lastCond = true;
                sb.append("\n").append(code.build()).append("\n").append(calcSpace(spaces)).append("}");
            } else {
                if(lastCond) {
                    lastCond = false;
                } else sb.append(";");
                sb.append("\n").append(calcSpace(spaces)).append(code.thisCode);
            }
        }
        return sb.toString();
    }
    String calcSpace(int space) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < space; i++) sb.append(" ");
        return sb.toString();
    }
}
