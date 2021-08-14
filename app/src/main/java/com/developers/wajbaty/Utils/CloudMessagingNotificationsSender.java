package com.developers.wajbaty.Utils;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public class CloudMessagingNotificationsSender {

    private static final CollectionReference usersRef =
            FirebaseFirestore.getInstance().collection("Users");
    //  private static final String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    private static final APIService apiService =
            Client.getClient("https://fcm.googleapis.com/").create(APIService.class);

    public static void sendNotification(String userId, Data data) {

        if (data != null) {

            Log.d("ttt", "sending to userid: " + userId);
            usersRef.document(userId).get().addOnSuccessListener(document -> {

                final String token = document.getString("cloudMessagingToken");

//                if (GlobalVariables.getCurrentToken() == null ||
//                        GlobalVariables.getCurrentToken().equals(token))
//                    return;

                Log.d("ttt", "sending to token: " + token);
                Sender sender = new Sender(data, token);

                apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<MyResponse> call,
                                           @NonNull Response<MyResponse> response) {

//                        if (!data.getType().equals("message")) {
//                            GlobalVariables.getPreviousSentNotifications().add(data.getUser()
//                                    + data.getType() + data.getPromoId());
//                        }
                        Log.d("ttt", "notification send: " + response.message());
                    }

                    @Override
                    public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {
                        Log.d("ttt", "notification send error: " + t.getMessage());
                    }
                });
            });

        }

    }

    public interface APIService {
        @Headers(
                {
                        "Content-Type:application/json",
                        "Authorization:key=AAAA1TvAi2Q:APA91bEFvUyII3AEKn29H4Nl9cnOx5jQ1SyjDWvXFjsHJ_gWfj3dOp8san1mjiP2FExDOU2i0iWoVlJFRXVpiDdOPSxAX0I3rTszV54gVTFBx0eYL_tu8VFT8zuxxenZfMnDJkiMecJQ"
                }
        )
        @POST("fcm/send")
        Call<MyResponse> sendNotification(@Body Sender body);
    }

    public static class Client {
        private static Retrofit retrofit = null;

        public static Retrofit getClient(String url) {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder().baseUrl(url).addConverterFactory(GsonConverterFactory.create()).build();
            }
            return retrofit;
        }
    }

    private static class Sender {
        private final Data data;
        private final String to;

        Sender(Data data, String to) {
            this.data = data;
            this.to = to;
        }

        public Data getData() {
            return data;
        }

        public String getTo() {
            return to;
        }
    }


    public static class MyResponse {
        public int success;
    }

    public static class Data {

        public static final int TYPE_DELIVERY_REQUEST = 1, TYPE_MESSAGE = 2, TYPE_DRIVER_PROPOSAL = 3;

        private String senderID;
        private String title;
        private String body;
        private String imageUrl;
        private String destinationID;
        private int type;

        public Data(Map<String, String> dataMap) {

            if (dataMap.containsKey("senderID")) {
                this.senderID = dataMap.get("senderID");
            }
            if (dataMap.containsKey("title")) {
                this.title = dataMap.get("title");
            }
            if (dataMap.containsKey("body")) {
                this.body = dataMap.get("body");
            }
            if (dataMap.containsKey("imageURL")) {
                this.imageUrl = dataMap.get("imageURL");
            }

            if (dataMap.containsKey("destinationID")) {
                this.destinationID = dataMap.get("destinationID");
            }

            if (dataMap.containsKey("type")) {
                this.type = Integer.parseInt(dataMap.get("type"));
            }


        }

        public Data(String senderID, String title, String body, String imageUrl, String destinationID, int type) {
            this.senderID = senderID;
            this.title = title;
            this.body = body;
            this.imageUrl = imageUrl;
            this.destinationID = destinationID;
            this.type = type;
        }

//        public static class MessageData extends Data{
//
//            String
//
//
//        }

        public String getSenderID() {
            return senderID;
        }

        public void setSenderID(String senderID) {
            this.senderID = senderID;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getDestinationID() {
            return destinationID;
        }

        public void setDestinationID(String destinationID) {
            this.destinationID = destinationID;
        }

        public int getType() {
            return type;
        }

        public void setType(int type) {
            this.type = type;
        }
    }
}
