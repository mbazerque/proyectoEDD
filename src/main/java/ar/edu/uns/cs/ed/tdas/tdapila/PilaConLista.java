package ar.edu.uns.cs.ed.tdas.tdapila;

import ar.edu.uns.cs.ed.tdas.excepciones.EmptyListException;
import ar.edu.uns.cs.ed.tdas.excepciones.EmptyStackException;
import ar.edu.uns.cs.ed.tdas.tdalista.ListaDoblementeEnlazada;
import ar.edu.uns.cs.ed.tdas.tdalista.PositionList;

public class PilaConLista<E> implements Stack<E> {
	private PositionList<E> list;
	
	public PilaConLista() {
		list = new ListaDoblementeEnlazada<>();
	}
	
	public int size() {
		return list.size();
	}

	public boolean isEmpty() {
		return list.isEmpty();
	}

	public E top() throws EmptyStackException {
		if (isEmpty()) {
			throw new EmptyStackException("La pila esta vacia!");
		}
		try {
			return list.last().element();
		} catch (EmptyListException e) {
			throw new EmptyStackException("La pila esta vacia!");
		}
	}

	public void push(E element) {
		list.addLast(element);
	}

	public E pop() throws EmptyStackException {
		if (isEmpty()) {
			throw new EmptyStackException("La pila esta vacia!");
		}
		try {
			return list.remove(list.last());
		} catch (Exception e) {
			throw new EmptyStackException("La pila esta vacia!");
		}
	}
}
