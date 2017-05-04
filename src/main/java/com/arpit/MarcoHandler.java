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

import javax.xml.soap.Text;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.set;

@Component
public class MarcoHandler extends TextWebSocketHandler 
{
	private static final Logger logger = LoggerFactory.getLogger(MarcoHandler.class);

	public MongoClient mongoClient=null;

	Map<String,WebSocketSession> map = new HashMap<String, WebSocketSession>();

	protected void handleTextMessage( WebSocketSession session, TextMessage message) throws Exception 
	{
		//logger.info("Received message: " + message.getPayload());
		//String received = message.getPayload();
		//Thread.sleep(2000);
		try {
			MongoDatabase db = mongoClient.getDatabase("arpit");
			MongoCollection coll = db.getCollection("mappings");

			Document doc = (Document)coll.find(or(eq("first",session.getId()),eq("connected",session.getId()))).
					first();
			TextMessage new_mes = new TextMessage(
			        "<p class=\"strangermsg\"><strong class=\"msgsource\">Stranger:</strong> <span>"+message.getPayload()+"</span></p>");

			if(doc.getString("first").equals(session.getId()))
			{
				map.get(doc.getString("connected")).sendMessage(new_mes);
			}
			else
			{
				map.get(doc.getString("first")).sendMessage(new_mes);
			}
			session.sendMessage(new TextMessage("<p class=\"youmsg\"><strong class=\"msgsource\">You:</strong> <span>"+message.getPayload()+"</span></p>"));

		}catch(Exception e)
		{e.printStackTrace();}

	}
	
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception 
	{
		logger.info("Connection established");

		map.put(session.getId(),session);
		session.sendMessage(new TextMessage("<p class='statuslog'>Finding some stranger...</p>"));
		session.sendMessage(new TextMessage("server: textarea disabled"));
		Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    MongoDatabase db = mongoClient.getDatabase("arpit");
                    MongoCollection coll = db.getCollection("alternate");

                    Document doc = (Document)coll.find().first();

                    if(doc.getInteger("key") == 1)
                    {
                        //search someone from db

                        //make key zero
                        coll.updateOne(lt("key",2),set("key",0));

                        coll = db.getCollection("mappings");
                        Document query = new Document("connected", "no");
                        do {

                            doc = (Document)coll.find(query).first();
                        }while(doc == null);

                        String first = (String)doc.get("first");

                        query = new Document("first",first);
                        query.append("connected","no");

                        coll.updateOne(and(eq("first",first),eq("connected","no")),
                                set("connected",session.getId()));

                        //session.sendMessage(new TextMessage("connected to "+first));
                    }
                    else
                    {
                        //make it one
                        coll.updateOne(lt("key",2),set("key",1));

                        //put this in db and wait for someone
                        coll = db.getCollection("mappings");
                        coll.insertOne(new Document("first",session.getId()).append("connected","no"));


                        doc = null;
                        while(doc == null)
                        {
                            doc = (Document)coll.find(and(eq("first",session.getId()),ne("connected","no"))).first();
                        }

                        //session.sendMessage(new TextMessage("connected to "+ doc.get("connected")));
                    }
                    session.sendMessage(new TextMessage(
                            "<p class=\"statuslog\">You're now chatting with a random stranger. Say hi!</p>"));

                    session.sendMessage(new TextMessage("server: textarea enabled"));
                } catch (Exception eee)
                {
                    eee.printStackTrace();
                }
            }
        });
        thread.run();
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception
	{
	    try {
            String id = session.getId();
            logger.info("Connection closed. ID: " + id);
            map.remove(session.getId());
            MongoDatabase db = mongoClient.getDatabase("arpit");
            MongoCollection coll = db.getCollection("mappings");

            Document doc = (Document) coll.find(or(eq("first", id), eq("connected", id))).
                    first();

            TextMessage stranger = new TextMessage(
                    "<p class=\"statuslog\">Stranger has disconnected</p>");

            TextMessage you = new TextMessage("<p class=\"statuslog\">You disconnected.</p>");

            String stranger_id="";
            if (doc.getString("first").equals(id))
            {
                stranger_id = doc.getString("connected");
            }
            else
            {
                stranger_id = doc.getString("first");
            }
            map.get(stranger_id).sendMessage(stranger);
            map.get(stranger_id).sendMessage(new TextMessage("server: disconnected"));

            map.get(stranger_id).close();
            map.remove(stranger_id);
            coll.deleteOne(or(eq("first", id), eq("connected", id)));

            //session.sendMessage(you);
        }catch (Exception e){}

	}
	public void setConnection()
	{
		try
		{
			String mongo_host = System.getProperty("localhost");
			int mongo_port = Integer.parseInt(("27017"));
			String mongo_pass_str="arpit";
			char mongo_pass[] = mongo_pass_str.toCharArray();
			MongoCredential credential = MongoCredential.createCredential("admin", "admin",mongo_pass );

			mongoClient = new MongoClient(new ServerAddress(mongo_host,mongo_port), Arrays.asList(credential));
			MongoDatabase database = mongoClient.getDatabase("arpit");
			MongoCollection collection = database.getCollection("alternate");
			collection.drop();
			collection.insertOne(new Document("key",0));
            collection = database.getCollection("mappings");
            collection.drop();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
