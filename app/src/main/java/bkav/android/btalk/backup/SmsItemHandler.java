package bkav.android.btalk.backup;

import java.util.ArrayList;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The Class SmsItemHandler. To get sms content from xml document
 * 
 * @author DuyPN-Bkis
 * 
 */
public class SmsItemHandler extends DefaultHandler {

    /** The address. */
    public static String ADDRESS_TAG = "a";

    /** The body. */
    public static String BODY_TAG = "b";

    /** The body tag have been patched error for prior 3.0.10.27 */
    public static String BODY_TG = "body";

    /** The type. */
    public static String TYPE_TAG = "t";

    /** The type tag have been patched error for prior 3.0.10.27 */
    public static String TYPE_TG = "type";

    /** The person. */
    public static String PERSON_TAG = "p";

    /** The date. */
    public static String DATE_TAG = "d";

    /** The sms. */
    public static String SMS_TAG = "s";

    /** The msg. */
    private SmsMessage msg = null;

    /** The Lst msg. */
    private ArrayList<SmsMessage> LstMsg = new ArrayList<SmsMessage>();

    /** The Tag content */
    StringBuilder tmpContent;

    /**
     * Gets the msgs.
     * 
     * @return the msgs
     */
    public ArrayList<SmsMessage> getMsgs() {
        return this.LstMsg;
    }

    @Override
    public void startDocument() throws SAXException {

    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    @Override
    public void startElement(String n, String l, String q, Attributes a)
            throws SAXException {

        if (SMS_TAG.equals(q)) {
            msg = new SmsMessage();
        }
        tmpContent = new StringBuilder();
    }

    @Override
    public void endElement(String n, String l, String q) throws SAXException {

        if (SMS_TAG.equals(q)) {
            LstMsg.add(msg);
        } else if (ADDRESS_TAG.equals(q)) {
            msg.setAddress(tmpContent.toString());
        } else if (BODY_TAG.equals(q) || BODY_TG.equals(q)) {
            msg.setBody(tmpContent.toString());
        } else if (PERSON_TAG.equals(q)) {
            msg.setPerson(Long.parseLong(tmpContent.toString()));
        } else if (TYPE_TAG.equals(q) || TYPE_TG.equals(q)) {
            msg.setType(Integer.parseInt(tmpContent.toString()));
        } else if (DATE_TAG.equals(q)) {
            msg.setDate(Long.parseLong(tmpContent.toString()));
        }

    }

    @Override
    public void characters(char ch[], int start, int length) {
        // tmpContent = String.copyValueOf(ch, start, length).trim();
        if (tmpContent != null) {
            for (int i = start; i < start + length; i++) {
                tmpContent.append(ch[i]);
            }
        }
    }
}