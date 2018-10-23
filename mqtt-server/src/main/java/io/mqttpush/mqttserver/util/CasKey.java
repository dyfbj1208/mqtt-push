package io.mqttpush.mqttserver.util;

public abstract class CasKey<T> {

	
	protected T t;
	protected CasKey<T>[] childs;
	protected CasKey<T> parentKey;
	protected int childReadIndex;
	protected int childWriteIndex;

	
	public CasKey(T t) {
		super();
		childs=new CasKey[16];
		this.t = t;
	}
	
	public abstract T getParentKey();

	@Override
	public int hashCode() {
	
		
		return  t.hashCode();
	}
}
