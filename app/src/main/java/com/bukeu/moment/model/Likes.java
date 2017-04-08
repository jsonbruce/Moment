package com.bukeu.moment.model;

public class Likes extends BaseModel {
	
	/** likes id*/
	private Long id;
	
	/** likes user uuid */
	private String uuid;
	
	/** likes moment id */
	private Long momentId;

	public Likes() {
		// TODO Auto-generated constructor stub
	}
	
	public Likes(String uuid, Long momentId){
		this.uuid = uuid;
		this.momentId = momentId;
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

	public Long getMomentId() {
		return momentId;
	}

	public void setMomentId(Long momentId) {
		this.momentId = momentId;
	}
	
	
}
