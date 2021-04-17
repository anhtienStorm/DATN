package bkav.android.btalk.backup;

/**
 * The Class info abount one SMS Message.
 */
public class SmsMessage {

	/** The address. */
	private String address;

	/** The person. */
	private long person;

	/** The date. */
	private long date;

	/** The body. */
	private String body;

	/** The type. */
	private int type;

	/**
	 * Instantiates a new msg.
	 */
	public SmsMessage() {
		this.address = null;
		this.person = 0;
		this.body = null;
		this.date = 0;
		this.type = 0;
	}

	/**
	 * Instantiates a new msg.
	 *
	 * @param address the address
	 * @param person the person
	 * @param date the date
	 * @param body the body
	 * @param type the type
	 */
	public SmsMessage(String address, long person, long date, String body, int type) {
		this.address = address;
		this.person = person;
		this.date = date;
		this.body = body;
		this.type = type;
	}

	/**
	 * Sets the address.
	 *
	 * @param address the new address
	 */
	public void setAddress(String address) {
		this.address = address;
	}

	/**
	 * Gets the address.
	 *
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}

	/**
	 * Sets the person.
	 *
	 * @param person the new person
	 */
	public void setPerson(long person) {
		this.person = person;
	}

	/**
	 * Gets the person.
	 *
	 * @return the person
	 */
	public long getPerson() {
		return person;
	}

	/**
	 * Sets the date.
	 *
	 * @param date the new date
	 */
	public void setDate(long date) {
		this.date = date;
	}

	/**
	 * Gets the date.
	 *
	 * @return the date
	 */
	public long getDate() {
		return date;
	}

	/**
	 * Sets the body.
	 *
	 * @param body the new body
	 */
	public void setBody(String body) {
		this.body = body;
	}

	/**
	 * Gets the body.
	 *
	 * @return the body
	 */
	public String getBody() {
		return body;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(int type) {
		this.type = type;
	}

	/**
	 * Gets the type.
	 *
	 * @return the type
	 */
	public int getType() {
		return type;
	}
}
