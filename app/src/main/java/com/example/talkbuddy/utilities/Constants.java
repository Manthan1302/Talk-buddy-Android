package com.example.talkbuddy.utilities;

import java.util.HashMap;

public class Constants {

    public static final String KEY_COLLECTION_USERS="Users";
    public static final String KEY_NAME="Name";
    public static final String KEY_PHONENUMBER="phoneNumber";
    public static final String KEY_PASSWORD="Password";
    public static final String KEY_PREFERENCE_NAME="ChatAppPreference";
    public static final String KEY_IS_SIGNED_IN="IsSignedIn";
    public static final String KEY_USER_ID="UserId";
    public static final String KEY_IMAGE="Image";

    public  static final String KEY_FCM_TOKEN="FcmToken";

    public static final String KEY_USER = "user";





    public static final String KEY_CHAT_COLLECTION = "chat";
    public static final String KEY_SENDER_ID = "senderId";
    public static final String KEY_RECEIVER_ID = "receiverId";
    public static final String KEY_MESSAGE = "message";
    public static final String KEY_TIME = "time";









    public static final String KEY_COLLECTION_CONVERSATIONS = "conversations";
    public static final String KEY_SENDER_NAME = "sendername";
    public static final String KEY_RECEIVER_NAME = "receivername";
    public static final String KEY_SENDER_IMAGE = "senderimage";
    public static final String KEY_RECEIVER_IMAGE = "receiverimage";
    public static final String KEY_LAST_MESSAGE = "lastmessage";
    public static final String KEY_AVAILABILITY = "availability";
    public static final String MSG_AUTHORIZATION = "authorization";
    public static final String MSG_CONTENT_TYPE = "content_type";
    public static final String MSG_DATA = "data";
    public static final String MSG_REGISTRATION_IDS = "registration_ids";

    public static HashMap<String,String> msgHeaders=null;

    public static HashMap<String,String> getMsgHeaders(){
        if(msgHeaders==null){
            msgHeaders=new HashMap<>();
            msgHeaders.put(MSG_AUTHORIZATION,"key=AAAADv9FqDg:APA91bGos_gyWa7xTssTtUMYU_LkZwkkJJo29sdctPHPhtvVQVLJkb-p6lsvBoo4hIM3Ml6jShmCXElbVUipqmKFbefoJsBPAM7NzRna-u3_53AdN-Ly6LmjeBhtf6_nX1mMGbfxWppy");
            msgHeaders.put(MSG_CONTENT_TYPE,"application/json");

        }
        return msgHeaders;
    }




}
