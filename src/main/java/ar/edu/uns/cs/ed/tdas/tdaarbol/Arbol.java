package ar.edu.uns.cs.ed.tdas.tdaarbol;

import java.util.Iterator;
import ar.edu.uns.cs.ed.tdas.Position;
import ar.edu.uns.cs.ed.tdas.excepciones.*;
import ar.edu.uns.cs.ed.tdas.tdalista.ListaDoblementeEnlazada;
import ar.edu.uns.cs.ed.tdas.tdalista.PositionList;

public class Arbol<E> implements Tree<E> {
	protected TNode<E> root;
	protected int cantElems;
	
	public Arbol() {
		root = null;
		cantElems = 0;
	}
	
	public int size() {
		return cantElems;
	}

	public boolean isEmpty() {
		return cantElems == 0;
	}

	public Iterator<E> iterator() {
		PositionList<E> toRet = new ListaDoblementeEnlazada<>();
		if (!isEmpty()) {
			preOrder(root, toRet);
		}
		return toRet.iterator();
	}

	public Iterable<Position<E>> positions() {
		PositionList<Position<E>> toRet = new ListaDoblementeEnlazada<>();
		if (!isEmpty()) {
			preOrderPos(root, toRet);
		}
		return toRet;
	}

	public E replace(Position<E> v, E e) throws InvalidPositionException {
		TNode<E> node = checkPosition(v);
		E toRet = node.element();
		node.setElement(e);
		return toRet;
	}

	public Position<E> root() throws EmptyTreeException {
		if (isEmpty()) {
			throw new EmptyTreeException("Arbol vacio!");
		}
		return root;
	}

	public Position<E> parent(Position<E> v) throws InvalidPositionException, BoundaryViolationException {
		TNode<E> node = checkPosition(v);
		if (node == root) {
			throw new BoundaryViolationException("La raiz no tiene padre!");
		}
		return node.getFather();
	}

	public Iterable<Position<E>> children(Position<E> v) throws InvalidPositionException {
		TNode<E> node = checkPosition(v);
		PositionList<Position<E>> toRet = new ListaDoblementeEnlazada<>();
		for (TNode<E> child : node.getChildrens()) {
			toRet.addLast(child);
		}
		return toRet;
	}

	public boolean isInternal(Position<E> v) throws InvalidPositionException {
		TNode<E> node = checkPosition(v);
		return !node.getChildrens().isEmpty();
	}

	public boolean isExternal(Position<E> v) throws InvalidPositionException {
		TNode<E> node = checkPosition(v);
		return node.getChildrens().isEmpty();
	}

	public boolean isRoot(Position<E> v) throws InvalidPositionException {
		TNode<E> node = checkPosition(v);
		return node == root;
	}

	public void createRoot(E e) throws InvalidOperationException {
		if (root != null) {
			throw new InvalidOperationException("El arbol ya tiene raiz!");
		}
		root = new TNode<>(e);
		cantElems++;
	}

	public Position<E> addFirstChild(Position<E> p, E e) throws InvalidPositionException {
		TNode<E> node = checkPosition(p);
		TNode<E> newNode = new TNode<>(e, node);
		node.getChildrens().addFirst(newNode);
		cantElems++;
		return newNode;
	}

	public Position<E> addLastChild(Position<E> p, E e) throws InvalidPositionException {
		TNode<E> node = checkPosition(p);
		TNode<E> newNode = new TNode<>(e, node);
		node.getChildrens().addLast(newNode);
		cantElems++;
		return newNode;
	}

	public Position<E> addBefore(Position<E> p, Position<E> rb, E e) throws InvalidPositionException {
		TNode<E> node = checkPosition(p);
		TNode<E> nodeRB = checkPosition(rb);
		if (nodeRB.getFather() != node) {
			throw new InvalidPositionException("El hermano derecho no pertenece al padre");
		}
		TNode<E> newNode = new TNode<>(e, node);
		PositionList<TNode<E>> childrens = node.getChildrens();
		for (Position<TNode<E>> pos : childrens.positions()) {
			if (pos.element() == nodeRB) {
				childrens.addBefore(pos, newNode);
				cantElems++;
				return newNode;
			}
		}
		throw new InvalidPositionException("Hermano no encontrado");
	}

