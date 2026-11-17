package ar.edu.uns.cs.ed.tdas.tdadiccionario;

import java.util.Iterator;
import ar.edu.uns.cs.ed.tdas.Entry;
import ar.edu.uns.cs.ed.tdas.Position;
import ar.edu.uns.cs.ed.tdas.excepciones.InvalidEntryException;
import ar.edu.uns.cs.ed.tdas.excepciones.InvalidKeyException;
import ar.edu.uns.cs.ed.tdas.excepciones.InvalidPositionException;
import ar.edu.uns.cs.ed.tdas.tdalista.ListaDoblementeEnlazada;
import ar.edu.uns.cs.ed.tdas.tdalista.PositionList;

public class DiccionarioHashAbierto<K, V> implements Dictionary<K, V> {
	protected static final int DEFAULT_BUCKETS = 11;
	protected int buckets;
	protected PositionList<Entry<K, V>>[] array;
	protected int cantElems;
	
	@SuppressWarnings("unchecked")
	public DiccionarioHashAbierto() {
		this(DEFAULT_BUCKETS);
	}

	@SuppressWarnings("unchecked")
	public DiccionarioHashAbierto(int capacidad) {
		this.buckets = capacidad > 0 ? capacidad : DEFAULT_BUCKETS;
		this.array = (PositionList<Entry<K, V>>[]) new PositionList[this.buckets];
		for (int i = 0; i < this.buckets; i++) {
			this.array[i] = new ListaDoblementeEnlazada<>();
		}
		this.cantElems = 0;
	}
	
	public int size() {
		return cantElems;
	}

	public boolean isEmpty() {
		return cantElems == 0;
	}
	
	public Entry<K, V> find(K key) throws InvalidKeyException {
		if (key == null) {
			throw new InvalidKeyException("Clave nula!");
		}
		int clave = hash(key);
		for (Entry<K, V> entrada : array[clave]) {
			if (entrada.getKey().equals(key)) {
				return entrada;
			}
		}
		return null;
	}
	
	public Iterable<Entry<K, V>> findAll(K key) throws InvalidKeyException {
		if (key == null) {
			throw new InvalidKeyException("Clave nula!");
		}
		PositionList<Entry<K, V>> toRet = new ListaDoblementeEnlazada<>();
		int clave = hash(key);
		for (Entry<K, V> entrada : array[clave]) {
			if (entrada.getKey().equals(key)) {
				toRet.addLast(entrada);
			}
		}
		return toRet;
	}
	
	public Entry<K, V> insert(K key, V value) throws InvalidKeyException {
		if (key == null) {
			throw new InvalidKeyException("Clave nula!");
		}
		int clave = hash(key);
		Entry<K, V> toInsert = new Entrada<>(key, value);
		array[clave].addLast(toInsert);
		cantElems++;
		return toInsert;
	}
	
	public Entry<K, V> remove(Entry<K, V> e) throws InvalidEntryException {
		if (e == null || e.getKey() == null) {
			throw new InvalidEntryException("Entrada invalida!");
		}
		int clave = hash(e.getKey());
		for (Position<Entry<K, V>> pos : array[clave].positions()) {
			if (pos.element().equals(e) || (pos.element().getKey().equals(e.getKey()) && 
			    (pos.element().getValue() == null ? e.getValue() == null : pos.element().getValue().equals(e.getValue())))) {
				try {
					array[clave].remove(pos);
					cantElems--;
					return e;
				} catch (InvalidPositionException ex) {
					throw new InvalidEntryException("Error al remover entrada");
				}
			}
		}
		throw new InvalidEntryException("Entrada no se encuentra en el diccionario!");
	}
	
	public Iterable<Entry<K, V>> entries() {
		PositionList<Entry<K, V>> toRet = new ListaDoblementeEnlazada<>();
		for (int i = 0; i < buckets; i++) {
			for (Entry<K, V> entrada : array[i]) {
				toRet.addLast(entrada);
			}
		}
		return toRet;
	}
	
	protected int hash(K key) {
		return Math.abs(key.hashCode()) % buckets;
	}
}
