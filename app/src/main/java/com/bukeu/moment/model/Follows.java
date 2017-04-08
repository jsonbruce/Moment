package com.bukeu.moment.model;

public class Follows extends BaseModel {
	
	/** 索引id */
	private Long id;
	
	private String uuid;
	
	private String followerUuid;
	
	public Follows(){
	}
	
	public Follows(String uuid, String followerUuid){
		this.uuid = uuid;
		this.followerUuid = followerUuid;
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

	public String getFollowerUuid() {
		return followerUuid;
	}

	public void setFollowerUuid(String followerUuid) {
		this.followerUuid = followerUuid;
	}
	
	
}
