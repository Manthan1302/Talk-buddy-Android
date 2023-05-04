package com.example.talkbuddy.activities;


import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.talkbuddy.R;
import com.example.talkbuddy.adapters.ChatAdapter;
import com.example.talkbuddy.databinding.ActivityChatBinding;
import com.example.talkbuddy.models.ChatMessage;
import com.example.talkbuddy.models.User;
import com.example.talkbuddy.network.ApiClient;
import com.example.talkbuddy.network.ApiService;
import com.example.talkbuddy.utilities.Constants;
import com.example.talkbuddy.utilities.PreferencesManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.core.QueryListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import org.checkerframework.checker.nullness.qual.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends BaseActivity {

    private ActivityChatBinding binding;
    private User reciverUser;
    private List<ChatMessage> chatmessage;
    private ChatAdapter chatAdapter;
    private PreferencesManager preferencesManager;
    private FirebaseFirestore database;
    private Spinner fromSpinner;
    private String conversionId = null;

    private Boolean isReceiverAvailable = false;
    private static final int REQUEST_PERMISSION_CODE = 1;

    private TextView translateTV;
    String[] fromLanguage = {"English", "Afrikaans", "Arabic", "Bengali","Hindi", "urdu", "Gujarati"};
    String fromLanguageCode = null;

    int count=0;
    String trans;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        loadReceiverDetails();
        setListeners();

        fromSpinner = findViewById(R.id.spinner_language);

        translateTV = findViewById(R.id.idTranslatedTV);

        fromSpinner.setVisibility(View.VISIBLE);
        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguage[i]);
                count=i;
                translateTV.setText(fromLanguage[count]);
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                fromSpinner.setVisibility(View.VISIBLE);
            }
        });
        init();
        SendReceivedMessage();

        ArrayAdapter fromAdapter = new ArrayAdapter(this, R.layout.spinner_item, fromLanguage);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

    }

    private void init() {
        preferencesManager = new PreferencesManager(getApplicationContext());
        chatmessage = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatmessage, getBitmapFromEncodedString(reciverUser.image), preferencesManager.getString(Constants.KEY_USER_ID));
        binding.ChatRecyclerView.setAdapter(chatAdapter);
        database = FirebaseFirestore.getInstance();
    }

    private void sendMessage() {
        HashMap<String, Object> message = new HashMap<>();
        message.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
        message.put(Constants.KEY_RECEIVER_ID, reciverUser.id);
        message.put(Constants.KEY_TIME, new Date());

//        translateTV.setText("");
        translateText(TranslateLanguage.ENGLISH,fromLanguageCode,binding.InputMessage.getText().toString());


        if(binding.InputMessage.getText().toString().isEmpty()){
            ShowToast("Type Something");
        }else{
            if (fromLanguageCode!=TranslateLanguage.ENGLISH){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(trans==null){
                            ShowToast("Network Error");
                        }else{
                            message.put(Constants.KEY_MESSAGE,trans);
                            database.collection(Constants.KEY_CHAT_COLLECTION).add(message);
                        }

                    }
                },4000);
            }
            else{
                message.put(Constants.KEY_MESSAGE,binding.InputMessage.getText().toString());
                database.collection(Constants.KEY_CHAT_COLLECTION).add(message);
            }
        }

        if (conversionId != null) {
            if (fromLanguageCode!=TranslateLanguage.ENGLISH){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateConversion(trans);
                    }
                },2000);
            }else
                updateConversion(binding.InputMessage.getText().toString());
        } else {
            HashMap<String, Object> conversion = new HashMap<>();
            conversion.put(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
            conversion.put(Constants.KEY_SENDER_NAME, preferencesManager.getString(Constants.KEY_NAME));
            conversion.put(Constants.KEY_SENDER_IMAGE, preferencesManager.getString(Constants.KEY_IMAGE));
            conversion.put(Constants.KEY_RECEIVER_ID, reciverUser.id);
            conversion.put(Constants.KEY_RECEIVER_NAME, reciverUser.name);
            conversion.put(Constants.KEY_RECEIVER_IMAGE, reciverUser.image);
            if (fromLanguageCode!=TranslateLanguage.ENGLISH){
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        conversion.put(Constants.KEY_LAST_MESSAGE,trans);
                    }
                },2000);
             }
            else{
                conversion.put(Constants.KEY_LAST_MESSAGE, binding.InputMessage.getText().toString());
            }

//            conversion.put(Constants.KEY_LAST_MESSAGE, binding.InputMessage.getText().toString());
            conversion.put(Constants.KEY_TIME, new Date());
            addConversion(conversion);
        }
        if (isReceiverAvailable) {
            try {
                JSONArray tokens = new JSONArray();
                tokens.put(reciverUser.token);
                JSONObject data = new JSONObject();
                data.put(Constants.KEY_USER_ID, preferencesManager.getString(Constants.KEY_USER_ID));
                data.put(Constants.KEY_NAME, preferencesManager.getString(Constants.KEY_NAME));
                data.put(Constants.KEY_FCM_TOKEN, preferencesManager.getString(Constants.KEY_FCM_TOKEN));
                data.put(Constants.KEY_MESSAGE, binding.InputMessage.getText().toString());

                JSONObject body = new JSONObject();
                body.put(Constants.MSG_DATA, data);
                body.put(Constants.MSG_REGISTRATION_IDS, tokens);

//                SendNotification(body.toString());

            } catch (Exception exception) {
                ShowToast(exception.getMessage());
            }
        }
        binding.InputMessage.setText(null);
    }


    private void ShowToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

