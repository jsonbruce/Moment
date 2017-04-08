package com.bukeu.moment.model;

public class Feedback extends BaseModel {
	
	private Long id;
	
	private String uuid;
	
	private String content;
	
	private String phoneModel;
	
	private String buildVersion;
	
	private Integer appversionCode;
	
	private String appversionName;
	
	private String telephone;
	
	private String extra;
	
	private String createDate;
	
	public Feedback(){
		
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getPhoneModel() {
		return phoneModel;
	}

	public void setPhoneModel(String phoneModel) {
		this.phoneModel = phoneModel;
	}

	public String getBuildVersion() {
		return buildVersion;
	}

	public void setBuildVersion(String buildVersion) {
		this.buildVersion = buildVersion;
	}

	public Integer getAppversionCode() {
		return appversionCode;
	}

	public void setAppversionCode(Integer appversionCode) {
		this.appversionCode = appversionCode;
	}

	public String getAppversionName() {
		return appversionName;
	}

	public void setAppversionName(String appversionName) {
		this.appversionName = appversionName;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getExtra() {
		return extra;
	}

	public void setExtra(String extra) {
		this.extra = extra;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}
	
	
}
