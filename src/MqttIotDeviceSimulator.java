import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;



public class MqttIotDeviceSimulator {
	

	
	public static void main(String[] args){
		System.out.println("This is a Mqtt Iot Device Simulator");
		
		MqttService mService = new MqttService();
		
		mService.connectToService();
		
		BufferedReader mCmdReader = new BufferedReader(new InputStreamReader(System.in));
		String cmdLine;
		
		while(true){
			try {
				cmdLine = mCmdReader.readLine();
				if(cmdLine.equals("exit"))
					break;
				
				if(cmdLine.contains("send_alarm")){
					String[] cmdArgs = cmdLine.split(" "); //args: 1.deviceId 2.chnum 
					if(cmdArgs.length == 3){
						String deviceId = cmdArgs[1];
						int chnum = Integer.parseInt(cmdArgs[2]);
						
						mService.sendAlarm(deviceId, chnum);
					}
				}
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		mService.disconnectService();
	}
}
