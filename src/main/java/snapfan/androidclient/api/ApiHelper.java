package snapfan.androidclient.api;

import com.github.kevinsawicki.http.HttpRequest;
import net.pickapack.io.serialization.JsonSerializationHelper;
import net.pickapack.notice.model.forum.Forum;
import net.pickapack.notice.model.forum.ForumThread;
import net.pickapack.notice.model.forum.ForumThreadMessage;
import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

public class ApiHelper {
//    private static final String SERVER_URL = "http://10.26.27.29:3721";
    private static final String SERVER_URL = "http://10.0.2.2:3721";
//    private static final String SERVER_URL = "http://110.34.174.170:3721";

    public static String login(final String userId, final String password) {
        return call(new HashMap<String, String>(){{
            put("action", "login");
            put("userId", userId);
            put("password", password);
        }});
    }

    //TODO: add handling for news items

    public static Forum[] getAllForums() {
        return call(new HashMap<String, String>(){{
            put("action", "getAllForums");
        }}, Forum[].class);
    }

    public static ForumThread[] getForumThreadsByParentId(final long parentId) {
        return call(new HashMap<String, String>(){{
            put("action", "getForumThreadsByParentId");
            put("parentId", parentId + "");
        }}, ForumThread[].class);
    }

    public static ForumThreadMessage[] getForumThreadMessagesByParentId(final long parentId) {
        return call(new HashMap<String, String>(){{
            put("action", "getForumThreadMessagesByParentId");
            put("parentId", parentId + "");
        }}, ForumThreadMessage[].class);
    }

    public static Forum getParentByForumId(final long forumId) {
        return call(new HashMap<String, String>(){{
            put("action", "getParentByForumId");
            put("forumId", forumId + "");
        }}, Forum.class);
    }

    public static Forum getParentByForumThreadId(final long forumThreadId) {
        return call(new HashMap<String, String>(){{
            put("action", "getParentByForumThreadId");
            put("forumThreadId", forumThreadId + "");
        }}, Forum.class);
    }

    public static ForumThread getParentByForumThreadMessageId(final long forumThreadMessageId) {
        return call(new HashMap<String, String>(){{
            put("action", "getParentByForumThreadMessageId");
            put("forumThreadMessageId", forumThreadMessageId + "");
        }}, ForumThread.class);
    }

    private static String call(Map<String, String> data) {
        try {
            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            return request.body("utf-8").trim();
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    private static <T> T call(Map<String, String> data, Class<T> clz) {
        try {
            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(clz, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
