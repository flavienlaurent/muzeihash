package com.flavienlaurent.muzei.hash;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Query;

/**
 */
public interface GooglePlusService {

    public static String API_URL = "https://www.googleapis.com/plus/v1";

    @GET("/activities?fields=items(actor(displayName,id),object(actor(displayName,id),attachments(displayName,fullImage/url,id)),url,title)&key=" + Config.API_KEY)
    public Result fetch(@Query("query") String query);

    static class Result {
        List<Item> items;
    }

    static class Item {
        Actor actor;
        Object object;

        @Override
        public String toString() {
            return "Item{" +
                    "actor=" + actor +
                    ", object=" + object +
                    '}';
        }
    }

    static class Object {
        Actor actor;
        String url;
        String title;
        List<Attachment> attachments;

        @Override
        public String toString() {
            return "Object{" +
                    "actor=" + actor +
                    "url=" + url +
                    "title=" + title +
                    ", attachments=" + attachments +
                    '}';
        }
    }

    static class Actor {
        String id;
        String displayName;

        @Override
        public String toString() {
            return "Actor{" +
                    "id='" + id + '\'' +
                    ", displayName='" + displayName + '\'' +
                    '}';
        }
    }

    static class Attachment {
        String id;
        String displayName;
        FullImage fullImage;

        @Override
        public String toString() {
            return "Attachment{" +
                    "id='" + id + '\'' +
                    ", displayName='" + displayName + '\'' +
                    ", fullImage=" + fullImage +
                    '}';
        }
    }

    static class FullImage {
        String url;

        @Override
        public String toString() {
            return "FullImage{" +
                    "url='" + url + '\'' +
                    '}';
        }
    }
}
