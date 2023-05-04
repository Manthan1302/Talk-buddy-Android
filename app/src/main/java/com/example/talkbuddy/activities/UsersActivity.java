package com.example.talkbuddy.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;


import com.example.talkbuddy.adapters.UsersAdapter;
import com.example.talkbuddy.databinding.ActivityUsersBinding;
import com.example.talkbuddy.listeners.UserListener;
import com.example.talkbuddy.models.User;
import com.example.talkbuddy.utilities.Constants;
import com.example.talkbuddy.utilities.PreferencesManager;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends BaseActivity implements UserListener {

    private ActivityUsersBinding binding;
    private PreferencesManager preferencesManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding=ActivityUsersBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencesManager=new PreferencesManager(getApplicationContext());
        setListeners();
        getUsers();
    }

    private void setListeners(){
        binding.ImageBack.setOnClickListener(view -> onBackPressed());
    }
    private void getUsers(){
        Loading(true);
        FirebaseFirestore database=FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .get()
                .addOnCompleteListener(task -> {
                    Loading(false);
                    String currentUserId=preferencesManager.getString(Constants.KEY_USER_ID);
                    if(task.isSuccessful() && task.getResult()!=null ){
                        List<User> users=new ArrayList<>();
                        for(QueryDocumentSnapshot queryDocumentSnapshot:task.getResult()) {
                            if (currentUserId.equals(queryDocumentSnapshot.getId())) {
                                continue;
                            }
                            User user = new User();
                            user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME);
                            user.mobileNumber = queryDocumentSnapshot.getString(Constants.KEY_PHONENUMBER);
                            user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE);
                            user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN);
                            user.id=queryDocumentSnapshot.getId();
                            users.add(user);
                        }
                        if(users.size()>0){
                            UsersAdapter usersAdapter=new UsersAdapter(users,this);
                            binding.UserRecyclerView.setAdapter(usersAdapter);
                            binding.UserRecyclerView.setVisibility(View.VISIBLE);
                        }
                        else {
                            ShowErrorMessage();
                        }
                    }else {
                        ShowErrorMessage();
                    }
                });
    }

    private void ShowErrorMessage(){
        binding.TextErrorMessage.setText(String.format("%s","User Not Available"));
        binding.TextErrorMessage.setVisibility(View.VISIBLE);
    }
    private void Loading(boolean isLoading){
        if(isLoading){
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.progressBar.setVisibility(View.INVISIBLE);

        }
    }

    public void onUserClicked(User user){
        Intent intent = new Intent(getApplicationContext(),ChatActivity.class);
        intent.putExtra(Constants.KEY_USER,user);
        startActivity(intent);
        finish();
    }


}