package com.example.talkbuddy.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Toast;

import com.example.talkbuddy.R;
import com.example.talkbuddy.databinding.ActivitySignInBinding;
import com.example.talkbuddy.utilities.Constants;
import com.example.talkbuddy.utilities.PreferencesManager;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;

public class SignInActivity extends AppCompatActivity {

    private ActivitySignInBinding binding;//for get all id access of all avctivities
    private PreferencesManager preferencesManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        preferencesManager=new PreferencesManager(getApplicationContext());
        binding=ActivitySignInBinding.inflate(getLayoutInflater());//inflater use for take layout from view
        setContentView(binding.getRoot());
        Animation scale= AnimationUtils.loadAnimation(this,R.anim.scale);
        binding.logo.startAnimation(scale);

        //check user login ore not
        if(preferencesManager.getBoolean(Constants.KEY_IS_SIGNED_IN)){
            Intent intent =new Intent(getApplicationContext(),MainActivity.class);
            startActivity(intent);
            finish();
        }
        setListeners();
    }

    private void setListeners(){


       binding.TextCreateNewAccount.setOnClickListener(view ->
               startActivity(new Intent(getApplicationContext(),SignUpActivity.class)));
       binding.SignInButton.setOnClickListener(view -> {
           if(isValidSignInDetails())
               SignIn();

       });
    }

    private void SignIn(){
        Loading(true);
        FirebaseFirestore database= FirebaseFirestore.getInstance();
        database.collection(Constants.KEY_COLLECTION_USERS)
                .whereEqualTo(Constants.KEY_PHONENUMBER,binding.SINumber.getText().toString())
                .whereEqualTo(Constants.KEY_PASSWORD,binding.SIPassword.getText().toString())
                .get()
                .addOnCompleteListener(task -> {
                    if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                        DocumentSnapshot documentSnapshot=task.getResult().getDocuments().get(0);
                        preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                        preferencesManager.putString(Constants.KEY_USER_ID,documentSnapshot.getId());
                        preferencesManager.putString(Constants.KEY_NAME,documentSnapshot.getString(Constants.KEY_NAME));
                        preferencesManager.putString(Constants.KEY_IMAGE,documentSnapshot.getString(Constants.KEY_IMAGE));
                        Intent i_login=new Intent(getApplicationContext(),MainActivity.class);
                        i_login.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(i_login);
                    }else {
                        Loading(false);
                        ShowToast("Invalid Mobile number and password");
                    }

                });
    }

    private void Loading(boolean isLoading){
        if(isLoading){
            binding.SignInButton.setVisibility(View.INVISIBLE);
            binding.progressBar.setVisibility(View.VISIBLE);
        }else {
            binding.SignInButton.setVisibility(View.VISIBLE);
            binding.progressBar.setVisibility(View.INVISIBLE);

        }



    }
    private void ShowToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //validation for sign in
    private Boolean isValidSignInDetails(){
        if(binding.SINumber.getText().toString().trim().isEmpty()){
            ShowToast("Enter Mobile Number");
            return false;
        } else if (!Patterns.PHONE.matcher(binding.SINumber.getText().toString()).matches()) {
            ShowToast("Enter valid Mobile Number");
            return false;
        } else if (binding.SIPassword.getText().toString().trim().isEmpty()) {
            ShowToast("Enter Password");
            return false;
        }else {
            return true;
        }

    }

}