//    private void SendNotification(String messageBody) {
//        ApiClient.getClient().create(ApiService.class).sendMessage(
//                Constants.getMsgHeaders(),
//                messageBody
//        ).enqueue(new Callback<String>() {
//            @Override
//            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
//                if (response.isSuccessful()) {
//                    try {
//                        if (response.body() != null) {
//                            JSONObject responseJson = new JSONObject(response.body());
//                            JSONArray results = responseJson.getJSONArray("results");
//                            if (responseJson.getInt("failure") == 1) {
//                                JSONObject error = (JSONObject) results.get(0);
//                                ShowToast(error.getString("error"));
//                                return;
//                            }
//                        }
//                    } catch (JSONException e) {
//                        e.printStackTrace();
//                    }
//                    ShowToast("Notification Sent Successfully");
//                } else {
//                    ShowToast("Error:" + response.code());
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<String> call, @NonNull Throwable t) {
//                ShowToast(t.getMessage());
//            }
//        });
//    }

    private void listenAvailabilityOfReceiver() {
        database.collection(Constants.KEY_COLLECTION_USERS).document(
                reciverUser.id
        ).addSnapshotListener(ChatActivity.this, (value, error) -> {
            if (error != null) {
                return;
            }
            if (value != null) {
                if (value.getLong(Constants.KEY_AVAILABILITY) != null) {
                    int availability = Objects.requireNonNull(
                            value.getLong(Constants.KEY_AVAILABILITY)
                    ).intValue();
                    isReceiverAvailable = availability == 1;
                }
                reciverUser.token = value.getString(Constants.KEY_FCM_TOKEN);
                if (reciverUser.image == null) {
                    reciverUser.image = value.getString(Constants.KEY_IMAGE);
                    chatAdapter.setReceiverProfileImage(getBitmapFromEncodedString(reciverUser.image));
                    chatAdapter.notifyItemRangeChanged(0, chatmessage.size());
                }

            }
            if (isReceiverAvailable) {
                binding.UserAvailability.setVisibility(View.VISIBLE);
            } else {
                binding.UserAvailability.setVisibility(View.GONE);
            }
        });
    }

    private void SendReceivedMessage() {
        database.collection(Constants.KEY_CHAT_COLLECTION)
                .whereEqualTo(Constants.KEY_SENDER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .whereEqualTo(Constants.KEY_RECEIVER_ID, reciverUser.id)
                .addSnapshotListener(eventListener);
        database.collection(Constants.KEY_CHAT_COLLECTION)
                .whereEqualTo(Constants.KEY_SENDER_ID, reciverUser.id)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, preferencesManager.getString(Constants.KEY_USER_ID))
                .addSnapshotListener(eventListener);

    }

    private final EventListener<QuerySnapshot> eventListener = (value, error) -> {

        if (error != null) {
            return;
        }
        if (value != null) {
            int count = chatmessage.size();
            for (DocumentChange documentChange : value.getDocumentChanges()) {
                if (documentChange.getType() == DocumentChange.Type.ADDED) {
                    ChatMessage chatMessage = new ChatMessage();
                    chatMessage.senderId = documentChange.getDocument().getString(Constants.KEY_SENDER_ID);
                    chatMessage.recieverId = documentChange.getDocument().getString(Constants.KEY_RECEIVER_ID);
                    chatMessage.message = documentChange.getDocument().getString(Constants.KEY_MESSAGE);
                    chatMessage.dateTime = getDateTime(documentChange.getDocument().getDate(Constants.KEY_TIME));
                    chatMessage.date = documentChange.getDocument().getDate(Constants.KEY_TIME);
                    chatmessage.add(chatMessage);
                }

            }
            Collections.sort(chatmessage, (obj1, obj2) -> obj1.date.compareTo(obj2.date));
            if (count == 0) {
                chatAdapter.notifyDataSetChanged();
            } else {
                chatAdapter.notifyItemRangeInserted(chatmessage.size(), chatmessage.size());
                binding.ChatRecyclerView.smoothScrollToPosition(chatmessage.size() - 1);
            }
            binding.ChatRecyclerView.setVisibility(View.VISIBLE);
        }
        binding.progressBar.setVisibility(View.GONE);
        if (conversionId == null) {
            checkConversion();
        }


    };

    private Bitmap getBitmapFromEncodedString(String encodedImage) {
        if (encodedImage != null) {
            byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        } else {
            return null;
        }
    }

    private void loadReceiverDetails() {
        reciverUser = (User) getIntent().getSerializableExtra(Constants.KEY_USER);
        binding.TextName.setText(reciverUser.name);
    }

    private void setListeners() {
        binding.ImageBack.setOnClickListener(v -> onBackPressed());
        binding.SendMesage.setOnClickListener(view -> sendMessage());

    }

    private String getDateTime(Date date) {
        return new SimpleDateFormat("MMMM dd,yyyy - hh:mm a", Locale.getDefault()).format(date);
    }

    private void addConversion(HashMap<String, Object> conversion) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .add(conversion)
                .addOnSuccessListener(documentReference -> conversionId = documentReference.getId());
    }

    private void updateConversion(String message) {
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_CONVERSATIONS).document(conversionId);
        documentReference.update(
                Constants.KEY_LAST_MESSAGE, message,
                Constants.KEY_TIME, new Date()
        );
    }

    private void checkConversion() {
        if (chatmessage.size() != 0) {
            checkConversionRemote(preferencesManager.getString(Constants.KEY_USER_ID), reciverUser.id);
            checkConversionRemote(reciverUser.id, preferencesManager.getString(Constants.KEY_USER_ID));
        }
    }

    private void checkConversionRemote(String senderId, String receiverId) {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
                .whereEqualTo(Constants.KEY_SENDER_ID, senderId)
                .whereEqualTo(Constants.KEY_RECEIVER_ID, receiverId)
                .get()
                .addOnCompleteListener(conversionOnCompleteListener);

    }

    private final OnCompleteListener<QuerySnapshot> conversionOnCompleteListener = task -> {
        if (task.isSuccessful() && task.getResult() != null && task.getResult().getDocuments().size() > 0) {
            DocumentSnapshot documentSnapshot = task.getResult().getDocuments().get(0);
            conversionId = documentSnapshot.getId();
        }

    };

    @Override
    protected void onResume() {
        super.onResume();
        listenAvailabilityOfReceiver();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_PERMISSION_CODE)
        {
            if (resultCode==RESULT_OK && data!=null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                binding.InputMessage.setText(result.get(0));
            }
        }
    }

    private void translateText(String fromLanguageCode, String toLanguageCode, String source) {
        TranslatorOptions options =
                new TranslatorOptions.Builder()
                        .setSourceLanguage(fromLanguageCode)
                        .setTargetLanguage(toLanguageCode)
                        .build();
        final Translator englishGermanTranslator =
                Translation.getClient(options);
        DownloadConditions conditions = new DownloadConditions.Builder()
                .requireWifi()
                .build();
        englishGermanTranslator.downloadModelIfNeeded(conditions)
                .addOnSuccessListener(
                        new OnSuccessListener< Void>() {
                            @Override
                            public void onSuccess(Void v) {
                                // Model downloaded successfully. Okay to start translating.
                                // (Set a flag, unhide the translation UI, etc.)
                                Log.d("FromSource", String.valueOf(fromLanguageCode));
                                Log.d("toSource", String.valueOf(toLanguageCode));
                                translateTV.setText("Translation...");
                                englishGermanTranslator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                                    @Override
                                    public void onSuccess(String s) {
                                        trans=s;
                                        translateTV.setText(fromLanguage[count]);
//                                        translateTV.setText(s);
//                                        trans=translateTV.getText().toString();
//                                        HashMap<String, Object> message = new HashMap<>();
//                                        message.put(Constants.KEY_MESSAGE,trans);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@androidx.annotation.NonNull Exception e) {
                                        Toast.makeText(ChatActivity.this, "Failed to Translate!! try again", Toast.LENGTH_SHORT).show();
                                    }
                                });
                            }
                        })
                .addOnFailureListener(
                        new OnFailureListener() {
                            @Override
                            public void onFailure(@androidx.annotation.NonNull Exception e) {
                                // Model couldn’t be downloaded or other internal error.
                                // ...
                            }
                        });
    }

    private String getLanguageCode(String language)
    {
        String languageCode;
//        = String.valueOf(0);
        switch (language){
            case "English":
                languageCode = TranslateLanguage.ENGLISH;
                break;
            case "Afrikaans":
                languageCode = TranslateLanguage.AFRIKAANS;
                break;
            case "Arabic":
                languageCode = TranslateLanguage.ARABIC;
                break;
//            case "Belarusin":
//                languageCode = TranslateLanguage.BELARUSIAN;
//                break;
//            case "Bulgarian":
//                languageCode = TranslateLanguage.BULGARIAN;
//                break;
            case "Bengali":
                languageCode = TranslateLanguage.BENGALI;
                break;
//            case "Catalan":
//                languageCode = TranslateLanguage.CATALAN;
//                break;
//            case "Czech":
//                languageCode = TranslateLanguage.CZECH;
//                break;
//            case "Welsh":
//                languageCode = TranslateLanguage.WELSH;
//                break;
            case "Hindi":
                languageCode = TranslateLanguage.HINDI;
                break;
            case "urdu":
                languageCode = TranslateLanguage.URDU;
                break;
            case "Gujarati":
                languageCode = TranslateLanguage.GUJARATI;
                break;
            default:
                languageCode = TranslateLanguage.ENGLISH;
        }
        return languageCode;
    }
}




