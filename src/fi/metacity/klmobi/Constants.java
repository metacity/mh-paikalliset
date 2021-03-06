package fi.metacity.klmobi;

public final class Constants {
	public static final String EXTRA_DATE = "fi.metacity.klmobi.extra.DATE";
	public static final String EXTRA_TIME = "fi.metacity.klmobi.extra.TIME";
	public static final String EXTRA_NUMBER_OF_ROUTES = "fi.metacity.klmobi.extra.NUMBER_OF_ROUTES";
	public static final String EXTRA_ROUTING_TYPE = "fi.metacity.klmobi.extra.ROUTING_TYPE";
	public static final String EXTRA_WALKING_SPEED = "fi.metacity.klmobi.extra.WALKING_SPEED";
	public static final String EXTRA_MAX_WALKING_DISTANCE = "fi.metacity.klmobi.extra.MAX_WALKING_DISTANCE";
	public static final String EXTRA_CHANGE_MARGIN = "fi.metacity.klmobi.extra.CHANGE_MARGIN";
	public static final String EXTRA_TIME_DIRECTION = "fi.metacity.klmobi.extra.TIME_DIRECTION";
	
	public static final String EXTRA_ROUTE_INDEX = "fi.metacity.klmobi.extra.ROUTE_INDEX";
	
	public static final String EXTRA_SAVABLE_FAVOURITE = "fi.metacity.klmobi.extra.SAVABLE_FAVOURITE";
	
	public static final String[] CITY_SUBDOMAINS = {
		"hameenlinna",
		"lappeenranta",
		"iisalmi",
		"joensuu",
		"jyvaskyla",
		"kajaani",
		"kotka",
		"kouvola",
		"kuopio",
		"lahti",
		"mikkeli",
		"pori",
		"porvoo",
		"raahe",
		"rauma",
		"riihimaki",
		"rovaniemi",
		"salo",
		"savonlinna",
		"seinajoki",
		"tampere",
		"TURKU_DUMMY_DOMAIN",
		"uusimaa",
		"varkaus"
	};
	
	public static final String TURKU_BASE_URL = "http://reittiopas.turku.fi/";
	public static final int TURKU_INDEX = 21;
	
	public static final String[] ROUTING_TYPES = {
		"default",
		"fastest",
		"minchanges",
		"minwalk"
	};
	
	public static final String ADDRESS_SEARCH_THREAD_POOL_ID = "addressSearchThreads";
}
