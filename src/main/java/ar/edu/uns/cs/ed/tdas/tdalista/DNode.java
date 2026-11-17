package ar.edu.uns.cs.ed.tdas.tdalista;

import ar.edu.uns.cs.ed.tdas.Position;

public class DNode<E> implements Position<E> {
	private E element;
	private DNode<E> next, prev;
	
	public DNode(E element, DNode<E> next, DNode<E> prev) {
		this.element = element;
		this.next = next;
		this.prev = prev;
	}

	public E element() {
		return element;
	}

	public void setElement(E element) {
		this.element = element;
	}

	public DNode<E> getNext() {
		return next;
	}

	public void setNext(DNode<E> next) {
		this.next = next;
	}

	public DNode<E> getPrev() {
		return prev;
	}

	public void setPrev(DNode<E> prev) {
		this.prev = prev;
	}
}
