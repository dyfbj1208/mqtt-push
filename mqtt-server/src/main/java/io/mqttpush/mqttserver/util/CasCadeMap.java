package io.mqttpush.mqttserver.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 可以接受父子级别的map
 * 
 * @author tzj
 *
 */
public class CasCadeMap<K, V> extends ConcurrentHashMap<CaseCadeKey<K>, Collection<V>> {

	/**
	 * 用于指定key到级联key的映射
	 */
	ConcurrentHashMap<K, CaseCadeKey<K>> concurrentHashMap = new ConcurrentHashMap<>();
	/**
	 * 
	 */
	private static final long serialVersionUID = -4010313665766496429L;

	CaseCadeKey<K> getCasCadeKey(K k) {

		if (!concurrentHashMap.containsKey(k))
			return null;
		return concurrentHashMap.get(k);
	}

	/**
	 * 根据key获取 所有级联key关联的值
	 * 
	 * @param k
	 * @param vs
	 */
	public void get(K k, Collection<V> vs) {

		CaseCadeKey<K> key = getCasCadeKey(k);
		if (key != null)
			get(key, vs);
	}
	
	/**
	 * 获取到知道value后直接执行任务
	 * @param k
	 * @param action
	 */
	public void get(K k, Consumer<V> action) {

		CaseCadeKey<K> key = getCasCadeKey(k);
		if (key != null)
			get(key, action);
	}

	/**
	 * 获取级联key关键的值已经级联key的子级别关键的值 使用递归获取
	 * 
	 * @param key
	 * @param vs
	 */
	 void get(CaseCadeKey<K> key, Collection<V> vs) {

		if (!containsKey(key))
			key = concurrentHashMap.get(key.kvalue);

		vs.addAll(get(key));

		if (key.childrenKeyFirst != null) {

			Iterator<CaseCadeKey<K>> iterator = key.iterator();

			while (iterator.hasNext()) {
				get(iterator.next(), vs);
			}
		}

	}
	
	/**
	 * 获取了结果直接遍历
	 * @param key
	 * @param action
	 */
	 void get(CaseCadeKey<K> key, Consumer<V> action) {

		if (!containsKey(key))
			key = concurrentHashMap.get(key.kvalue);

		Collection<V> collection=get(key);
		if(collection!=null)
		get(key).forEach(action);

		if (key.childrenKeyFirst != null) {

			Iterator<CaseCadeKey<K>> iterator = key.iterator();

			while (iterator.hasNext()) {
				get(iterator.next(), action);
			}
		}

	}
	
	/**
	 *  根据父级key存放 级联键值对
	 * @param key
	 * @param pkey
	 * @param value
	 * @return
	 */
	public boolean putCasCade(K key,K pkey,V value){
		
		CaseCadeKey<K> pcasekey=getCasCadeKey(pkey);
		
		if(pcasekey!=null){
			
			if(key instanceof String){
				putCasCade((CaseCadeKey<K>) 
						new StringCasCadeKey((String)key,
						(CaseCadeKey<String>) pcasekey),value);
			}else{				
				putCasCade(new CaseCadeKey<K>(key, pcasekey), value);
			}
			return true;
		}
		return false;
	}
	
	/**
	 * 移除指定的key
	 * 有待优化
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean removeCasCade(K key,V value){
		
		CaseCadeKey<K> cadeKey=getCasCadeKey(key);
		if(cadeKey!=null){
			Collection<V> collection=get(cadeKey);
			if(collection!=null)
				return collection.remove(value);
		}
		return false;
	}

	/**
	 * 存放 级联key和 值 对于级联key首先根据级联key的key寻找是否已经包含了 如果已经有了级联key后续操作就使用该级联key
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public void putCasCade(CaseCadeKey<K> key, V value) {

		K kv = key.kvalue;
		
		if (concurrentHashMap.containsKey(kv)){
			key = concurrentHashMap.get(kv);
		}
		else{
			concurrentHashMap.put(key.kvalue, key);
			if (key.parentKey != null) {
				key.parentKey.pushChild(key);
			}
		}

		if(value!=null){
			
			Collection<V> collection=null;
			if(containsKey(key)){
				collection=super.get(key);
			}else{
				super.put(key, collection=new HashSet<>());
			}
			if(collection!=null&&
					!collection.contains(value))
				collection.add(value);
		}
		
		

	}

	public static void main(String[] args) {

//		CasCadeMap<String, Integer> cadeMap = new CasCadeMap<>();
//
//		CaseCadeKey<String> rootKey = new CaseCadeKey<String>("root");
//
//		CaseCadeKey<String> cadeKey1 = new CaseCadeKey<String>("device", rootKey);
//
//		CaseCadeKey<String> cadeKey2 = new CaseCadeKey<String>("user", rootKey);
//
//		CaseCadeKey<String> cadeKey3 = new CaseCadeKey<String>("city", cadeKey1);
//
//		cadeMap.putCasCade(rootKey, null);
//		cadeMap.putCasCade(cadeKey1, 12);
//		cadeMap.putCasCade(cadeKey1, 2);
//		cadeMap.putCasCade(cadeKey2, 3);
//		cadeMap.putCasCade(cadeKey3, 4);
//
//		List<Integer> integers = new ArrayList<>();
//		cadeMap.get("root", (Integer i)->{
//			System.out.print("\t"+i);
//		});
//		
//		cadeMap.removeCasCade("root", 1);
//		System.out.println();
//		cadeMap.get("root", (Integer i)->{
//			System.out.print("\t"+i);
//		});
//		System.out.println(integers);
//		

		CasCadeMap<String, Integer> cadeMap = new CasCadeMap<>();
		
		StringCasCadeKey cadeKey=new StringCasCadeKey("root");
		
		StringCasCadeKey cadeKey2=new StringCasCadeKey("home", cadeKey);
		
		StringCasCadeKey cadeKey4=new StringCasCadeKey("home2", cadeKey);
		
		StringCasCadeKey cadeKey3=new StringCasCadeKey("nodemanager", cadeKey2);
		
		cadeMap.putCasCade(cadeKey, 1);
//		cadeMap.putCasCade(cadeKey, 4);
//		cadeMap.putCasCade(cadeKey2, 2);
//		cadeMap.putCasCade(cadeKey3, 3);
		
		cadeMap.putCasCade("aaa", "/root", 11);
		
		cadeMap.putCasCade("home", "/root", 0);
		
		cadeMap.putCasCade("nodemanager", "/root/home", 2);
		
		cadeMap.putCasCade("nodemanager", "/root/home", 2);

		cadeMap.get("/root", (Integer i)->{
			System.out.print("\t"+i);
		});
	}
}
