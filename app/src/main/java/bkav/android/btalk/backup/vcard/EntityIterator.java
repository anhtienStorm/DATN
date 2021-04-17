package bkav.android.btalk.backup.vcard;

import java.util.Iterator;
import android.content.Entity;

// TODO: Auto-generated Javadoc
/**
 * The Interface EntityIterator.
 */
public interface EntityIterator extends Iterator<Entity> {
	
	/**
	 * Reset.
	 */
	public void reset();

	/**
	 * Close.
	 */
	public void close();
}
