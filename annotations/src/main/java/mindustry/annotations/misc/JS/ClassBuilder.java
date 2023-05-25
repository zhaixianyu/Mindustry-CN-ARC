package mindustry.annotations.misc.JS;

import com.sun.tools.javac.jvm.Code;

import java.util.ArrayList;

public class ClassBuilder {
    String className;
    CodeBlock builder = new CodeBlock(0);
    ArrayList<CodeBlock> vars = new ArrayList<>();
    ArrayList<CodeBlock> func = new ArrayList<>();
    int spaces;
    public ClassBuilder(String name, int spaces) {
        className = name;
        this.spaces = spaces;
    }
    public ClassBuilder(String name, String extend, int spaces) {
        className = name + " extends " + extend;
        this.spaces = spaces;
    }
    public ClassBuilder add(String code) {
        builder.add(code);
        return this;
    }
    public ClassBuilder add(CodeBlock code) {
        builder.add(code);
        return this;
    }
    public void addVariable(String name) {
        vars.add(new CodeBlock(name + ";"));
    }
    public void addVariable(String name, String defaultValue) {
        vars.add(new CodeBlock(name + "=" + defaultValue + ";"));
    }
    public CodeBlock addMethod(String name, String[] args) {
        StringBuilder sb = new StringBuilder();
        boolean start = true;
        for(String i : args) {
            if(start) start=false; else sb.append(",");
            sb.append(i);
        }
        CodeBlock cb = new CodeBlock(spaces + 4 + 4);
        func.add(cb);
        return cb.addSynx(name, sb.toString());
    }
    String calcSpace(int space) {
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i < space; i++) sb.append(" ");
        return sb.toString();
    }
    public String build() {
        StringBuilder sb = new StringBuilder("class " + className + "{");
        for(CodeBlock i : vars) {
            sb.append(calcSpace(spaces)).append(i).append("\n");
        }
        for(CodeBlock i : func) {
            sb.append(i.build()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
