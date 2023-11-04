package mindustry.arcModule;

import arc.files.Fi;
import arc.util.OS;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ARCClassLoader extends ClassLoader {
    ClassLoader loader, sys;
    Fi dir, cur;
    String className;
    public ARCClassLoader(ClassLoader ext) {
        loader = getClass().getClassLoader();
        sys = ext;
        dir = new Fi(OS.getAppDataDirectoryString("Mindustry")).child("arcfix");
    }
    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        return super.loadClass(name);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> clazz = findLoadedClass(name);//加载过的类

        try {//java内部类
            clazz = sys.loadClass(name);
        } catch (Exception ignored) {
        }

        className = name.replace(".", "/") + ".class";
        if (clazz == null && (cur = dir.child(className)).exists()) {//用于覆盖的类
            byte[] bytes = cur.readBytes();
            clazz = defineClass(name, bytes, 0, bytes.length);
        }

        if (clazz == null) {//本地类
            try (InputStream in = loader.getResourceAsStream(className)) {
                if (in == null) {
                    throw new ClassNotFoundException();
                }
                ByteArrayOutputStream output = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    output.write(buffer, 0, length);
                }
                byte[] bytes = output.toByteArray();
                clazz = defineClass(name, bytes, 0, bytes.length);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (clazz == null) {
            throw new ClassNotFoundException();
        }

        if (resolve) {
            resolveClass(clazz);
        }

        return clazz;
    }
}
