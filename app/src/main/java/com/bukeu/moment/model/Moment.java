package com.bukeu.moment.model;

public class Moment extends BaseModel {

	/** moment id */
	private Long id;

	/** 发布用户 uuid */
	private String uuid;

	/** 发布用户 avater url/path */
	private String avater;

	/** 发布用户 nickname */
	private String nickname;

	/** 图像url/path */
	private String image;

	/** 文字 */
	private String text;

	/** 创建时间 */
	private String createDate;

	/** 发布地点经度 */
	private Double locationLongitude;

	/** 发布地点纬度 */
	private Double locationLatitude;

	/** 发布地点名称 */
	private String locationName;

	/** 赞数统计 */
	private Long likesCount;

	public Moment() {
	}
	
	public Moment(Long likesCount){
		this.likesCount = likesCount;
	}

	public Moment(String text) {
		this.text = text;
	}

	public Moment(String nickname, String location, Long likesCount, String text) {
		this.nickname = nickname;
		this.locationName = location;
		this.likesCount = likesCount;
		this.text = text;
	}

	/************* getter & setter method **/

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

	public String getImage() {
		return image;
	}

	public void setImage(String image) {
		this.image = image;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getCreateDate() {
		return createDate;
	}

	public void setCreateDate(String createDate) {
		this.createDate = createDate;
	}

	public Double getLocationLongitude() {
		return locationLongitude;
	}

	public void setLocationLongitude(Double locationLongitude) {
		this.locationLongitude = locationLongitude;
	}

	public Double getLocationLatitude() {
		return locationLatitude;
	}

	public void setLocationLatitude(Double locationLatitude) {
		this.locationLatitude = locationLatitude;
	}

	public String getLocationName() {
		return locationName;
	}

	public void setLocationName(String locationName) {
		this.locationName = locationName;
	}

	public Long getLikesCount() {
		return likesCount;
	}

	public void setLikesCount(Long likesCount) {
		this.likesCount = likesCount;
	}
	
	public Long addLikesCount(){
		return ++(this.likesCount);
	}
	
	public Long desLikesCount() {
		return --(this.likesCount);
	}

	public String getAvater() {
		return avater;
	}

	public void setAvater(String avater) {
		this.avater = avater;
	}

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
}
