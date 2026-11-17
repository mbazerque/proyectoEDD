package ar.edu.uns.cs.ed.tdas.tdaarbol;

import ar.edu.uns.cs.ed.tdas.Position;
import ar.edu.uns.cs.ed.tdas.tdalista.ListaDoblementeEnlazada;
import ar.edu.uns.cs.ed.tdas.tdalista.PositionList;

public class TNode<E> implements Position<E> {
	private TNode<E> father;
	private PositionList<TNode<E>> childrens;
	private E element;
	
	public TNode(E element, TNode<E> father) {
		this.father = father;
		this.element = element;
		this.childrens = new ListaDoblementeEnlazada<>();
	}
	
	public TNode(E element) {
		this(element, null);
	}
	
	public E element() {
		return element;
	}

	public TNode<E> getFather() {
		return father;
	}

	public void setFather(TNode<E> father) {
		this.father = father;
	}

	public PositionList<TNode<E>> getChildrens() {
		return childrens;
	}

	public void setChildrens(PositionList<TNode<E>> childrens) {
		this.childrens = childrens;
	}

	public void setElement(E element) {
		this.element = element;
	}
}
