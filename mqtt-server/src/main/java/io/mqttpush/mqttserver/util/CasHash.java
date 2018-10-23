package io.mqttpush.mqttserver.util;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.netty.channel.Channel;

/**
 * 
 * @author tianzhenjiu
 *
 * @param <T>
 * @param <K>
 * @param <V>
 */
public class CasHash<T, K extends CasKey<T>, V> extends IdentityHashMap<K, V> {

	Map<T, CasKey<T>> keysMap = new ConcurrentHashMap<>(10000);

	@Override
	public V put(K key, V value) {

		keysMap.putIfAbsent(key.t, key);

		if (!"/root".equals(key.t)) {

			T t = key.getParentKey();

			CasKey<T> parentKey = null;
			if (keysMap.containsKey(t)) {
				parentKey = keysMap.get(t);
			}

			if (parentKey != null) {
				parentKey.childs[parentKey.childWriteIndex++] = key;
			}
		}

		return super.put(key, value);

	}

	@Override
	public boolean remove(Object key, Object value) {

		CasKey<T> casKey = null;
		if (keysMap.containsKey(key)) {
			casKey = keysMap.get(key);
		}

		if (casKey != null) {
			return super.remove(casKey, value);
		}

		return false;
	}

	/**
	 * 获取级联的key
	 * 
	 * @param t
	 * @return
	 */
	public CasKey<T> getCasKey(T t) {
		if (keysMap.containsKey(t)) {
			return keysMap.get(t);
		}

		return null;
	}

	/**
	 * 消费
	 * 
	 * @param tkey
	 * @param consumer
	 */
	public void consume(T tkey, Consumer<V> consumer) {
		CasKey<T> casKey = keysMap.get(tkey);
		List<V> list = new ArrayList<>(32);
		getAndChild(casKey, list);
		list.forEach(consumer);

	}

	/**
	 * 得到所有级联的对象
	 * 
	 * @param key
	 * @param allValues
	 * @return
	 */
	public V getAndChild(CasKey<T> key, List<V> allValues) {

		V v = null;
		if (allValues == null) {
			return null;
		}
		if (key == null) {
			return null;
		}

		if (!containsKey(key)) {
			return null;
		}

		allValues.add(v = super.get(key));

		CasKey<T>[] ks = key.childs;
		for (int i = 0; i < ks.length; i++) {
			getAndChild(ks[i], allValues);
		}

		return v;
	}

	/**
	 * 消费得到所有级联的对象
	 * 
	 * @param key
	 * @param allValues
	 * @return
	 */
	public void getAndConsumeChild(CasKey<T> key, Consumer<V> consumer) {

		if (consumer == null) {
			return;
		}
		if (key == null) {
			return;
		}

		if (!containsKey(key)) {
			return;
		}

		consumer.accept(super.get(key));

		CasKey<T>[] ks = key.childs;
		for (int i = 0; i < ks.length; i++) {
			getAndConsumeChild(ks[i], consumer);
		}

	}

}
