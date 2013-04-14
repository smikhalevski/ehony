/**
 * ┌──┐
 * |  |
 * |Eh|ony
 * └──┘
 */
package org.ehony;

import com.mongodb.*;
import com.mongodb.util.JSON;
import org.slf4j.*;

import javax.jms.*;
import java.net.URLEncoder;
import java.util.*;

/**
 * When {@link Runnable} is started it connects to configured
 * broker subject and starts listening to its events.
 */
public class JmsConsumer implements MessageListener, ExceptionListener {

    private Mongo mongo;
    private MongoURI uri;
    private DBCollection collection;
    private Logger logger = LoggerFactory.getLogger(JmsConsumer.class);

    public void setMongo(Mongo mongo) {
        this.mongo = mongo;
    }

    public void setUri(MongoURI uri) {
        this.uri = uri;
    }

    public DBCollection getCollection() {
        if (collection == null) {
            collection = mongo.getDB(uri.getDatabase()).getCollection(uri.getCollection());
        }
        return collection;
    }

    private String sanitize(String input) {
        return input;
    }

    private DBObject normalize(DBObject o) {
        Set<String> keys = new HashSet<String>(o.keySet());
        for (String key : keys) {
            try {
                String newKey = URLEncoder.encode(key, "utf-8");
                if (!key.equals(newKey) && keys.contains(newKey)) {
                    int i = 1;
                    while (keys.contains(newKey + i)) {
                        i++;
                    }
                    newKey += i;
                }
                Object value = o.get(key);
                if (value instanceof DBObject) {
                    value =  normalize((DBObject)value);
                }
                if (value instanceof List) {
                    List<DBObject> list = new ArrayList<DBObject>();
                    for (DBObject item : (List<DBObject>)value) {
                        list.add(normalize(item));
                    }
                    value = list;
                }
                if (key.equals(newKey)) {
                    o.put(key, value);
                } else {
                    o.removeField(key);
                    o.put(newKey, value);
                }
            } catch (Exception e) {
                logger.error("Cannot normalize key: " + key);
            }
        }
        return o;
    }

    @Override
    public void onException(JMSException e) {
        logger.error("Exception on message broker occurred.", e);
    }

    @Override
    public void onMessage(Message message) {
        if (message instanceof TextMessage) {
            try {
                String input = sanitize(((TextMessage)message).getText());
                logger.debug(input);
                DBObject records = normalize((DBObject)JSON.parse(input));
                if (records instanceof List) {
                    for (Object record : (List)records) {
                        getCollection().insert((DBObject)record);
                    }
                } else {
                    getCollection().insert(records);
                }
            } catch (Exception e) {
                logger.error("Database insert failed.", e);
            }
        }
    }
}
