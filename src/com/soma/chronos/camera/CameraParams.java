package com.soma.chronos.camera;

public interface CameraParams {

	public static final String KEY_ZOOM = "KEY_zoom";
	public static final String KEY_MIN_ZOOM = "KEY_minZoom";
	public static final String KEY_MAX_ZOOM = "KEY_maxZoom";

	public static final String KEY_EXPOSURE = "KEY_exposure";
	public static final String KEY_MIN_EXPOSURE = "KEY_minExposure";
	public static final String KEY_MAX_EXPOSURE = "KEY_maxExposure";

	public static final String KEY_SCENE = "KEY_scene";

	public static final String KEY_WHITE_BALANCE = "KEY_whiteBalance";

	public static final String KEY_EFFECT = "KEY_effect";

	public static final String KEY_ISO = "KEY_iso";
	public static final String AUTO = "auto";

	public static final String ZERO = "0";
	public static final String KEY = "KEY_";

	// http://developer.android.com/sdk/api_diff/5/changes/android.hardware.Camera.Parameters.html
	// 참고하세요!

	// EFFECT
	public static final String EFFECT_AQUA = "aqua";
	public static final String EFFECT_BLACKBOARD = "blackboard";
	public static final String EFFECT_MONO = "mono";
	public static final String EFFECT_NEGATIVE = "negative";
	public static final String EFFECT_NONE = "none";
	public static final String EFFECT_POSTERIZE = "posterize";
	public static final String EFFECT_SEPIA = "sepia";
	public static final String EFFECT_SOLARIZE = "solarize";
	public static final String EFFECT_WHITEBOARD = "whiteboard";

	// FLASH_MODE
	public static final String FLASH_MODE_AUTO = "auto";
	public static final String FLASH_MODE_OFF = "off";
	public static final String FLASH_MODE_ON = "on";
	public static final String FLASH_MODE_RED_EYE = "red-eye";
	public static final String FLASH_MODE_TORCH = "torch";

	// FOCUS_MODE
	public static final String FOCUS_MODE_AUTO = "auto";
	public static final String FOCUS_MODE_FIXED = "fixed";
	public static final String FOCUS_MODE_INFINITY = "infinity";
	public static final String FOCUS_MODE_MACRO = "macro";

	// SCENE_MODE
	public static final String SCENE_MODE_ACTION = "action";
	public static final String SCENE_MODE_AUTO = "auto";
	public static final String SCENE_MODE_BEACH = "beach";
	public static final String SCENE_MODE_CANDLELIGHT = "candlelight";
	public static final String SCENE_MODE_FIREWORKS = "fireworks";
	public static final String SCENE_MODE_LANDSCAPE = "landscape";
	public static final String SCENE_MODE_NIGHT = "night";
	public static final String SCENE_MODE_NIGHT_PORTRAIT = "night-portrait";
	public static final String SCENE_MODE_PARTY = "party";
	public static final String SCENE_MODE_PORTRAIT = "portrait";
	public static final String SCENE_MODE_SNOW = "snow";
	public static final String SCENE_MODE_SPORTS = "sports";
	// public static final String SCENE_MODE_STEADYPHOTO = "steadyphoto";
	// public static final String SCENE_MODE_SUNSET = "sunset";
	// public static final String SCENE_MODE_THEATRE = "theatre";

	public static final String[] SCENE_ARRAY = { SCENE_MODE_ACTION,
			SCENE_MODE_AUTO, SCENE_MODE_BEACH, SCENE_MODE_CANDLELIGHT,
			SCENE_MODE_FIREWORKS, SCENE_MODE_LANDSCAPE, SCENE_MODE_NIGHT,
			SCENE_MODE_NIGHT_PORTRAIT, SCENE_MODE_PARTY, SCENE_MODE_PORTRAIT,
			SCENE_MODE_SNOW, SCENE_MODE_SPORTS };
	// SCENE_MODE_STEADYPHOTO,
	// SCENE_MODE_SUNSET, SCENE_MODE_THEATRE };

	// WHITE_BALANCE
	public static final String WHITE_BALANCE_AUTO = "auto";
	public static final String WHITE_BALANCE_CLOUDY_DAYLIGHT = "cloudy-daylight";
	public static final String WHITE_BALANCE_DAYLIGHT = "daylight";
	public static final String WHITE_BALANCE_FLUORESCENT = "fluorescent";
	public static final String WHITE_BALANCE_INCANDESCENT = "incandescent";
	// public static final String WHITE_BALANCE_SHADE = "shade";
	// public static final String WHITE_BALANCE_TWILIGHT = "twilight";
	// public static final String WHITE_BALANCE_WARM_FLUORESCENT =
	// "warm-fluorescent";

	public static final String[] WHITE_BALANCE_ARRAY = { WHITE_BALANCE_AUTO,
			WHITE_BALANCE_CLOUDY_DAYLIGHT, WHITE_BALANCE_DAYLIGHT,
			WHITE_BALANCE_FLUORESCENT, WHITE_BALANCE_INCANDESCENT };
	// WHITE_BALANCE_SHADE, WHITE_BALANCE_TWILIGHT,
	// WHITE_BALANCE_WARM_FLUORESCENT };

}
