package com.example.talkbuddy.activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.talkbuddy.databinding.ActivitySignUpBinding;
import com.example.talkbuddy.models.User;
import com.example.talkbuddy.utilities.Constants;
import com.example.talkbuddy.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class  SignUpActivity extends AppCompatActivity {

    private ActivitySignUpBinding binding;
    public String EncodedImage;
    private PreferencesManager preferencesManager;

    boolean data;

    String numbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Access all id's using binding
        binding=ActivitySignUpBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        //call preference manager constructor()
        preferencesManager=new PreferencesManager(this);
        setListeners();
    }

    private void setListeners(){
        //Buttton for go to sign in
        binding.TextSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i_signUp_page= new Intent(getApplicationContext(),SignInActivity.class);
                i_signUp_page.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);//for clear activity stack after activity change
                startActivity(i_signUp_page);
            }
        });
        //Buttton for go to sign up after data add
        binding.SignUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(IsValidSignUpDetails()){
                        SignUp();


                    }//if all data is valid then do signup else show toast
            }
        });

        //button for set profilepic
        binding.ProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i_set_image=new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i_set_image.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                PickImage.launch(i_set_image);
            }
        });
    }


    public void ShowToast(String message){
        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private void SignUp(){
        Loading(true);
        FirebaseFirestore firebaseFireStore= FirebaseFirestore.getInstance();
        HashMap<String,Object> user= new HashMap<>();
        //put data in database
        user.put(Constants.KEY_NAME,binding.InputName.getText().toString());
        user.put(Constants.KEY_PHONENUMBER,binding.SUInputNumber.getText().toString());
        user.put(Constants.KEY_PASSWORD,binding.SUPassword.getText().toString());
        user.put(Constants.KEY_IMAGE,EncodedImage);
        firebaseFireStore.collection(Constants.KEY_COLLECTION_USERS)//create collection name
                .add(user)
                .addOnSuccessListener(documentReference -> {
                    Loading(false);
                    preferencesManager.putBoolean(Constants.KEY_IS_SIGNED_IN,true);
                    preferencesManager.putString(Constants.KEY_USER_ID, documentReference.getId());
                    preferencesManager.putString(Constants.KEY_NAME,binding.InputName.getText().toString());
                    preferencesManager.putString(Constants.KEY_IMAGE,EncodedImage);
                    Intent intent=new Intent(getApplicationContext(),SignInActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK| Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);//if signup then go to mainactivity
                    finish();

                })
                .addOnFailureListener(exception ->{

                    Loading(false);
                    ShowToast(exception.getMessage());
                });

    }

    //convert image to bitmap and add in bytearray
    private String EncodeImage(Bitmap bitmap){
        int PreviewWidth=150;
        int PreviewHeight = bitmap.getHeight() * PreviewWidth/bitmap.getWidth();
        Bitmap PreviewBitmap=Bitmap.createScaledBitmap(bitmap,PreviewWidth,PreviewHeight,false);
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        PreviewBitmap.compress(Bitmap.CompressFormat.JPEG,50,byteArrayOutputStream);
        byte[] byteArray=byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray,Base64.DEFAULT);


    }


    //get image from gallery and et on profilepic
    private final ActivityResultLauncher<Intent> PickImage=registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if(result.getResultCode()==RESULT_OK)
                {
                    if(result.getData()!=null){
                        Uri imageUri=result.getData().getData();
                        try {

                            InputStream inputStream=getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap= BitmapFactory.decodeStream(inputStream);
                            binding.ProfileImage.setImageBitmap(bitmap);
                            binding.TextAddImage.setVisibility(View.GONE);
                            EncodedImage=EncodeImage(bitmap);

                        }catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

    );


        private boolean IsValidSignUpDetails () {//validation for all fields

            String MobilePattern = "[0-9]{10}";
            FirebaseFirestore database1= FirebaseFirestore.getInstance();
            database1.collection(Constants.KEY_COLLECTION_USERS)
                    .whereEqualTo(Constants.KEY_PHONENUMBER,binding.SUInputNumber.getText().toString())
                    .get()
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful() && task.getResult()!=null && task.getResult().getDocuments().size()>0){
                            data=false;
                        }else {
                            data=true;
                        }

                    });


            if (EncodedImage == null) {
                ShowToast("Select Profile Image");
                return false;
            } else if (binding.InputName.getText().toString().trim().isEmpty()) {
                ShowToast("Enter Name");
                return false;
            } else if (binding.SUInputNumber.getText().toString().trim().isEmpty()) {
                ShowToast("Enter Number");
                return false;
            } else if (!binding.SUInputNumber.getText().toString().matches(MobilePattern)) {
                Toast.makeText(getApplicationContext(), "Please enter valid 10 digit phone number", Toast.LENGTH_SHORT).show();
                return false;
            } else if (!data) {
                ShowToast("Account Already Exists");
                return false;
            }
            else if (binding.SUPassword.getText().toString().trim().isEmpty()) {
                ShowToast("Enter Password");
                return false;
            } else if (binding.SUConfirmPassword.getText().toString().trim().isEmpty()) {
                ShowToast("Confirm Your Password");
                return false;
            } else if (!binding.SUPassword.getText().toString().equals(binding.SUConfirmPassword.getText().toString())) {
                ShowToast("Password must be same");
                return false;
            } else
                return true;
        }



    private void Loading(Boolean IsLoading){//lpading show while data store in database
        if(IsLoading){
            binding.SignUpButton.setVisibility(View.INVISIBLE);
            binding.ProgressBar.setVisibility(View.VISIBLE);
        }
        else {
            binding.ProgressBar.setVisibility(View.INVISIBLE);
            binding.SignUpButton.setVisibility(View.VISIBLE);
        }
    }
}