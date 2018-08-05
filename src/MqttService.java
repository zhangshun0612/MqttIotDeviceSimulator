import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import com.google.gson.Gson;

import bean.Constant;
import bean.MonitorPoint;
import bean.MonitorPointChannel;
import bean.MonitorPointList;

public class MqttService {
	final static String brokerUrl = "ssl://iottestbyzs.mqtt.iot.gz.baidubce.com:1884";
    final static String clientId = "com_langkai_www" + UUID.randomUUID().toString();
    final static String userName = "iottestbyzs/device1";
    final static String password = "O060WwWajgLjsLkeVMzscLoOstPdEdmw3rB9Amc24ts=";
    
    final String mpListTopic = "mpList";
    final String mpDataTopic = "mpData";

    final String requestMpListTopic = "request_mpList";
    final String requestMpDataTopic = "request_mpData";
    
    final String requestMpListCmd = "GET:request_mpList";
    final String requestMpDataCmd = "GET:request_mpData"; //with args
    
    private MonitorPointData mpDatas;
    
    private MqttClient mClient = null;
    private MqttCallback mCallback = new MqttCallback(){

		@Override
		public void connectionLost(Throwable arg0) {
			// TODO Auto-generated method stub
			System.out.println("Connection Lost");
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void messageArrived(String topic, MqttMessage message) throws Exception {
			System.out.println("Topic: " + topic);
			System.out.println("Message: " + message.toString());
			
			if(topic.equals(requestMpListTopic)){
				sendMpList();
			}else if(topic.equals(requestMpDataTopic)){
				String messageStr = message.toString();
				System.out.println(messageStr);
				
				String[] cmdBuff = messageStr.split(":");
				if(cmdBuff.length != 2){
					return;
				}
				
				String cmdType = cmdBuff[0];
				String cmdLine = cmdBuff[1];
				
				System.out.println(cmdType);
				System.out.println(cmdLine);
				
				if(cmdType.equals("GET")){
					String[] argsBuff = cmdLine.split(" ");
					System.out.println(argsBuff.length);
					System.out.println(argsBuff[0]);
					System.out.println(argsBuff[1]);
					
					if(argsBuff.length == 2 && argsBuff[0].equals("request_mpData")){
						String deviceId = argsBuff[1];
						
						String jsonStr = mpDatas.getMonitorPointJson(deviceId);
						System.out.println(jsonStr);
						publish(mpDataTopic, jsonStr);
					}
				}
			}
		}
    	
    };
    
    public MqttService(){
    	mpDatas = new MonitorPointData();
    }
    
    public void connectToService(){
    	try {
			mClient = new MqttClient(brokerUrl, clientId);
			
			MqttConnectOptions connOpt = new MqttConnectOptions();
			connOpt.setUserName(userName);
            connOpt.setPassword(password.toCharArray());
            connOpt.setCleanSession(true);
            connOpt.setConnectionTimeout(10);
            connOpt.setKeepAliveInterval(20);
            
            mClient.setCallback(mCallback);
            mClient.connect(connOpt);
			
            mClient.subscribe(requestMpListTopic);
            mClient.subscribe(requestMpDataTopic);
            
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void disconnectService(){
    	if(mClient != null && mClient.isConnected()){
    		try {
				mClient.disconnect();
			} catch (MqttException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    }
    
    private void publish(String topic, String messageStr){
    	if(mClient == null && !mClient.isConnected())
    		return;
    	
    	MqttMessage message = new MqttMessage();
    	message.setQos(1);
    	message.setPayload(messageStr.getBytes());
    	
    	try {
			mClient.publish(topic, message);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void sendAlarm(String id, int chnum){
    	MonitorPoint mp = mpDatas.getMonitorPoint(id);
    	if(mp == null)
    		return;
    	
    	MonitorPointChannel ch = mp.getMonitorPointChannel(chnum);
    	if(ch == null)
    		return;
    	
    	ch.setChannelStatus(Constant.STATUS_ALARM);
    	ch.setChannelAlarmType(Constant.ALARM_UPPER_LIMIT);
    	
    	mp.setMonitorPointChannel(chnum, ch);
    	mpDatas.setMonitorPoint(id, mp);
    	
    	String jsonStr = mpDatas.getMonitorPointJson(id);
    	
    	publish(mpDataTopic, jsonStr);
    }
    
    private void sendMpList(){
    	if(mClient == null && !mClient.isConnected())
    		return;
    	
    	MqttMessage message = new MqttMessage();
    	message.setQos(1);
    	message.setPayload(mpDatas.getMonitorPointListJson().getBytes());
    	
    	try {
			mClient.publish(mpListTopic, message);
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private class MonitorPointData{
    	private MonitorPointList monitorList;
    	private Map<String, MonitorPoint> mMap;
    	
    	private Gson gson = new Gson();
    	
    	public MonitorPointData(){
    		monitorList = new MonitorPointList();
    		mMap = new HashMap<>();
    		
    		for(int i = 1; i < 18 ; i++){
    			String id = String.format("%05d", i);
    		
    			MonitorPoint mp = new MonitorPoint(id, "未定义");
    			MonitorPointChannel ch1 = new MonitorPointChannel(1, Constant.FUNCTION_RESIDUAL_CURRENT);
                ch1.setChannelStatus(Constant.STATUS_OK);
                ch1.setValue(30.22);
                mp.addMonitorPointChannel(1, ch1);

                MonitorPointChannel ch2 = new MonitorPointChannel(1, Constant.FUNCTION_TEMPERATURE);
                ch2.setChannelStatus(Constant.STATUS_OK);
                ch2.setValue(38.22);
                mp.addMonitorPointChannel(2, ch2);

                MonitorPointChannel ch3 = new MonitorPointChannel(1, Constant.FUNCTION_RUNNING_CURRENT);
                ch3.setChannelStatus(Constant.STATUS_OK);
                ch3.setValue(20);
                mp.addMonitorPointChannel(3, ch3);


                MonitorPointChannel ch4 = new MonitorPointChannel(1, Constant.FUNCTION_RUNNING_VOLT);
                ch4.setChannelStatus(Constant.STATUS_OK);
                ch4.setValue(220);
                mp.addMonitorPointChannel(4, ch4);
                
                mMap.put(id, mp);
                
                MonitorPointList.MonitorPointInfo info = new MonitorPointList.MonitorPointInfo(id, "未定义", Constant.STATUS_OK);
    			monitorList.addDeviceInfo(info);
    		}
    		
    		
    		for(int i = 18; i < 20 ; i++){
    			String id = String.format("%05d", i);
    		
    			MonitorPoint mp = new MonitorPoint(id, "未定义");
    			MonitorPointChannel ch1 = new MonitorPointChannel(1, Constant.FUNCTION_RESIDUAL_CURRENT);
                ch1.setChannelStatus(Constant.STATUS_OK);
                ch1.setValue(30.22);
                mp.addMonitorPointChannel(1, ch1);

                MonitorPointChannel ch2 = new MonitorPointChannel(1, Constant.FUNCTION_TEMPERATURE);
                ch2.setChannelStatus(Constant.STATUS_ALARM);
                ch2.setValue(100.00);
                mp.addMonitorPointChannel(2, ch2);

                MonitorPointChannel ch3 = new MonitorPointChannel(1, Constant.FUNCTION_RUNNING_CURRENT);
                ch3.setChannelStatus(Constant.STATUS_OK);
                ch3.setValue(20);
                mp.addMonitorPointChannel(3, ch3);


                MonitorPointChannel ch4 = new MonitorPointChannel(1, Constant.FUNCTION_RUNNING_VOLT);
                ch4.setChannelStatus(Constant.STATUS_OK);
                ch4.setValue(220);
                mp.addMonitorPointChannel(4, ch4);
                
                mMap.put(id, mp);
                
                MonitorPointList.MonitorPointInfo info = new MonitorPointList.MonitorPointInfo(id, "未定义", Constant.STATUS_ALARM);
    			monitorList.addDeviceInfo(info);
    		}
    		
    	}
    	
    	public MonitorPoint getMonitorPoint(String id){
    		MonitorPoint mp = null;
    		if(mMap.containsKey(id)){
    			mp = mMap.get(id);
    		}
    		
    		return mp;
    	}
    	
    	public void setMonitorPoint(String id, MonitorPoint mp){
    		mMap.put(id, mp);
    	}
    	
    	public int getMonitorPointListSize(){
    		return mMap.size();
    	}
    	
    	public String getMonitorPointListJson(){
    		String jsonStr = "";
    		jsonStr = gson.toJson(monitorList);  		
    		return jsonStr;
    	}
    	
    	public String getMonitorPointJson(String deviceId){
    		String jsonStr = "";
    		if(mMap.containsKey(deviceId)){
    			MonitorPoint mp = mMap.get(deviceId);
    			jsonStr = gson.toJson(mp);
    		}  		
    		return jsonStr;
    	}
    }
}
