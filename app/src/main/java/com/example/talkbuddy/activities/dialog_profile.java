package com.example.talkbuddy.activities;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.talkbuddy.R;
import com.example.talkbuddy.utilities.Constants;
import com.example.talkbuddy.utilities.PreferencesManager;
import com.makeramen.roundedimageview.RoundedImageView;

public class dialog_profile extends AppCompatActivity {

    RoundedImageView Profileimage1;
    PreferencesManager preferencesManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_profile);
        preferencesManager=new PreferencesManager(getApplicationContext());

        Profileimage1=findViewById(R.id.Profile);


        byte[] bytes1= android.util.Base64.decode(preferencesManager.getString(Constants.KEY_IMAGE), android.util.Base64.DEFAULT);
        Bitmap bitmap= BitmapFactory.decodeByteArray(bytes1,0,bytes1.length);

//               dialog =new Dialog(MainActivity.this);
//               dialog.setContentView(R.layout.dialog_profile);
//               dialog.show();
        Profileimage1.setImageBitmap(bitmap);
    }

}
