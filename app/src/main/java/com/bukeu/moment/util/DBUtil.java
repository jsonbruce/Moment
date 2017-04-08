package com.bukeu.moment.util;



public class DBUtil {
	
	public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

//*****         Table            *****/
	public static final String TABLE_USER = "user";
	public static final String TABLE_MOMENT = "moment";
	public static final String TABLE_LIKES = "likes";
	public static final String TABLE_FOLLOWS = "follows";
	public static final String TABLE_PRODUCT = "scumarket_product";
	public static final String TABLE_PRODUCT_TYPE = "scumarket_type";
	public static final String TABLE_PRODUCT_IMAGE = "scumarket_image";
	public static final String TABLE_CAMPUS = "scumarket_campus";
	public static final String TABLE_COLLEGE = "scumarket_college";
	public static final String TABLE_DORM = "scumarket_dorm";
	public static final String TABLE_TEXT_TO_PINYIN = "text_to_pinyin";

//*********      Column 'scumarket_user'             *********/	
	public static final String COLUMN_USER_ID = "id";
	public static final String COLUMN_USER_UUID = "uuid";
	public static final String COLUMN_USER_WEIBO_UID = "weibo_uid";
	public static final String COLUMN_USER_QQ_OPENID = "qq_openid";
	public static final String COLUMN_USER_WECHAT_OPENID = "wechat_openid";
	public static final String COLUMN_USER_NICKNAME = "nickname";
	public static final String COLUMN_USER_GENDER = "gender";
	public static final String COLUMN_USER_AVATER = "avater";
	public static final String COLUMN_USER_EMAILADDRESS = "email";
	public static final String COLUMN_USER_PASSWORD = "password";
	public static final String COLUMN_USER_TELEPHONE = "telephone";
	public static final String COLUMN_USER_DESCRIPTION = "description";
	public static final String COLUMN_USER_SIGNUP_DATE = "signup_date";
	public static final String COLUMN_USER_LAST_SIGNIN_DATE = "last_signin_date";
	public static final String COLUMN_USER_ISACTIVATED = "is_activated";
	public static final String COLUMN_USER_ISLOCKED = "is_locked";
	public static final String COLUMN_USER_FOLLOWER = "followers";
	public static final String COLUMN_USER_FOLLOWING = "following";
	
//*********      Column 'moment_moment'             *********/
	public static final String COLUMN_MOMENT_ID = "id";
	public static final String COLUMN_MOMENT_UUID = "uuid";
	public static final String COLUMN_MOMENT_IMAGE = "image";
	public static final String COLUMN_MOMENT_TEXT = "text";
	public static final String COLUMN_MOMENT_CREATE_DATA = "create_date";
	public static final String COLUMN_MOMENT_LOCATION_LONGITUDE = "location_longitude";
	public static final String COLUMN_MOMENT_LOCATION_LATITUDE = "location_latitude";
	public static final String COLUMN_MOMENT_LOCATION_NAME = "location_name";
	public static final String COLUMN_MOMENT_LIKES_COUNT = "likes_count";
	
	//************** Column 'moment_likes' *****************88/
	public static final String COLUMN_LIKES_ID = "id";
	public static final String COLUMN_LIKES_MOMENT_ID = "moment_id";
	
	//************ Column 'moment_follows' ***************/
	public static final String COLUMN_FOLLOWS_ID = "id";
	public static final String COLUMN_FOLLOWS_UUID = "uuid";
	public static final String COLUMN_FOLLOWS_FOLLOWER_UUID = "follower_uuid";
	
}
