//package io.mqttpush.mqttserver.service;
//
//import java.io.Serializable;
//import java.lang.reflect.Field;
//import java.lang.reflect.Method;
//import java.lang.reflect.Modifier;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.HashMap;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Map.Entry;
//import java.util.concurrent.ConcurrentHashMap;
//
//import org.springframework.beans.factory.annotation.Autowired;
//
//import redis.clients.jedis.Jedis;
//import redis.clients.jedis.JedisPool;
///**
// * 
// * 对redis操作的服务
// * @author acer
// *
// */
//public class RedisService {
//
//	@Autowired
//	JedisPool jedisPool;
//	
//	private String auth;
//	
//	SimpleDateFormat dateFormat=new SimpleDateFormat("yyy-MM-dd HH:mm:ss");
//	
//	ConcurrentHashMap<Class, Map<String, Method>> entyGmethod = new ConcurrentHashMap<Class, Map<String, Method>>();
//	
//	
//	ConcurrentHashMap<Class, Map<String, Method>> entySmethod = new ConcurrentHashMap<Class, Map<String, Method>>();
//	
//	
//	
//	protected  void  saveSerializable(String group,String iden,Serializable serializable){
//		if(iden==null||group==null)
//			throw new NullPointerException("group  或 iden 为空");
//			 
//		Jedis jedis=getredis();
//		jedis.hmset((group+iden).getBytes(), object2Map(serializable));
//		jedis.close();
//		
//		
//	}
//	
//	/**
//	 * 保存list
//	 * @param group 分组
//	 * @param strings 要保存的集合
//	 */
//	protected void saveList(String group,String item){
//		if(group==null)
//			throw new NullPointerException("group  为空");
//		
//		Jedis jedis=getredis();
//		jedis.lpush(group, item);
//		jedis.close();
//	}
//
//	
//	protected String popList(String group){
//		
//		Jedis jedis=getredis();
//		String item =jedis.rpop(group);
//		jedis.close();
//		return item;
//	}
//	
//	/**
//	 * 保存map
//	 * @param group
//	 * @param iden
//	 * @param map 
//	 */
//	protected void  saveMap(String group,String iden,Map<String, String> map){
//		if(iden==null||group==null)
//			throw new NullPointerException("group  或 iden 为空");
//
//		Jedis jedis=getredis();
//		jedis.hmset(group+iden, map);
//		jedis.close();
//	}
//	
//	/**
//	 *根据key查找Hash
//	 * @param keys
//	 * @return
//	 * @throws Exception 
//	 */
//	protected  <T> T findObj(Class<T> class1,String key) throws Exception{
//		
//		Jedis jedis=getredis();
//		jedis.close();
//		return	map2Obj(jedis.hgetAll(key.getBytes()),class1);
//
//	}
//	
//	/**
//	 * 对象所有没有的字段生成map
//	 * @param serializable
//	 * @return
//	 */
//	public Map<byte[], byte[]> object2Map(Serializable serializable){
//		
//		if(serializable==null)
//			throw new NullPointerException("serializable不能空");
//		
//		Map<byte[], byte[]> map=new HashMap<>();
//		Class<?> class1=serializable.getClass();
//		Field[] fields=class1.getDeclaredFields();
//		
//		try {
//			for (int i = 0; i < fields.length; i++) {
//				if(Modifier.isStatic(fields[i].getModifiers()))
//					continue;
//				
//				String key=fields[i].getName();
//				Object value=getGmethodFromField(class1, key).invoke(serializable);
//				
//				if(value!=null){
//					
//					if(value instanceof byte[]){
//						map.put(key.getBytes(),(byte[])value);
//					}else if(value instanceof  Date){
//						map.put(key.getBytes(),dateFormat.format((Date)value).getBytes());
//					}else{
//						map.put(key.getBytes(),value.toString().getBytes());
//					}
//				}
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		
//		
//		return map;
//	}
//	
//	
//	
//	/**
//	 * 根据map生产obj
//	 * @param serializable
//	 * @return
//	 * @throws Exception 
//	 * @throws  
//	 */
//	public <T>  T map2Obj(Map<byte[], byte[]> map,Class<T> class1) throws  Exception{
//		
//		T t=class1.newInstance();
//		if(map==null||map.isEmpty())
//			return null;
//		 map.entrySet().forEach((Entry<byte[], byte[]> entry)->{
//	
//			 try {
//				 String strkey=new String(entry.getKey()); 
//				 Method smethod= getSmethodFromField(class1, strkey);
//				 String strval=new String(entry.getValue());
//				 Object setval=null;
//				 
//				 Class<?>[] paramtypes=smethod.getParameterTypes();
//				 Class<?> paramtype=null;
//				 if(paramtypes!=null&&paramtypes.length>0){
//					 paramtype=paramtypes[0];
//				 }
//				 
//				if(paramtype==String.class)
//				{
//					setval=strval;
//				}else if(Number.class.isAssignableFrom(paramtype)){
//					setval=paramtype.getConstructor(String.class).newInstance(strval);
//				}
//				else if(Date.class.isAssignableFrom(paramtype)){
//					setval=dateFormat.parse(strval);
//				}
//				else if(paramtype==byte[].class){
//					setval=strval.getBytes();
//				}
//				
//				if(setval!=null)
//					smethod.invoke(t, setval);
//				 
//			} catch (Exception e) {
//				e.printStackTrace();
//				Iterator iterator=map.keySet().iterator();
//				while(iterator.hasNext()){
//					byte[] key=(byte[])iterator.next();
//					System.out.println(new String(key)+","+map.get(key));
//				}
//			}
//			 
//		 });
//		
//		return t;
//	}
//	/**
//	 * 根据 字段名获取getter
//	 * @param class1
//	 * @param fieldName
//	 * @return
//	 */
//	private Method getGmethodFromField(Class class1, String fieldName) {
//
//		Map<String, Method> mapMethod = entyGmethod.get(class1);
//		if (mapMethod == null) {
//			mapMethod = new HashMap<String, Method>();
//			entyGmethod.put(class1, mapMethod);
//		}
//
//		if (!mapMethod.containsKey(fieldName)) {
//
//			StringBuilder builder = new StringBuilder();
//
//			
//			try {
//
//				Field field = class1.getDeclaredField(fieldName);
//				if(field.getType()==boolean.class||field.getType()==Boolean.class)
//					builder.append("is");
//				else builder.append("get");
//				
//				builder.append(Character.toUpperCase(fieldName.charAt(0)));
//
//				if (fieldName.length() > 1)
//					builder.append(fieldName.substring(1));
//				mapMethod.put(fieldName,
//						class1.getMethod(builder.toString()));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		return mapMethod.get(fieldName);
//
//	}
//	
//	
//	/**
//	 * 根据 字段名获取setter
//	 * @param class1
//	 * @param fieldName
//	 * @return
//	 */
//	private Method getSmethodFromField(Class class1, String fieldName) {
//
//		Map<String, Method> mapMethod = entySmethod.get(class1);
//		if (mapMethod == null) {
//			mapMethod = new HashMap<String, Method>();
//			entySmethod.put(class1, mapMethod);
//		}
//
//		if (!mapMethod.containsKey(fieldName)) {
//
//			StringBuilder builder = new StringBuilder("set");
//
//			builder.append(Character.toUpperCase(fieldName.charAt(0)));
//
//			if (fieldName.length() > 1)
//				builder.append(fieldName.substring(1));
//			try {
//
//				Field field = class1.getDeclaredField(fieldName);
//				mapMethod.put(fieldName,
//						class1.getMethod(builder.toString(),field.getType()));
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//
//		return mapMethod.get(fieldName);
//
//	}
//	
//	public Jedis  getredis(){
//		
//		Jedis jedis=jedisPool.getResource();
//		jedis.auth(auth);
//		return  jedis;
//	}
//
//	public String getAuth() {
//		return auth;
//	}
//
//	public void setAuth(String auth) {
//		this.auth = auth;
//	}
//	
//	
//
//}
