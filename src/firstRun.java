import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.lightcouch.CouchDbClient;
import org.lightcouch.CouchDbProperties;

import com.fourspaces.couchdb.Database;
import com.fourspaces.couchdb.Document;
import com.fourspaces.couchdb.Session;
import com.google.gson.JsonObject;

import starter.NLP;


public class firstRun {


	/**
	 * Usage: java twitter4j.examples.search.SearchTweets [query]
	 *
	 * @param args
	 *            search query
	 * @throws IOException
	 * @throws TwitterException
	 * @throws InterruptedException
	 */
	static Session dbSession;
	static Database db;
	private static CouchDbClient dbClient;
	private static final int MAX_CONNECTIONS = 20;

	public static void main(String[] args) throws IOException,
			InterruptedException {
		
		try {
			NLP nl = new NLP();
			String  database = "sydney_topsy";
			CouchDbProperties properties = new CouchDbProperties()
					.setDbName(database).setCreateDbIfNotExist(true)
					.setProtocol("http").setHost("115.146.93.22").setPort(5984)
					.setMaxConnections(MAX_CONNECTIONS);
			
			dbClient = new CouchDbClient(properties);

			List<JsonObject> list = dbClient.view("story/career")
					.includeDocs(false).reduce(false).group(false).limit(25000).skip(0)
					.query(JsonObject.class);
			
HashMap<String, Integer>Hm = new HashMap<String, Integer>();
	
			
			for (JsonObject s : list) {

				String value = s.get("key").getAsString();
				int sentimentValue = nl.getTweetSentimentValue(value);
				System.out.println(value);
				if (sentimentValue == 0)
				{
					String year =  s.get("value").getAsString()+"Neutral";
					if (Hm.containsKey(year))
					{
						Integer negativeCount = Hm.get(year);
						negativeCount = negativeCount.intValue() + 1;
						Hm.put(year, negativeCount);
					}else
					{
						Hm.put(year, 1);
					}
				}
				else
				if (sentimentValue < 0)
				{
					String year =  s.get("value").getAsString()+"Negative";
					if (Hm.containsKey(year))
					{
						Integer negativeCount = Hm.get(year);
						negativeCount = negativeCount.intValue() + 1;
						Hm.put(year, negativeCount);
					}else
					{
						Hm.put(year, 1);
					}
				}
				else
				{
					String year =  s.get("value").getAsString()+"Positive";
					if (Hm.containsKey(year))
					{
						Integer negativeCount = Hm.get(year);
						negativeCount = negativeCount.intValue() + 1;
						Hm.put(year, negativeCount);
					}else
					{
						Hm.put(year, 1);
					}
				}

			}
			
			System.out.println(database+" second RUN skip 75000");
			Iterator itr = Hm.keySet().iterator();
			while(itr.hasNext())
		    {
				String key = itr.next().toString();
				System.out.println("Year :"+key+"  value "+ Hm.get(key));
		    }

			
		} catch (Exception e) {
			System.exit(-1);
		}
	}
	public static Document tweetToCouchDocument(String s1, String s2) {
		

		Document couchDocument = new Document();
		couchDocument.put("Word", s1);
		couchDocument.put("Count", s2);
		
		return couchDocument;
	}

	

	public static void saveDocument(Document doc) {
		try {
			db.saveDocument(doc);
		} catch (Exception e) {
		}
	}

	public static void createDatabase(String dbName) {
		dbSession = new Session("115.146.93.22", 5984);
		db = dbSession.createDatabase(dbName);
		if (db == null)
			db = dbSession.getDatabase(dbName);
	}

}
