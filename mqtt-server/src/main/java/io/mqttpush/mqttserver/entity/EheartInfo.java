package io.mqttpush.mqttserver.entity;

import java.math.BigDecimal;

public class EheartInfo {
    private Long sid;

    private String phnumber;

    private String deviceid;

    private String networkoperatorname;

    private String simserialnumber;

    private String subscriberid;

    private String networkcountryiso;

    private String networkoperator;

    private String product;

    private String model;

    private String brand;

    private String manufacturer;

    private BigDecimal battery;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String address;

    private BigDecimal totalcachesize;

    private BigDecimal cpuusage;

    private BigDecimal totalmemory;

    private BigDecimal availmemory;

    private BigDecimal phonesize;

    private BigDecimal phoneavailsize;

    private BigDecimal sdtotalsize;

    private BigDecimal sdavailsize;

    private BigDecimal trraficinfo;

    public Long getSid() {
        return sid;
    }

    public void setSid(Long sid) {
        this.sid = sid;
    }

    public String getPhnumber() {
        return phnumber;
    }

    public void setPhnumber(String phnumber) {
        this.phnumber = phnumber == null ? null : phnumber.trim();
    }

    public String getDeviceid() {
        return deviceid;
    }

    public void setDeviceid(String deviceid) {
        this.deviceid = deviceid == null ? null : deviceid.trim();
    }

    public String getNetworkoperatorname() {
        return networkoperatorname;
    }

    public void setNetworkoperatorname(String networkoperatorname) {
        this.networkoperatorname = networkoperatorname == null ? null : networkoperatorname.trim();
    }

    public String getSimserialnumber() {
        return simserialnumber;
    }

    public void setSimserialnumber(String simserialnumber) {
        this.simserialnumber = simserialnumber == null ? null : simserialnumber.trim();
    }

    public String getSubscriberid() {
        return subscriberid;
    }

    public void setSubscriberid(String subscriberid) {
        this.subscriberid = subscriberid == null ? null : subscriberid.trim();
    }

    public String getNetworkcountryiso() {
        return networkcountryiso;
    }

    public void setNetworkcountryiso(String networkcountryiso) {
        this.networkcountryiso = networkcountryiso == null ? null : networkcountryiso.trim();
    }

    public String getNetworkoperator() {
        return networkoperator;
    }

    public void setNetworkoperator(String networkoperator) {
        this.networkoperator = networkoperator == null ? null : networkoperator.trim();
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product == null ? null : product.trim();
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model == null ? null : model.trim();
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand == null ? null : brand.trim();
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer == null ? null : manufacturer.trim();
    }

    public BigDecimal getBattery() {
        return battery;
    }

    public void setBattery(BigDecimal battery) {
        this.battery = battery;
    }

    public BigDecimal getLongitude() {
        return longitude;
    }

    public void setLongitude(BigDecimal longitude) {
        this.longitude = longitude;
    }

    public BigDecimal getLatitude() {
        return latitude;
    }

    public void setLatitude(BigDecimal latitude) {
        this.latitude = latitude;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address == null ? null : address.trim();
    }

    public BigDecimal getTotalcachesize() {
        return totalcachesize;
    }

    public void setTotalcachesize(BigDecimal totalcachesize) {
        this.totalcachesize = totalcachesize;
    }

    public BigDecimal getCpuusage() {
        return cpuusage;
    }

    public void setCpuusage(BigDecimal cpuusage) {
        this.cpuusage = cpuusage;
    }

    public BigDecimal getTotalmemory() {
        return totalmemory;
    }

    public void setTotalmemory(BigDecimal totalmemory) {
        this.totalmemory = totalmemory;
    }

    public BigDecimal getAvailmemory() {
        return availmemory;
    }

    public void setAvailmemory(BigDecimal availmemory) {
        this.availmemory = availmemory;
    }

    public BigDecimal getPhonesize() {
        return phonesize;
    }

    public void setPhonesize(BigDecimal phonesize) {
        this.phonesize = phonesize;
    }

    public BigDecimal getPhoneavailsize() {
        return phoneavailsize;
    }

    public void setPhoneavailsize(BigDecimal phoneavailsize) {
        this.phoneavailsize = phoneavailsize;
    }

    public BigDecimal getSdtotalsize() {
        return sdtotalsize;
    }

    public void setSdtotalsize(BigDecimal sdtotalsize) {
        this.sdtotalsize = sdtotalsize;
    }

    public BigDecimal getSdavailsize() {
        return sdavailsize;
    }

    public void setSdavailsize(BigDecimal sdavailsize) {
        this.sdavailsize = sdavailsize;
    }

    public BigDecimal getTrraficinfo() {
        return trraficinfo;
    }

    public void setTrraficinfo(BigDecimal trraficinfo) {
        this.trraficinfo = trraficinfo;
    }

	@Override
	public String toString() {
		return "EheartInfo [sid=" + sid + ", phnumber=" + phnumber
				+ ", deviceid=" + deviceid + ", networkoperatorname="
				+ networkoperatorname + ", simserialnumber=" + simserialnumber
				+ ", subscriberid=" + subscriberid + ", networkcountryiso="
				+ networkcountryiso + ", networkoperator=" + networkoperator
				+ ", product=" + product + ", model=" + model + ", brand="
				+ brand + ", manufacturer=" + manufacturer + ", battery="
				+ battery + ", longitude=" + longitude + ", latitude="
				+ latitude + ", address=" + address + ", totalcachesize="
				+ totalcachesize + ", cpuusage=" + cpuusage + ", totalmemory="
				+ totalmemory + ", availmemory=" + availmemory + ", phonesize="
				+ phonesize + ", phoneavailsize=" + phoneavailsize
				+ ", sdtotalsize=" + sdtotalsize + ", sdavailsize="
				+ sdavailsize + ", trraficinfo=" + trraficinfo + "]";
	}
    
}