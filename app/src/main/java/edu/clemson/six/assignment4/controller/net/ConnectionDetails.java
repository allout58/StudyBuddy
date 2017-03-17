package edu.clemson.six.assignment4.controller.net;

import edu.clemson.six.assignment4.Constants;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Holder class for details for connecting to a web API
 */

public class ConnectionDetails {
    public int getTimeout() {
        return timeout;
    }

    public enum Method {
        GET,
        POST,
    }

    private final String url;
    private final Map<String, String> arguments;
    private final Method method;
    private final int timeout;

    public ConnectionDetails(String url, Map<String, String> arguments, Method method, int timeout) {
        this.url = url;
        this.arguments = arguments;
        this.method = method;
        this.timeout = timeout;
    }

    public ConnectionDetails(String url, Map<String, String> arguments, Method method) {
        this.url = url;
        this.arguments = arguments;
        this.method = method;
        this.timeout = Constants.API_TIMEOUT;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getArguments() {
        return arguments;
    }

    /**
     * Format the arguments as a GET request string
     *
     * @return Formatted argument string
     */
    // No, this is not a mistype ;)
    public String getGetArgments() {
        List<String> l = new ArrayList<>();
        for (Map.Entry<String, String> e : arguments.entrySet()) {
            l.add(e.getKey() + "=" + e.getValue());
        }
        return StringUtils.join(l, "&");
    }

    public Method getMethod() {
        return method;
    }
}
