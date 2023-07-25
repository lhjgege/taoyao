package com.acgist.taoyao.signal.protocol.room;

import java.util.Map;

import org.springframework.context.ApplicationListener;

import com.acgist.taoyao.boot.annotation.Description;
import com.acgist.taoyao.boot.annotation.Protocol;
import com.acgist.taoyao.boot.config.Constant;
import com.acgist.taoyao.boot.model.Message;
import com.acgist.taoyao.signal.client.Client;
import com.acgist.taoyao.signal.client.ClientType;
import com.acgist.taoyao.signal.event.room.RoomCloseEvent;
import com.acgist.taoyao.signal.party.room.Room;
import com.acgist.taoyao.signal.protocol.ProtocolRoomAdapter;

/**
 * 关闭房间信令
 * 
 * @author acgist
 */
@Protocol
@Description(
    body = """
    {
        "roomId": "房间ID"
    }
    """,
    flow = "终端->信令服务+)终端"
)
public class RoomCloseProtocol extends ProtocolRoomAdapter implements ApplicationListener<RoomCloseEvent> {

    private static final String SIGNAL = "room::close";
    
    public RoomCloseProtocol() {
        super("关闭房间信令", SIGNAL);
    }
    
    @Override
    public void onApplicationEvent(RoomCloseEvent event) {
        final Room room = event.getRoom();
        final Client mediaClient = room.getMediaClient();
        final Map<String, String> body = Map.of(Constant.ROOM_ID, room.getRoomId());
        mediaClient.push(this.build(body));
    }

    @Override
    public void execute(String clientId, ClientType clientType, Room room, Client client, Client mediaClient, Message message, Map<String, Object> body) {
        if(clientType.mediaClient()) {
            room.close();
        } else if(clientType.mediaServer()) {
            room.remove();
            room.broadcast(message);
        } else {
            this.logNoAdapter(clientType);
        }
    }
    
}
