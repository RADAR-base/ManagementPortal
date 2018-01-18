package org.radarcns.management.web.rest.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpUtil {

    private static Logger log = LoggerFactory.getLogger(HttpUtil.class);

    /**
     * Checks whether given {@link URL} can be reachable.
     *
     * @return {@code true} if reachable, {@code false} otherwise
     */
    public static boolean isReachable(URL urlServer) {
        try {
            HttpURLConnection urlConn = (HttpURLConnection) urlServer.openConnection();
            urlConn.setConnectTimeout(30000); //<- 30Seconds Timeout
            urlConn.connect();
            return urlConn.getResponseCode() == 200;
        } catch (IOException e) {
            log.warn("Server {} is unreachable: {}", urlServer.toString(), e.getMessage());
            return false;
        }
    }


}
