//package io.netty.example.redis;
//
//import io.mqttpush.mqttserver.beans.SendableMsg;
//
//import java.beans.BeanInfo;
//import java.beans.Introspector;
//import java.beans.PropertyDescriptor;
//import java.lang.reflect.Method;
//
//public class Testm2 {
//
//
//
//    public  static  void  main(String[] args) throws Exception{
//
//        SendableMsg sendableMsg=new SendableMsg("a","b","123".getBytes());
//
//
//        Class<?> class1=SendableMsg.class;
//
//        long l1=System.currentTimeMillis();
//
//        BeanInfo beaninfo=Introspector.getBeanInfo(SendableMsg.class);
//
//        PropertyDescriptor[] descriptor= beaninfo.getPropertyDescriptors();
//
//        Method method=null;
//        for(int i=0;i<100000000;i++){
////            try {
////                class1.getDeclaredMethod("setRetain",boolean.class).invoke(sendableMsg,true);
////            } catch (Exception e) {
////                e.printStackTrace();
////            }
//
//            if(method!=null){
//                method.invoke(sendableMsg,true);
//                continue;
//            }
//            for (int j = 0; j < descriptor.length; j++) {
//
//                String name=descriptor[j].getName();
//                if(name.equals("retain")){
//                    (method=descriptor[j].getWriteMethod()).invoke(sendableMsg,true);
//                    break;
//                }
//            }
//
//
//        }
//
//        long l2=System.currentTimeMillis();
//
//        System.out.println("run  in  time is"+(l2-l1));
//    }
//}
