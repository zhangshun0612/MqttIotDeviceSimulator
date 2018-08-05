package bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MonitorPoint implements Serializable {
    @SerializedName("id")
    private String deviceId;
    @SerializedName("nm")
    private String monitorPointName;

    @SerializedName("chs")
    private Map<Integer, MonitorPointChannel> channels = null;

    @SerializedName("st")
    private int status;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }


    public String getMonitorPointName() {
        return monitorPointName;
    }

    public void setMonitorPointName(String monitorPointName) {
        this.monitorPointName = monitorPointName;
    }

    public MonitorPoint(){
        this.monitorPointName = "未定义";
    }

    public MonitorPoint(String deviceId, String name){

        this.deviceId = deviceId;
        this.monitorPointName = name;
    }

    public void addMonitorPointChannel(int chnum, MonitorPointChannel ch){
        if(channels == null){
            channels = new HashMap<>(); //因为稀疏矩阵在gson序列化过程中，产生了很多关联信息，json字符串bytes数增多，不适用于mqtt物联网这种少字节多请求通信，仍然适用HashMap
        }

        channels.put(chnum, ch);
    }

    public MonitorPointChannel getMonitorPointChannel(int chum){
        if(channels.containsKey(chum)){
            return channels.get(chum);
        }else{
            return null;
        }
    }
    
    public void setMonitorPointChannel(int chnum, MonitorPointChannel ch){
    	channels.put(chnum, ch);
    }

    public int getMonitorPointStatus(){
        if(channels == null){
            return Constant.STATUS_OK;
        }

        Collection<MonitorPointChannel> chList = channels.values();

        int status = Constant.STATUS_OK;
        Iterator<MonitorPointChannel> iter = chList.iterator();
        while(iter.hasNext()){
            MonitorPointChannel ch = iter.next();
            if(ch.getChannelStatus() == Constant.STATUS_ALARM){
                status = Constant.STATUS_ALARM;
                break;
            }else if(ch.getChannelStatus() == Constant.STATUS_FAULT){
                status = Constant.STATUS_FAULT;
            }else if(ch.getChannelStatus() == Constant.STATUS_DISCONNECTED){
                status = Constant.STATUS_DISCONNECTED;
            }
        }

        return status;
    }

}
