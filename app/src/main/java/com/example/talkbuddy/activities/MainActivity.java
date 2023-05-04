package com.example.talkbuddy.activities;



import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.talkbuddy.R;
import com.example.talkbuddy.adapters.RecentConversionAdapter;
import com.example.talkbuddy.databinding.ActivityMainBinding;
import com.example.talkbuddy.databinding.ActivitySignInBinding;
import com.example.talkbuddy.listeners.ConversionListeners;
import com.example.talkbuddy.models.ChatMessage;
import com.example.talkbuddy.models.User;
import com.example.talkbuddy.utilities.Constants;
import com.example.talkbuddy.utilities.PreferencesManager;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;


import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class    MainActivity extends BaseActivity implements ConversionListeners {

    private ActivityMainBinding binding;
    PreferencesManager preferencesManager;

    private List<ChatMessage> conversations;
    private RecentConversionAdapter recentConversionAdapter;
    private FirebaseFirestore database;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferencesManager=new PreferencesManager(getApplicationContext());



        init();
        LoadUserDetails();
        getToken();//optional
        setListener();
        listenConversation();


    }

    private void init(){
        conversations =new ArrayList<>();
        recentConversionAdapter=new RecentConversionAdapter(conversations,this);
        binding.conversionRecyclerView.setAdapter(recentConversionAdapter);
        database=FirebaseFirestore.getInstance();


    }

    private void setListener(){
        binding.ImageSignOut.setOnClickListener(v -> SignOut());
        binding.NewChat.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(),UsersActivity.class)));
       binding.ProfileImage.setOnClickListener(new View.OnClickListener() {


           @Override
           public void onClick(View view) {
               Intent intent= new Intent(MainActivity.this,dialog_profile.class);
               startActivity(intent);
           }

       });


    }
    private void LoadUserDetails(){

        binding.TextName.setText(preferencesManager.getString(Constants.KEY_NAME));
        byte[] bytes= android.util.Base64.decode(preferencesManager.getString(Constants.KEY_IMAGE), android.util.Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.ProfileImage.setImageBitmap(bitmap);


    }

    private void ShowToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void listenConversation(){

        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID,preferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_RECEIVER_ID,preferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);
    }


    private final EventListener<QuerySnapshot> eventListener =(value,error)->{
        if(error!=null){
            return;
        }
        if(value!=null){
            for(DocumentChange documentChange:value.getDocumentChanges()){
                if(documentChange.getType()== DocumentChange.Type.ADDED){
                    String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    ChatMessage chatMessage= new ChatMessage();
                    chatMessage.senderId=senderId;
                    chatMessage.recieverId=receiverId;
                    if(preferencesManager.getString(Constants.KEY_USER_ID).equals(senderId)){
                        chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_RECEIVER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_RECEIVER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    }
                    else {
                        chatMessage.conversionImage=documentChange.getDocument().getString(Constants.KEY_SENDER_IMAGE);
                        chatMessage.conversionName=documentChange.getDocument().getString(Constants.KEY_SENDER_NAME);
                        chatMessage.conversionId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    }
                 chatMessage.message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                 chatMessage.date=documentChange.getDocument().getDate(Constants.KEY_TIME);
                 conversations.add(chatMessage);
                }else if(documentChange.getType() == DocumentChange.Type.MODIFIED){
                    for(int i=0;i<conversations.size();i++)
                    {
                        String senderId=documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                        String receiverId=documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                        if(conversations.get(i).senderId.equals(senderId) && conversations.get(i).recieverId.equals(receiverId)){
                            conversations.get(i).message=documentChange.getDocument().getString(Constants.KEY_LAST_MESSAGE);
                            conversations.get(i).date=documentChange.getDocument().getDate(Constants.KEY_TIME);
                            break;
                        }
                    }
                }
            }
            Collections.sort(conversations,(obj1,obj2)-> obj2.date.compareTo(obj1.date));
            recentConversionAdapter.notifyDataSetChanged();
            binding.conversionRecyclerView.smoothScrollToPosition(0);
            binding.conversionRecyclerView.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.GONE);
        }
    };

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::UpdateToken);
    }
    private void UpdateToken(String token){
        preferencesManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferencesManager.getString(Constants.KEY_USER_ID));
        documentReference.update(Constants.KEY_FCM_TOKEN,token);
//                        .addOnSuccessListener(unused -> ShowToast("Token Updated successfully"))
//                        .addOnFailureListener(e -> ShowToast("Unable To updataToken"));
    }

    private void SignOut(){

        FirebaseFirestore database= FirebaseFirestore.getInstance();
        DocumentReference documentReference=
                database.collection(Constants.KEY_COLLECTION_USERS).document(preferencesManager.getString(Constants.KEY_USER_ID));
        HashMap<String,Object> updates=new HashMap<>();
        updates.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
        documentReference.update(updates)
                .addOnSuccessListener(unused -> {
                    preferencesManager.clear();
                    startActivity(new Intent(getApplicationContext(),SignInActivity.class));
                    finish();
                })
                .addOnFailureListener(e-> ShowToast("Unable to Sign out"));
    }

    @Override
    public void onConversionClicked(User user) {
        Intent intent=new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);

    }
}