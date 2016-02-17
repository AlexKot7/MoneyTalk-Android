/*
 * This is the source code of Telegram for Android v. 3.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

import ru.tinkoff.mt.processor.InjectFromConfig;

@InjectFromConfig
public class BuildVars {
    public static boolean DEBUG_VERSION = false;
    public static int BUILD_VERSION = 542;
    public static int APP_ID = 0; //obtain your own APP_ID at https://core.telegram.org/api/obtaining_api_id
    public static String APP_HASH = ""; //obtain your own APP_HASH at https://core.telegram.org/api/obtaining_api_id
    public static String HOCKEY_APP_HASH = "";
    public static String HOCKEY_APP_HASH_DEBUG = "";
    public static String GCM_SENDER_ID = "";
    public static String SEND_LOGS_EMAIL = "";
    public static String BING_SEARCH_KEY = ""; //obtain your own KEY at https://www.bing.com/dev/en-us/dev-center
    public static String FOURSQUARE_API_KEY = ""; //obtain your own KEY at https://developer.foursquare.com/
    public static String FOURSQUARE_API_ID = ""; //obtain your own API_ID at https://developer.foursquare.com/
    public static String FOURSQUARE_API_VERSION = "";
}

