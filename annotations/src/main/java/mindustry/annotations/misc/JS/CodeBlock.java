package mindustry.annotations.misc.JS;

import arc.func.Cons;

import java.util.ArrayList;

public class CodeBlock extends JSBuilder{
    ArrayList<JSBuilder> list;
    String thisCode;
    Boolean isBlock = false;
    int spaces;
    public CodeBlock(String code) {
        thisCode = code;
    }

    public CodeBlock(int spaces) {
        list = new ArrayList<>();
        isBlock = true;
        this.spaces = spaces;
    }
    public CodeBlock add(String str, Cons<CodeBlock> cbcb) {
        CodeBlock cb = new CodeBlock(str);
        list.add(cb);
        cbcb.get(cb);
        return this;
    }
    public CodeBlock add(String str) {
        list.add(new CodeBlock(str));
        return this;
    }
    public CodeBlock add(JSBuilder code) {
        list.add(code);
        return this;
    }
    public CodeBlock addSynx(String synx, String cond) {
        add(synx + "(" + cond + "){");
        CodeBlock cb = new CodeBlock(spaces);
        add(cb);
        add("}");
        cb.noSemicolon = true;
        return cb;
    }
    @Override
    public String build() {
        if(!isBlock) return thisCode;
        StringBuilder sb = new StringBuilder();
        String space = calcSpace(spaces);
        boolean[] next = {false};
        boolean[] lastCond = {true};
        JSBuilder[] lastBlock = {new CodeBlock(0)};
        for(JSBuilder code : list) {
            if(code.isBlock) {
                lastCond[0] = true;
                if(code.forceWarp) sb.append("\n");
                sb.append(code.build());
            } else {
                if(lastCond[0]) {
                    lastCond[0] = false;
                } else if(lastBlock[0].forceSemicolon || !code.noSemicolon) sb.append(";");//TODO wrong semicolon
                if(next[0]) {
                    if(code.forceWarp || !lastBlock[0].noWarp && !code.noWarp) sb.append("\n");
                } else {
                    next[0] = true;
                }
                sb.append(space).append(code.build());
            }
            lastBlock[0] = code;
        }
        return sb.toString();
    }
    public ClassBuilder newClass(String name) {
        ClassBuilder cb = new ClassBuilder(name, spaces);
        add(cb);
        return cb;
    }
    public ClassBuilder newClass(String name, String extend) {
        ClassBuilder cb = new ClassBuilder(name, extend, spaces);
        add(cb);
        return cb;
    }
    public CodeBlock newBlock(){
        CodeBlock cb = new CodeBlock(spaces + 4);
        add(cb);
        return cb;
    }
    public CodeBlock newClosure() {
        CodeBlock cb = new CodeBlock(spaces + 4);
        add("(()=>{");
        add(cb);
        add("})()");
        return cb;
    }
    public CodeBlock newExp(JSBuilder code) {
        CodeBlock cb = new CodeBlock("(" + code.build() + ")");
        return this;
    }
    public ObjectBuilder newObject() {
        ObjectBuilder ob = new ObjectBuilder(spaces);
        add(ob);
        return ob;
    }
}
