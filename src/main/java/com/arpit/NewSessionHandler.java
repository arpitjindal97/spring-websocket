package com.arpit;

/*import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.IMap;*/
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class NewSessionHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(NewSessionHandler.class);

    //public HazelcastInstance hzInstance;

    //record of current sessions
    Map<String, WebSocketSession> map = new HashMap<String, WebSocketSession>();
    Map<String,String> alternate;
    Map<String,String> mappings = new HashMap<String,String>();

    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        //logger.info("Received message: " + message.getPayload());
        String received = message.getPayload();

        if (received.equals("give_me_stranger 0")) {

            search_stranger(session,0);

        } else if (received.equals("give_me_stranger 1")) {

            search_stranger(session,1);

        } else {

            //pass the message to appropriate person
            try {

                //Map<String, String> mappings = hzInstance.getMap("mappings");

                TextMessage new_mes = new TextMessage(
                        "<p class=\"strangermsg\"><strong class=\"msgsource\">Stranger:</strong> <span>" + message.getPayload() + "</span></p>");


                for(Map.Entry entry : mappings.entrySet())
                {
                    if(entry.getKey().toString().equals(session.getId()))
                    {
                        map.get(entry.getValue().toString()).sendMessage(new_mes);
                        break;
                    }
                    if(entry.getValue().toString().equals(session.getId()))
                    {
                        map.get(entry.getKey().toString()).sendMessage(new_mes);
                        break;
                    }
                }

                session.sendMessage(new TextMessage("<p class=\"youmsg\"><strong class=\"msgsource\">You:</strong> <span>" + message.getPayload() + "</span></p>"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        //logger.info("Connection established ID: "+session.getId());

        map.put(session.getId(), session);

        //disable his inputs
        session.sendMessage(new TextMessage("<p class='statuslog'>Finding some stranger...</p>"));
        session.sendMessage(new TextMessage("server: textarea disabled"));

        //IMap<String,String> alternate = hzInstance.getMap("alternate");


        for(Map.Entry entry: alternate.entrySet()) {
            String key = entry.getValue().toString();

            session.sendMessage(new TextMessage("server: key is " + key));

            if (key.equals("0")) {

                //make it one
                alternate.put("key","1");

                //put yourself in db and wait for someone
                //Map<String, String> mappings = hzInstance.getMap("mappings");

                mappings.put(session.getId(), "null");
                //client will be able to find partner in next request

            } else {
                //make it zero
                alternate.put("key","0");
            }

            //ask client for search_stranger request
            session.sendMessage(new TextMessage("server: didt found"));
            return;
        }

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            String id = session.getId();
            //logger.info("Connection closed ID: " + id);
            map.remove(id);


            //Map<String,String> alternate = hzInstance.getMap("mappings");

            String stranger_id="";
            for(Map.Entry entry : mappings.entrySet())
            {
                if(entry.getKey().equals(id) )
                {
                    stranger_id = entry.getValue().toString();
                    mappings.remove(id);
                    break;
                }
                if(entry.getValue().equals(id))
                {
                    stranger_id = entry.getKey().toString();
                    mappings.remove(id);
                    break;
                }
            }

            TextMessage stranger = new TextMessage(
                    "<p class=\"statuslog\">Stranger has disconnected</p>");

            //send him disconnecting info
            map.get(stranger_id).sendMessage(stranger);
            map.get(stranger_id).sendMessage(new TextMessage("server: disconnected"));
            map.get(stranger_id).close();
            map.remove(stranger_id);

            //no use of below message, it won't be received by client
            //session.sendMessage(new TextMessage("server: disconnected"));

        } catch (Exception e) {
        }

    }

    public void setConnection() {

        //hzInstance = Hazelcast.newHazelcastInstance();
        alternate = new HashMap<String,String>();
        alternate.put("key","1");
    }

    public void search_stranger(WebSocketSession session, int key) {
        try {
            //Map<String,String> alternate = hzInstance.getMap("mappings");

            if (key == 1) {
                //search someone from db

                //search one time only
                for(Map.Entry entry : mappings.entrySet())
                {
                    if(entry.getValue().equals("null"))
                    {
                        mappings.put(entry.getKey().toString(),session.getId());

                        //will only be executed after finding partner
                        session.sendMessage(new TextMessage(
                                "<p class=\"statuslog\">You're now chatting with a random stranger. Say hi!</p>"));
                        session.sendMessage(new TextMessage("server: textarea enabled"));

                        return;
                    }
                }

                //client will resend the request after reading this
                session.sendMessage(new TextMessage("server: didt found"));
                return;

            } else {

                //check if someone is sitting beside

                String value = mappings.get(session.getId());

                if (value.equals("null")) {
                    session.sendMessage(new TextMessage("server: didt found"));
                    return;
                }

                //will only be executed after finding partner
                session.sendMessage(new TextMessage(
                        "<p class=\"statuslog\">You're now chatting with a random stranger. Say hi!</p>"));
                session.sendMessage(new TextMessage("server: textarea enabled"));

            }


        } catch (Exception eee) {
            eee.printStackTrace();
        }
    }
}

