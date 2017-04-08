package com.bukeu.moment.model;

import java.util.Set;

/**
 * @author Max Xu
 *
 */
public class User extends BaseModel {

	private Set<Moment> moments;

	/** 索引id */
	private Long id;

	/** 全局用户id **/
	private String uuid;

	/** 微博uid */
	private String weiboUid;

	/** QQ OpenId */
	private String qqOpenid;

	/** WeChat OpenId */
	private String wechatOpenid;

	/** 用户昵称 */
	private String nickname;

	/** 用户性别 */
	private String gender;

	/** 用户头像 */
	private String avater;

	/** 用户邮箱 */
	private String email;

	/** 用户密码（md5加密） */
	private String password;

	/** 用户电话号码 */
	private String telephone;

	/** 用户描述 */
	private String description;

	/** 用户注册时间 */
	private String signupDate;

	/** 用户最后一次登陆时间 */
	private String lastSigninDate;

	/** 用户是否激活（激活才能发布） */
	private Boolean isActivated;

	/** 用户是否被锁定 */
	private Boolean isLocked;
	
	/** 被多少人关注*/
	private Long followers;
	
	//** 关注了多少人 */
	private Long following;

	private Long momentsCount;


// *****************************  methods ***********************************//

	public Set<Moment> getMoments() {
		return moments;
	}

	public void setMoments(Set<Moment> moments) {
		this.moments = moments;
	}

	public User() {
	}

	public User(Long id) {
		this.id = id;
	}

	public User(String email, String password) {
		this.email = email;
		this.password = password;
	}

	// ***************************** getter/setter
	// ***********************************//

	@Override
	public String toString() {
		return "昵称: " + this.nickname + "电话: " + this.telephone + "E-mail: "
				+ this.email + "个人简介：" + this.description;
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

	public String getNickname() {
		return nickname;
	}

	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	public String getAvater() {
		return avater;
	}

	public void setAvater(String avater) {
		this.avater = avater;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getTelephone() {
		return telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSignupDate() {
		return signupDate;
	}

	public void setSignupDate(String signupDate) {
		this.signupDate = signupDate;
	}

	public String getLastSigninDate() {
		return lastSigninDate;
	}

	public void setLastSigninDate(String lastSigninDate) {
		this.lastSigninDate = lastSigninDate;
	}

	public String getWeiboUid() {
		return weiboUid;
	}

	public void setWeiboUid(String weiboUid) {
		this.weiboUid = weiboUid;
	}

	public String getQqOpenid() {
		return qqOpenid;
	}

	public void setQqOpenid(String qqOpenid) {
		this.qqOpenid = qqOpenid;
	}

	public String getWechatOpenid() {
		return wechatOpenid;
	}

	public void setWechatOpenid(String wechatOpenid) {
		this.wechatOpenid = wechatOpenid;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public Boolean getIsActivated() {
		return isActivated;
	}

	public void setIsActivated(Boolean isActivated) {
		this.isActivated = isActivated;
	}

	public Boolean getIsLocked() {
		return isLocked;
	}

	public void setIsLocked(Boolean isLocked) {
		this.isLocked = isLocked;
	}

	public Long getFollowers() {
		return followers;
	}

	public void setFollowers(Long followers) {
		this.followers = followers;
	}

	public Long getFollowing() {
		return following;
	}

	public void setFollowing(Long following) {
		this.following = following;
	}
	
	public void addFollowers(){
		++(this.followers);
	}
	
	public void desFollowers(){
		--(this.followers);
	}
	
	public void addFollowing(){
		++(this.following);
	}
	
	public void desFollowing(){
		--(this.following);
	}

	public Boolean isActivated() {
		return isActivated;
	}

	public Boolean isLocked() {
		return isLocked;
	}

	public Long getMomentsCount() {
		return momentsCount;
	}

	public void setMomentsCount(Long momentsCount) {
		this.momentsCount = momentsCount;
	}
}
