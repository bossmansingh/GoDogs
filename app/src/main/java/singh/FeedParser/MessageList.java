package singh.FeedParser;

import android.util.Log;
import android.util.Xml;

import org.xmlpull.v1.XmlSerializer;

import java.io.StringWriter;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class MessageList {
	
	public List<Message> mMessages;

    public ArrayList<HashMap<String, String>> startFunction() {
        return loadFeed(ParserType.ANDROID_SAX);
    }


	private ArrayList<HashMap<String, String>> loadFeed(ParserType type){
    	try{
    		Log.i("AndroidNews", "ParserType=" + type.name());
	    	FeedParser parser = FeedParserFactory.getParser(type);
	    	long start = System.currentTimeMillis();
	    	mMessages = parser.parse();
	    	long duration = System.currentTimeMillis() - start;
	    	Log.i("AndroidNews", "Parser duration=" + duration);
	    	String xml = writeXml();
	    	Log.i("AndroidNews", xml);

	    	ArrayList<HashMap<String, String>> titles =
                    new ArrayList<HashMap<String, String>>(mMessages.size());

            String posted_on;

	    	for (Message msg : mMessages){
                HashMap<String, String> newsPost = new HashMap<String, String>();
	    		newsPost.put("title", msg.getTitle());
                posted_on = msg.getDate();
                SimpleDateFormat sdf1 = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss zzzz");
                sdf1.getTimeZone();
                Date d1 = null;
                try {
                    d1 = sdf1.parse(posted_on);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("MMM d, yyyy   h:mm a");
                String dateWithoutTime = sdf.format(d1);
                posted_on = dateWithoutTime;

                newsPost.put("date", posted_on);
                titles.add(newsPost);
	    	}
            return titles;

    	} catch (Throwable t){
    		Log.e("AndroidNews", t.getMessage(), t);
    	}
        return null;
    }
    
	private String writeXml(){
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "messages");
			serializer.attribute("", "number", String.valueOf(mMessages.size()));
			for (Message msg: mMessages){
				serializer.startTag("", "message");
				serializer.attribute("", "date", msg.getDate());
				serializer.startTag("", "title");
				serializer.text(msg.getTitle());
				serializer.endTag("", "title");
				serializer.startTag("", "url");
				serializer.text(msg.getLink().toExternalForm());
				serializer.endTag("", "url");
				serializer.startTag("", "body");
				serializer.text(msg.getDescription());
				serializer.endTag("", "body");
				serializer.endTag("", "message");
			}
			serializer.endTag("", "messages");
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
	}
}