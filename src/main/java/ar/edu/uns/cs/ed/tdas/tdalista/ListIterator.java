package ar.edu.uns.cs.ed.tdas.tdalista;

import java.util.Iterator;
import java.util.NoSuchElementException;
import ar.edu.uns.cs.ed.tdas.Position;
import ar.edu.uns.cs.ed.tdas.excepciones.*;

public class ListIterator<E> implements Iterator<E> {
	private Position<E> cursor;
	private PositionList<E> list;
	
	public ListIterator(PositionList<E> l) {
		list = l;
		if (!l.isEmpty()) {
			try {
				cursor = l.first();
			} catch (EmptyListException e) {
				cursor = null;
			}
		} else {
			cursor = null;
		}
	}
	
	public boolean hasNext() {
		return cursor != null;
	}

	public E next() throws NoSuchElementException { 	
		if (cursor == null) {
			throw new NoSuchElementException("Error: No hay siguiente");
		}
		E toReturn = cursor.element();  
		try {
			cursor = (cursor == list.last()) ? null : list.next(cursor);
		} catch (EmptyListException | InvalidPositionException | BoundaryViolationException e) {
			cursor = null;
		} 
		return toReturn;
	}
}
