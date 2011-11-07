package de.lmu.ifi.dbs.elki.utilities.datastructures.heap;

/*
 This file is part of ELKI:
 Environment for Developing KDD-Applications Supported by Index-Structures

 Copyright (C) 2011
 Ludwig-Maximilians-Universität München
 Lehr- und Forschungseinheit für Datenbanksysteme
 ELKI Development Team

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.util.Comparator;
import java.util.HashMap;

/**
 * A heap as used in OPTICS that allows updating entries.
 * 
 * @author Erich Schubert
 * 
 * @param <O> object type
 */
public class UpdatableHeap<O> extends Heap<O> {
  /**
   * Serial version
   */
  private static final long serialVersionUID = 1L;

  /**
   * Holds the indices in the heap of each element.
   */
  private HashMap<Object, Integer> index = new HashMap<Object, Integer>();

  /**
   * Simple constructor with default size.
   */
  public UpdatableHeap() {
    super();
  }

  /**
   * Constructor with predefined size.
   * 
   * @param size Size
   */
  public UpdatableHeap(int size) {
    super(size);
  }

  /**
   * Constructor with comparator
   * 
   * @param comparator Comparator
   */
  public UpdatableHeap(Comparator<? super O> comparator) {
    super(comparator);
  }

  /**
   * Constructor with predefined size and comparator
   * 
   * @param size Size
   * @param comparator Comparator
   */
  public UpdatableHeap(int size, Comparator<? super O> comparator) {
    super(size, comparator);
  }

  @Override
  public void clear() {
    super.clear();
    index.clear();
  }

  @Override
  public boolean offer(O e) {
    Integer pos = index.get(e);
    if(pos == null) {
      // LoggingUtil.logExpensive(Level.INFO, "Inserting: "+e);
      // insert
      return super.offer(e);
    }
    else {
      // update
      if(compareExternal(e, pos) < 0) {
        // LoggingUtil.logExpensive(Level.INFO,
        // "Updating value: "+e+" vs. "+castQueueElement(pos));
        heapifyUp(pos, e);
        modCount++;
        // We have changed - return true according to {@link Collection#put}
        return true;
      }
      else {
        // LoggingUtil.logExpensive(Level.INFO,
        // "Keeping value: "+e+" vs. "+castQueueElement(pos));
        // Ignore, no improvement. Return success anyway.
        return true;
      }
    }
  }

  @Override
  protected O removeAt(int pos) {
    O node = super.removeAt(pos);
    // Keep index up to date
    index.remove(node);
    return node;
  }

  /**
   * Remove the given object from the queue.
   * 
   * @param e Obejct to remove
   * @return Existing entry
   */
  public O removeObject(O e) {
    Integer pos = index.get(e);
    if(pos != null) {
      return removeAt(pos);
    }
    else {
      return null;
    }
  }

  @Override
  public O poll() {
    O node = super.poll();
    index.remove(node);
    return node;
  }

  /**
   * Execute a "Heapify Upwards" aka "SiftUp". Used in insertions.
   * 
   * @param pos insertion position
   * @param elem Element to insert
   */
  @SuppressWarnings("unchecked")
  protected void heapifyUpComparable(int pos, O elem) {
    final Comparable<Object> cur = (Comparable<Object>) elem; // queue[pos];
    while(pos > 0) {
      final int parent = (pos - 1) >>> 1;
      Object par = queue[parent];
  
      if(cur.compareTo(par) >= 0) {
        break;
      }
      queue[pos] = par;    
      index.put(par, pos);
      pos = parent;
    }
    queue[pos] = cur;    
    index.put(cur, pos);
  }

  /**
   * Execute a "Heapify Upwards" aka "SiftUp". Used in insertions.
   * 
   * @param pos insertion position
   * @param cur Element to insert
   */
  protected void heapifyUpComparator(int pos, O cur) {
    while(pos > 0) {
      final int parent = (pos - 1) >>> 1;
      Object par = queue[parent];
  
      if(comparator.compare(cur, par) >= 0) {
        break;
      }
      queue[pos] = par;    
      index.put(par, pos);
      pos = parent;
    }
    queue[pos] = cur;    
    index.put(cur, pos);
  }

  /**
   * Execute a "Heapify Downwards" aka "SiftDown". Used in deletions.
   * 
   * @param pos re-insertion position
   */
  @SuppressWarnings("unchecked")
  protected void heapifyDownComparable(int pos, Object reinsert) {
    Comparable<Object> cur = (Comparable<Object>) reinsert;
    final int half = size >>> 1;
    while(pos < half) {
      // Get left child (must exist!)
      int cpos = (pos << 1) + 1;
      Object child = queue[cpos];
      // Test right child, if present
      final int rchild = cpos + 1;
      if(rchild < size) {
        Object right = queue[rchild];
        if(((Comparable<Object>)child).compareTo(right) > 0) {
          cpos = rchild;
          child = right;
        }
      }
      
      if(cur.compareTo(child) <= 0) {
        break;
      }
      queue[pos] = child;    
      index.put(child, pos);
      pos = cpos;
    }
    queue[pos] = cur;    
    index.put(cur, pos);
  }

  /**
   * Execute a "Heapify Downwards" aka "SiftDown". Used in deletions.
   * 
   * @param pos re-insertion position
   */
  protected void heapifyDownComparator(int pos, Object cur) {
    final int half = size >>> 1;
    while(pos < half) {
      int min = pos;
      Object best = cur;
  
      final int lchild = (pos << 1) + 1;
      Object left = queue[lchild];
      if(comparator.compare(best, left) > 0) {
        min = lchild;
        best = left;
      }
      final int rchild = lchild + 1;
      if(rchild < size) {
        Object right = queue[rchild];
        if(comparator.compare(best, right) > 0) {
          min = rchild;
          best = right;
        }
      }
      if(min == pos) {
        break;
      }
      queue[pos] = best;    
      index.put(best, pos);
      pos = min;
    }
    queue[pos] = cur;    
    index.put(cur, pos);
  }
}