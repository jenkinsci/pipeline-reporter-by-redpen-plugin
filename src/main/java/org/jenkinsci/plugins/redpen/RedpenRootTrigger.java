package org.jenkinsci.plugins.redpen;

import hudson.Extension;
import hudson.model.UnprotectedRootAction;
import hudson.security.csrf.CrumbExclusion;
import hudson.util.IOUtils;
import org.kohsuke.stapler.StaplerRequest;
import org.kohsuke.stapler.StaplerResponse;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

@Extension
public class RedpenRootTrigger implements UnprotectedRootAction {

    static final String URL = "redpenhook";

    private static final Logger LOGGER = Logger.getLogger(RedpenRootTrigger.class.getName());

    private static final int PAYLOAD_LENGTH = 8;

    private Set<StartTrigger> triggerThreads;

    private ExecutorService pool;

    public String getIconFileName() {
        return null;
    }

    public String getDisplayName() {
        return null;
    }

    public String getUrlName() {
        return URL;
    }

    public int getThreadCount() {
        return triggerThreads == null ? 0 : triggerThreads.size();
    }

    public RedpenRootTrigger() {
        triggerThreads = Collections.newSetFromMap(new WeakHashMap<StartTrigger, Boolean>());
        this.pool = Executors.newCachedThreadPool();
    }

    public void doIndex(StaplerRequest req,
                        StaplerResponse resp) {
        final String event = req.getHeader("X-GitHub-Event");
        final String signature = req.getHeader("X-Hub-Signature");
        final String type = req.getContentType();
        String payload = null;
        String body = null;

        if (type != null && type.toLowerCase().startsWith("application/json")) {
            body = extractRequestBody(req);
            if (body == null) {
                LOGGER.log(Level.SEVERE, "Can't get request body for application/json.");
                resp.setStatus(StaplerResponse.SC_BAD_REQUEST);
                return;
            }
            payload = body;
        } else if (type != null && type.toLowerCase().startsWith("application/x-www-form-urlencoded")) {
            body = extractRequestBody(req);
            if (body == null || body.length() <= PAYLOAD_LENGTH) {
                LOGGER.log(Level.SEVERE,
                        "Request doesn't contain payload. You are sending url encoded request, "
                                + "so you should pass github payload through 'payload' request parameter");
                resp.setStatus(StaplerResponse.SC_BAD_REQUEST);
                return;
            }
            try {
                String encoding = req.getCharacterEncoding();
                payload = URLDecoder.decode(body.substring(PAYLOAD_LENGTH), encoding != null ? encoding : "UTF-8");
            } catch (UnsupportedEncodingException e) {
                LOGGER.log(Level.SEVERE, "Error while trying to decode the payload");
                resp.setStatus(StaplerResponse.SC_BAD_REQUEST);
                return;
            }
        }

        if (payload == null) {
            LOGGER.log(Level.SEVERE,
                    "Payload is null, maybe content type ''{0}'' is not supported by this plugin. "
                            + "Please use 'application/json' or 'application/x-www-form-urlencoded'",
                    new Object[]{type});
            resp.setStatus(StaplerResponse.SC_UNSUPPORTED_MEDIA_TYPE);
            return;
        }

        LOGGER.log(Level.FINE, "Got payload event: {0}", event);
        final String threadBody = body;
        final String threadPayload = payload;
        handleAction(event, signature, threadPayload, threadBody);
    }

    private void handleAction(String event,
                              String signature,
                              String payload,
                              String body) {

        // Not sure if this is needed, but it may be to get info about old builds.
    }

    private class StartTrigger implements Runnable {

        @Override
        public void run() {
            try {
                System.out.println("trigger");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to run thread", e);
            } finally {
                triggerThreads.remove(this);
            }
        }
    }

    private String extractRequestBody(StaplerRequest req) {
        String body = null;
        BufferedReader br = null;
        try {
            br = req.getReader();
            body = IOUtils.toString(br);
        } catch (IOException e) {
            body = null;
        } finally {
            IOUtils.closeQuietly(br);
        }
        return body;
    }

    @Extension
    public static class RedpenRootTriggerCrumbExclusion extends CrumbExclusion {

        @Override
        public boolean process(HttpServletRequest req,
                               HttpServletResponse resp,
                               FilterChain chain) throws IOException, ServletException {
            String pathInfo = req.getPathInfo();
            if (pathInfo != null && pathInfo.equals(getExclusionPath())) {
                chain.doFilter(req, resp);
                return true;
            }
            return false;
        }

        public String getExclusionPath() {
            return "/" + URL + "/";
        }
    }
}
