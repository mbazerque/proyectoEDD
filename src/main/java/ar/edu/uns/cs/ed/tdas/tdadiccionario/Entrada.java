package ar.edu.uns.cs.ed.tdas.tdadiccionario;

import ar.edu.uns.cs.ed.tdas.Entry;

public class Entrada<K, V> implements Entry<K, V> {
	private K key;
	private V value;
	
	public Entrada(K k, V v) {
		key = k;
		value = v;
	}

	public K getKey() {
		return key;
	}

	public void setKey(K key) {
		this.key = key;
	}

	public V getValue() {
		return value;
	}

	public void setValue(V value) {
		this.value = value;
	}
	
	@Override
	public String toString() {
		return "(" + key + "," + value + ")";
	}
}
