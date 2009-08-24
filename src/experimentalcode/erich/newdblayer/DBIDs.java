package experimentalcode.erich.newdblayer;

import java.util.Collection;
import java.util.Iterator;

/**
 * Interface for a collection of database references (IDs).
 * 
 * @author Erich Schubert
 */
public interface DBIDs extends Iterable<DBID> {
  /**
   * Retrieve collection access to the IDs
   * 
   * @return a collection of IDs
   */
  public Collection<DBID> asCollection();

  /**
   * Retrieve Iterator access to the IDs.
   * 
   * @return an iterator for the IDs
   */
  public Iterator<DBID> iterator();

  /**
   * Retrieve the collection / data size.
   * 
   * @return collection size
   */
  public int size();
}
