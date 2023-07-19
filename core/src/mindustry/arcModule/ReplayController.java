package mindustry.arcModule;

import arc.Core;
import arc.files.Fi;
import arc.scene.event.Touchable;
import arc.scene.ui.layout.Table;
import arc.scene.ui.layout.WidgetGroup;
import arc.struct.IntSet;
import arc.util.Log;
import arc.util.Time;
import arc.util.io.ByteBufferOutput;
import arc.util.io.Reads;
import arc.util.io.Writes;
import mindustry.Vars;
import mindustry.core.GameState;
import mindustry.gen.Groups;
import mindustry.net.Net;
import mindustry.net.Packet;
import mindustry.net.Packets;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.zip.DeflaterOutputStream;

import static mindustry.Vars.*;

public class ReplayController {
    public static final int version = 1;
    Writes writes;
    Reads reads;
    long startTime, nowTime;
    long lastTime, nextTime;
    Thread thread;
    Fi dir = Vars.dataDirectory.child("replays");
    DeflaterOutputStream dos;
    ByteBuffer tmpBuf = ByteBuffer.allocate(32768);
    Writes tmpWr = new Writes(new ByteBufferOutput(tmpBuf));
    boolean recording = false, recordEnabled = false;
    float speed = 1f;
    Table controller = new Table();

    public ReplayController() {
        dir.mkdirs();
        thread = new Thread(() -> {
            while (true) {
                if (reads == null) {
                    try {
                        synchronized (thread) {
                            thread.wait();
                        }
                    } catch (InterruptedException ignored) {
                    }
                }
                if (state.getState() != GameState.State.playing && !netClient.isConnecting()) reads = null;
                try {
                    readNextPacket();
                } catch (Exception e) {
                    reads = null;
                    net.disconnect();
                    replaying = false;
                    Core.app.post(() -> logic.reset());
                }
            }
        }, "ReplayController");
        thread.setPriority(3);
        thread.start();
        WidgetGroup g = new WidgetGroup();
        g.addChild(controller);
        g.setFillParent(true);
        g.touchable = Touchable.childrenOnly;
        controller.setFillParent(true);
        Core.scene.add(g);
    }

    public void createReplay(String ip) {
        if (!recordEnabled) return;
        stop();
        try {
            writes = new Writes(new DataOutputStream(dos = new DeflaterOutputStream(new FileOutputStream(dir.child(new Date().getTime() + ".mrep").file()))));
        } catch (FileNotFoundException e) {
            Log.err("创建回放出错!", e);
            return;
        }
        writes.i(version);
        writes.l(new Date().getTime());
        writes.str(ip);
        writes.str(Vars.player.name.trim());
        recording = true;
        startTime = Time.nanos();
    }

    public void stop() {
        recording = false;
        try {
            dos.close();
        } catch (Exception ignored) {
        }
    }

    synchronized public void writePacket(Packet p) {
        if (!recording || p instanceof Packets.WorldStream) return;
        try {
            byte id = Net.getPacketId(p);
            try {
                writes.l(Time.nanos() - startTime);
                writes.b(id);
                tmpBuf.position(0);
                p.write(tmpWr);
                int l = tmpBuf.position();
                writes.s(l);
                writes.b(tmpBuf.array(), 0, l);
            } catch (Exception e) {
                net.disconnect();
                ui.showException("录制出错!", e);
            }
        } catch (Exception ignored) {
        }
    }

    public long timeEscaped() {
        long escaped = (long) ((Time.nanos() - nowTime) * speed);
        nowTime = Time.nanos();
        return escaped;
    }

    synchronized public void readNextPacket() {
        if (timeEscaped() < nextTime) {
            Thread.yield();
            return;
        }
        nextTime = reads.l();
        Packet p = Net.newPacket(reads.b());
        int l = reads.us();
        p.read(reads, l);
        Core.app.post(() -> net.handleClientReceived(p));
    }

    public void shouldRecord(boolean should) {
        recordEnabled = should;
    }

    public boolean shouldRecord() {
        return recordEnabled;
    }

    public void startPlay(Reads r) {
        replaying = true;
        int version = r.i();
        Date time = new Date(r.l());
        String ip = r.str();
        String name = r.str();
        Log.info("version: @, time: @, ip: @, name: @", version, time, ip, name);
        reads = r;
        logic.reset();
        net.reset();
        netClient.beginConnecting();
        Groups.clear();
        try {
            Field f = net.getClass().getDeclaredField("active");
            f.setAccessible(true);
            f.set(net, true);
            Field f2 = netClient.getClass().getDeclaredField("removed");
            f2.setAccessible(true);
            ((IntSet) f2.get(netClient)).clear();
        } catch (Exception e) {
            ui.showException(e);
        }
        ui.loadfrag.show("@connecting");
        ui.loadfrag.setButton(() -> {
            ui.loadfrag.hide();
            netClient.disconnectQuietly();
            reads = null;
        });
        lastTime = Time.nanos();
        synchronized (thread) {
            thread.notify();
        }
    }

    public void stopPlay() {
        reads = null;
        replaying = false;
    }

    public void setSpeed(float s) {
        speed = s;
        Time.setDeltaProvider(() -> Math.min(Core.graphics.getDeltaTime() * 60f * speed, 3f * speed));
    }
}
