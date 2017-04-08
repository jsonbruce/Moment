package com.bukeu.moment.util;

public class NetUtil {

	/* URLs */
	public static final String URL_HOST = "http://www.bukeu.com";
	public static final String URL_API_BASE = "http://api.moment.bukeu.com/v1";    // http://192.168.1.214:8080
	public static final String URL_USER_SIGNIN = URL_API_BASE + "/users/signin";
	public static final String URL_USER_SIGNUP = URL_API_BASE + "/users";
	public static final String URL_USER_INFO = URL_API_BASE + "/users/%s";
	public static final String URL_USER_UPDATE = URL_API_BASE + "/users/%s";
	public static final String URL_USER_UPDATE_AVATER = URL_API_BASE + "/users/%s/avater";
	public static final String URL_USER_UPDATE_PASSWORD = URL_API_BASE + "/users/%s/password";
	public static final String URL_SEARCH = URL_API_BASE + "/search";
	public static final String URL_MOMENTS = URL_API_BASE + "/moments";
	public static final String URL_USER_MOMENTS = URL_API_BASE + "/users/%s/moments";
	public static final String URL_USER_MOMENT = URL_API_BASE + "/users/%s/moments/%d";
	public static final String URL_USER_TODAY_MOMENT = URL_API_BASE + "/users/%s/today/moments";
	public static final String URL_FOLLOWING_MOMENTS = URL_API_BASE + "/users/%s/following/moments";
	public static final String URL_LIKES = URL_API_BASE + "/likes";
	public static final String URL_LIKES_ALL = URL_API_BASE + "/likes";
	public static final String URL_LIKES_TODAY = URL_API_BASE + "/likes/%s/today";
	public static final String URL_MOMENT_LIKED_USERS = URL_API_BASE + "/likes/%d/users";
	public static final String URL_FOLLOWS = URL_API_BASE + "/follows";
	public static final String URL_FOLLOWERS = URL_API_BASE + "/followers/%s";
	public static final String URL_FOLLOWING = URL_API_BASE + "/following/%s";
	public static final String URL_APPVERSION = URL_API_BASE + "/appversion";
	public static final String URL_FEEDBACK = URL_API_BASE + "/feedback";

	public static final String CONTENT_TYPE_JSON = "application/json; charset=utf-8";

	public static final String HEADER_APP_AUTHORIZATION = "X-Bukeu-Authorization";
	public static final String HEADER_APP_KEY = "X-Bukeu-Key";
	public static final String HEADER_BUKEU_USER = "X-Bukeu-User";
	public static final String HEADER_PAGE_PAGE = "X-Page-Page";
	public static final String HEADER_PAGE_SIZE = "X-Page-Size";
	public static final String HEADER_TOTAL_COUNT = "X-Total-Count";
	public static final String HEADER_RATE_LIMIT_LIMIT = "X-Rate-Limit-Limit";
	public static final String HEADER_RATE_LIMIT_REMAINING = "X-Rate-Limit-Remaining";
	public static final String HEADER_RATE_LIMIT_RESET = "X-Rate-Limit-Reset";

//*********   request params key and JSON output *********/
	
	public static final String PARAM_APP_ID = "app_id";
	public static final String PARAM_APP_KEY = "app_key";
	public static final String PARAM_APP_SECRET = "app_secret";
	public static final String PARAM_API_KEY = "api_key";
	
	public static final String PARAM_PAGE = "page";
	public static final String PARAM_SIZE = "size";
	/** "uid","pid"... */
	public static final String PARAM_SORT = "sort";
	/** "asc" or "desc" */
	public static final String PARAM_ORDER = "order";
	public static final String PARAM_LIMIT = "limit";
	public static final String PARAM_FIELDS = "fields";
	
	public static final String PARAM_UUID = "uuid";
	public static final String PARAM_MOMENTID = "momentId";
	public static final String PARAM_MOMENTIDS = "momentIds";
	public static final String PARAM_MOMENT = "moment";
	
	public static final String PARAM_FOLLOWER_UUID = "followerUuid";

	//******** user **************//
	
	public static final String PARAM_PASSWORD = "password";
	public static final String PARAM_NICKNAME = "nickname";
	public static final String PARAM_TELEPHONE = "telephone";
	public static final String PARAM_GENDER = "gender";
	public static final String PARAM_EMAIL = "email";
	public static final String PARAM_LAST_SIGNIN = "last_signin";
	public static final String PARAM_AVATER = "avater"; // only for JSON

	public static final String PARAM_FEEDBACK = "feedback";
	
	
	public static final String PARAM_FILE = "file";
	
	//********** error ************
	public static final String ERROR_USER_EXISTED = "user already existed";
	public static final String ERROR_USER_ISNOT_EXISTED = "user isn't existed";
}
