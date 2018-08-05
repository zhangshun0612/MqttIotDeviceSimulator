package bean;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.Date;

public class MonitorPointChannel implements Serializable{
    public int getChannelNumber() {
        return channelNumber;
    }

    public void setChannelNumber(int channelNumber) {
        this.channelNumber = channelNumber;
    }

    public int getChannelDefine() {
        return channelDefine;
    }

    public void setChannelDefine(int channelDefine) {
        this.channelDefine = channelDefine;
    }

    public int getChannelStatus() {
        return channelStatus;
    }

    public void setChannelStatus(int channelStatus) {
        this.channelStatus = channelStatus;
    }

    public int getChannelAlarmType() {
        return channelAlarmType;
    }

    public void setChannelAlarmType(int channelAlarmType) {
        this.channelAlarmType = channelAlarmType;
    }

    public int getChannelFaultType() {
        return channelFaultType;
    }

    public void setChannelFaultType(int channelFaultType) {
        this.channelFaultType = channelFaultType;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public Date getLatestUpdateTime() {
        return latestUpdateTime;
    }

    public void setLatestUpdateTime(Date latestUpdateTime) {
        this.latestUpdateTime = latestUpdateTime;
    }

    @SerializedName("chn")
    private int channelNumber;

    @SerializedName("chd")
    private int channelDefine;

    @SerializedName("chs")
    private int channelStatus;

    @SerializedName("at")
    private int channelAlarmType;

    @SerializedName("ft")
    private int channelFaultType;

    @SerializedName("v")
    private double value;

    @SerializedName("t")
    private Date latestUpdateTime;

    public MonitorPointChannel(int chnum){
        channelNumber = chnum;
        channelDefine = Constant.FUNCTION_UNDEFINED;
    }

    public MonitorPointChannel(int chnum, int chDef){
        channelNumber = chnum;
        channelDefine = chDef;
    }

}