	public Position<E> addAfter(Position<E> p, Position<E> lb, E e) throws InvalidPositionException {
		TNode<E> node = checkPosition(p);
		TNode<E> nodeLB = checkPosition(lb);
		if (nodeLB.getFather() != node) {
			throw new InvalidPositionException("El hermano izquierdo no pertenece al padre");
		}
		TNode<E> newNode = new TNode<>(e, node);
		PositionList<TNode<E>> childrens = node.getChildrens();
		for (Position<TNode<E>> pos : childrens.positions()) {
			if (pos.element() == nodeLB) {
				childrens.addAfter(pos, newNode);
				cantElems++;
				return newNode;
			}
		}
		throw new InvalidPositionException("Hermano no encontrado");
	}

	public void removeExternalNode(Position<E> p) throws InvalidPositionException {
		TNode<E> node = checkPosition(p);
		if (!isExternal(node)) {
			throw new InvalidPositionException("El nodo no es externo");
		}
		if (node == root) {
			root = null;
			cantElems = 0;
		} else {
			PositionList<TNode<E>> childrens = node.getFather().getChildrens();
			for (Position<TNode<E>> pos : childrens.positions()) {
				if (pos.element() == node) {
					childrens.remove(pos);
					node.setElement(null);
					node.setFather(null);
					cantElems--;
					return;
				}
			}
			throw new InvalidPositionException("La posicion no pertenece al arbol");
		}
	}

	public void removeInternalNode(Position<E> p) throws InvalidPositionException {
		TNode<E> node = checkPosition(p);
		if (!isInternal(node)) {
			throw new InvalidPositionException("El nodo no es interno");
		}
		if (node == root) {
			if (node.getChildrens().size() > 1) {
				throw new InvalidPositionException("La raiz tiene mas de un hijo");
			}
			try {
				Position<TNode<E>> posChild = node.getChildrens().first();
				root = posChild.element();
				node.setElement(null);
				node.getChildrens().remove(posChild);
				root.setFather(null);
				cantElems--;
			} catch (EmptyListException e) {
				// empty list
			}
		} else {
			TNode<E> father = node.getFather();
			PositionList<TNode<E>> childrensF = father.getChildrens();
			Position<TNode<E>> targetPos = null;
			for (Position<TNode<E>> pos : childrensF.positions()) {
				if (pos.element() == node) {
					targetPos = pos;
					break;
				}
			}
			if (targetPos == null) {
				throw new InvalidPositionException("El nodo no figura como hijo de su padre");
			}
			for (TNode<E> child : node.getChildrens()) {
				child.setFather(father);
				childrensF.addBefore(targetPos, child);
			}
			childrensF.remove(targetPos);
			node.setElement(null);
			node.setFather(null);
			cantElems--;
		}
	}

	public void removeNode(Position<E> p) throws InvalidPositionException {
		if (isExternal(p)) {
			removeExternalNode(p);
		} else {
			removeInternalNode(p);
		}
	}

	private void preOrder(TNode<E> node, PositionList<E> list) {
		list.addLast(node.element());
		for (TNode<E> child : node.getChildrens()) {
			preOrder(child, list);
		}
	}

	private void preOrderPos(TNode<E> node, PositionList<Position<E>> list) {
		list.addLast(node);
		for (TNode<E> child : node.getChildrens()) {
			preOrderPos(child, list);
		}
	}

	private TNode<E> checkPosition(Position<E> p) throws InvalidPositionException {
		if (p == null || isEmpty()) {
			throw new InvalidPositionException("Posicion invalida!");
		}
		try {
			return (TNode<E>) p;
		} catch (ClassCastException e) {
			throw new InvalidPositionException("Error de casteo!");
		}
	}
}
