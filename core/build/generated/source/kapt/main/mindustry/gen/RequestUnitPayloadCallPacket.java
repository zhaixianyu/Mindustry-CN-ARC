package mindustry.gen;

import arc.util.io.Reads;
import arc.util.io.Writes;
import java.lang.Override;

import mindustry.arcModule.ARCVars;
import mindustry.net.NetConnection;
import mindustry.net.Packet;

public class RequestUnitPayloadCallPacket extends Packet {
  private byte[] DATA = NODATA;

  public Player player;

  public Unit target;

  @Override
  public void write(Writes WRITE) {
    if(mindustry.Vars.net.server() || ARCVars.replayController.writing) {
      mindustry.io.TypeIO.writeEntity(WRITE, player);
    }
    mindustry.io.TypeIO.writeUnit(WRITE, target);
  }

  @Override
  public void read(Reads READ, int LENGTH) {
    DATA = READ.b(LENGTH);
  }

  @Override
  public void handled() {
    BAIS.setBytes(DATA);
    if(mindustry.Vars.net.client()) {
      player = mindustry.io.TypeIO.readEntity(READ);
    }
    target = mindustry.io.TypeIO.readUnit(READ);
  }

  @Override
  public void handleServer(NetConnection con) {
    if(con.player == null || con.kicked) {
      return;
    }
    mindustry.gen.Player player = con.player;
    mindustry.input.InputHandler.requestUnitPayload(player, target);
  }

  @Override
  public void handleClient() {
    mindustry.input.InputHandler.requestUnitPayload(player, target);
  }
}
