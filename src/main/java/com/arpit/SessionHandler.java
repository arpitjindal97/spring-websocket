package com.arpit;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;


public class SessionHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(SessionHandler.class);

    public MongoClient mongoClient = null;
    MongoDatabase db;

    //record of current sessions
    Map<String, WebSocketSession> map = new HashMap<String, WebSocketSession>();

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

                MongoCollection coll = db.getCollection("mappings");
                Document doc = (Document) coll.find(or(eq("first", session.getId()), eq("connected", session.getId()))).
                        first();
                TextMessage new_mes = new TextMessage(
                        "<p class=\"strangermsg\"><strong class=\"msgsource\">Stranger:</strong> <span>" + message.getPayload() + "</span></p>");

                if (doc.getString("first").equals(session.getId())) {
                    map.get(doc.getString("connected")).sendMessage(new_mes);
                } else {
                    map.get(doc.getString("first")).sendMessage(new_mes);
                }
                session.sendMessage(new TextMessage("<p class=\"youmsg\"><strong class=\"msgsource\">You:</strong> <span>" + message.getPayload() + "</span></p>"));

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        logger.info("Connection established ID: "+session.getId());

        map.put(session.getId(), session);

        //disable his inputs
        session.sendMessage(new TextMessage("<p class='statuslog'>Finding some stranger...</p>"));
        session.sendMessage(new TextMessage("server: textarea disabled"));

        MongoCollection coll = db.getCollection("alternate");
        Document doc = (Document) coll.find().first();

        if (doc.getInteger("key") == 0) {

            //make it one
            coll.updateOne(lt("key", 2), set("key", 1));

            //put yourself in db and wait for someone
            coll = db.getCollection("mappings");
            coll.insertOne(new Document("first", session.getId()).append("connected", "no"));
            //client will be able to find partner in next request
        }
        else
        {
            //make it zero
            coll.updateOne(lt("key", 2), set("key", 0));
        }

        //ask client for search_stranger request
        session.sendMessage(new TextMessage("server: key is " + doc.getInteger("key")));
        session.sendMessage(new TextMessage("server: didt found"));

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        try {
            String id = session.getId();
            logger.info("Connection closed ID: " + id);
            map.remove(session.getId());

            MongoCollection coll = db.getCollection("mappings");

            //find your entry to find stranger's id
            Document doc = (Document) coll.find(or(eq("first", id), eq("connected", id))).
                    first();

            //delete this entry
            coll.deleteOne(or(eq("first", id), eq("connected", id)));

            TextMessage stranger = new TextMessage(
                    "<p class=\"statuslog\">Stranger has disconnected</p>");

            //extract the stranger's id from doc
            String stranger_id = "";
            if (doc.getString("first").equals(id)) {
                stranger_id = doc.getString("connected");
            } else {
                stranger_id = doc.getString("first");
            }

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

        try {
            String mongo_host = System.getProperty("localhost");
            int mongo_port = Integer.parseInt(("27017"));
            String mongo_pass_str = "arpit";
            char mongo_pass[] = mongo_pass_str.toCharArray();
            MongoCredential credential = MongoCredential.createCredential("admin", "admin", mongo_pass);

            mongoClient = new MongoClient(new ServerAddress(mongo_host, mongo_port), Arrays.asList(credential));
            MongoDatabase database = mongoClient.getDatabase("arpit");
            MongoCollection collection = database.getCollection("alternate");
            collection.drop();
            collection.insertOne(new Document("key", 0));
            collection = database.getCollection("mappings");
            collection.drop();

            db = mongoClient.getDatabase("arpit");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void search_stranger(WebSocketSession session, int key) {
        try {

            MongoCollection coll = db.getCollection("mappings");

            Document doc;//= (Document)coll.find().first();

            if (key == 1) {
                //search someone from db

                Document query = new Document("connected", "no");

                //search one time only
                doc = (Document) coll.find(query).first();

                if (doc == null) {

                    //client will resend the request after reading this
                    session.sendMessage(new TextMessage("server: didt found"));
                    return;
                }

                //grab your seat if you found something
                String first = (String) doc.get("first");

                query = new Document("first", first);
                query.append("connected", "no");

                coll.updateOne(and(eq("first", first), eq("connected", "no")),
                        set("connected", session.getId()));

            } else {

                //check if someone is sitting beside

                doc = (Document) coll.find(and(eq("first", session.getId()), ne("connected", "no"))).first();

                if (doc == null) {
                    session.sendMessage(new TextMessage("server: didt found"));
                    return;
                }


            }

            //will only be executed after finding partner
            session.sendMessage(new TextMessage(
                    "<p class=\"statuslog\">You're now chatting with a random stranger. Say hi!</p>"));
            session.sendMessage(new TextMessage("server: textarea enabled"));

        } catch (Exception eee) {
            eee.printStackTrace();
        }
    }
}

