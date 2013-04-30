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

    public static String login(String userId, String password) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "login");
            data.put("userId", userId);
            data.put("password", password);

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            return request.body("utf-8");
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Forum[] getAllForums() {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "getAllForums");

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(Forum[].class, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static ForumThread[] getForumThreadsByParentId(long parentId) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "getForumThreadsByParentId");
            data.put("parentId", parentId + "");

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(ForumThread[].class, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static ForumThreadMessage[] getForumThreadMessagesByParentId(long parentId) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "getForumThreadMessagesByParentId");
            data.put("parentId", parentId + "");

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(ForumThreadMessage[].class, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Forum getParentByForumId(long forumId) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "getParentByForumId");
            data.put("forumId", forumId + "");

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(Forum.class, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static Forum getParentByForumThreadId(long forumThreadId) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "getParentByForumThreadId");
            data.put("forumThreadId", forumThreadId + "");

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(Forum.class, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public static ForumThread getParentByForumThreadMessageId(long forumThreadMessageId) {
        try {
            Map<String, String> data = new HashMap<String, String>();
            data.put("action", "getParentByForumThreadMessageId");
            data.put("forumThreadMessageId", forumThreadMessageId + "");

            HttpRequest request = HttpRequest.post(SERVER_URL).connectTimeout(6000).readTimeout(30000).form(data);

            if(request.code() != HttpStatus.SC_OK) {
                return null;
            }

            String result = request.body("utf-8").trim();
            return JsonSerializationHelper.deserialize(ForumThread.class, result);
        } catch (HttpRequest.HttpRequestException e) {
            return null;
        } catch (Exception e) {
            return null;
        }
    }
}
