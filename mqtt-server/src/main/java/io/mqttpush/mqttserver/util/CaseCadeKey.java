package io.mqttpush.mqttserver.util;

import java.util.Iterator;

/**
 * 具有父子级别的key
 * @author acer
 *
 * @param <K>
 */
public class CaseCadeKey<K> implements AddableKey,Iterable<CaseCadeKey<K>>{

	CaseCadeKey<K> parentKey;
	
	CaseCadeKey<K> childrenKeyFirst;
	
	CaseCadeKey<K> childrenKeyTail;

	K kvalue;
	
	CaseCadeKey<K> nextKey;
	
	public CaseCadeKey(){}
	
	public CaseCadeKey(K k){
		this.kvalue=k;
	}
	
	public CaseCadeKey(K k,CaseCadeKey<K> parentKey){
		this.parentKey=parentKey;
		this.kvalue=k;
	}
	
	@Override
	public AddableKey parentKey() {
	
		return parentKey;
	}

	/**
	 * 遍历子级别
	 */
	@Override
	public Iterator<CaseCadeKey<K>> iterator() {
	
		return new MyIterator(childrenKeyFirst);
	}
	
	public Iterator<CaseCadeKey<K>> iteratorChildChild() {
		
		return new MyIterator(childrenKeyFirst);
	}
	
	/**
	 * 子级别添加
	 * @param currentKey
	 */
	public void pushChild(CaseCadeKey<K> currentKey){
		
		if(childrenKeyTail==null){
			childrenKeyFirst=childrenKeyTail=currentKey;
		}else{
			childrenKeyTail.nextKey=currentKey;
			childrenKeyTail=currentKey;
		}
	}
	
	 class MyIterator implements Iterator<CaseCadeKey<K>>{
		 
		 private CaseCadeKey<K> currentKey;
		 
		 public MyIterator(CaseCadeKey<K> currentKey){
			 this.currentKey=currentKey;
		 }

		 
		@Override
		public boolean hasNext() {
			
			return currentKey!=null;
		}

		@Override
		public CaseCadeKey<K> next() {
		
			CaseCadeKey<K> kv=currentKey;
			currentKey=currentKey.nextKey;
			return kv;
		}
		
	}
	 
	 class MyIteratorChildChild implements Iterator<K>{
		 
		 private CaseCadeKey<K> currentKey;
		 
		 public MyIteratorChildChild(CaseCadeKey<K> currentKey){
			 this.currentKey=currentKey;
		 }

		 
		@Override
		public boolean hasNext() {
			
			return currentKey!=null;
		}

		@Override
		public K next() {
		
			K kv=currentKey.kvalue;
			currentKey=currentKey.nextKey;
			return kv;
		}
		
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return kvalue.hashCode();
	}
	
	 
	
	
}
