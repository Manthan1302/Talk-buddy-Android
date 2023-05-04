package com.example.talkbuddy.adapters;

import android.graphics.Bitmap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkbuddy.R;
import com.example.talkbuddy.databinding.RecievedMessageContainerBinding;
import com.example.talkbuddy.databinding.SentMessageContainerBinding;
import com.example.talkbuddy.models.ChatMessage;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.nl.translate.TranslateLanguage;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private Spinner fromSpinner;
    String[] fromLanguage = {"From","English","Afrikaans","Arabic","Belarusin","Bulgarian","Bengali","Catalan","Czech","Welsh","Hindi","urdu"};
    private static final int REQUEST_PERMISSION_CODE = 1;
    String languageCode,fromLanguageCode, toLanguageCode=null;
    private final List<ChatMessage> chatMessages;
    private Bitmap receiverProfileImage;
    private final String senderId;

    public static final int VIEW_TYPE_SENT = 1;
    public static final int VIEW_TYPE_RECEIVED = 2;

    public void setReceiverProfileImage(Bitmap bitmap){
        receiverProfileImage=bitmap;
    }
    public ChatAdapter(List<ChatMessage> chatMessages, Bitmap receiverProfileImage, String senderId) {
        this.chatMessages = chatMessages;
        this.receiverProfileImage = receiverProfileImage;
        this.senderId = senderId;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == VIEW_TYPE_SENT){
            return new SentMessageViewHolder(
                    SentMessageContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }else {
            return new ReceivedMessageViewHolder(
                   RecievedMessageContainerBinding.inflate(
                            LayoutInflater.from(parent.getContext()),parent,false
                    )
            );
        }

    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if(getItemViewType(position) == VIEW_TYPE_SENT){
            ((SentMessageViewHolder)holder).setData(chatMessages.get(position));
        }else{
            ((ReceivedMessageViewHolder)holder).setData(chatMessages.get(position),
                    receiverProfileImage);
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    @Override
    public int getItemViewType(int position) {
        if(chatMessages.get(position).senderId.equals(senderId)){
            return VIEW_TYPE_SENT;
        }else {
            return VIEW_TYPE_RECEIVED;
        }
    }

    static class SentMessageViewHolder extends RecyclerView.ViewHolder{

        //using binding layout final automatic connected with java file
        private final SentMessageContainerBinding binding;

        SentMessageViewHolder(SentMessageContainerBinding sentMessageContainerBinding){
            super(sentMessageContainerBinding.getRoot());
            binding=sentMessageContainerBinding;
        }

        void setData(ChatMessage chatMessage){
            binding.TextMessage.setText(chatMessage.message);
            binding.TextDateTime.setText(chatMessage.dateTime);
        }
    }

    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder{

        private final RecievedMessageContainerBinding binding;

        ReceivedMessageViewHolder(RecievedMessageContainerBinding recievedMessageContainerBinding){
            super(recievedMessageContainerBinding.getRoot());
            binding=recievedMessageContainerBinding;
        }

        void setData(ChatMessage chatMessage,Bitmap receiverProfileImage){
            binding.TextMessage.setText(chatMessage.message);
            binding.TextDateTime.setText(chatMessage.dateTime);
            if(receiverProfileImage!=null){
                binding.ImageProfile.setImageBitmap(receiverProfileImage);
            }
        }
    }



//    private void translateText(String fromLanguageCode, String toLanguageCode, String source) {
//        TranslatorOptions options =
//                new TranslatorOptions.Builder()
//                        .setSourceLanguage(fromLanguageCode)
//                        .setTargetLanguage(toLanguageCode)
//                        .build();
//        final Translator englishGermanTranslator =
//                Translation.getClient(options);
//        DownloadConditions conditions = new DownloadConditions.Builder()
//                .requireWifi()
//                .build();
//        englishGermanTranslator.downloadModelIfNeeded(conditions)
//                .addOnSuccessListener(
//                        new OnSuccessListener< Void>() {
//                            @Override
//                            public void onSuccess(Void v) {
//                                // Model downloaded successfully. Okay to start translating.
//                                // (Set a flag, unhide the translation UI, etc.)
//                                Log.d("FromSource", String.valueOf(fromLanguageCode));
//                                Log.d("toSource", String.valueOf(toLanguageCode));
//                                englishGermanTranslator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
//                                    @Override
//                                    public void onSuccess(String s) {
//
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                                    @Override
//                                    public void onFailure(@NonNull Exception e) {
//
//                                    }
//                                });
//                            }
//                        })
//                .addOnFailureListener(
//                        new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception e) {
//                                // Model couldn’t be downloaded or other internal error.
//                                // ...
//                            }
//                        });
//    }
//
//    private String getLanguageCode(String language)
//    {
//        String languageCode = String.valueOf(0);
//        switch (language){
//            case "English":
//                languageCode = TranslateLanguage.ENGLISH;
//                break;
//            case "Afrikaans":
//                languageCode = TranslateLanguage.AFRIKAANS;
//                break;
//            case "Arabic":
//                languageCode = TranslateLanguage.ARABIC;
//                break;
//            case "Belarusin":
//                languageCode = TranslateLanguage.BELARUSIAN;
//                break;
//            case "Bulgarian":
//                languageCode = TranslateLanguage.BULGARIAN;
//                break;
//            case "Bengali":
//                languageCode = TranslateLanguage.BENGALI;
//                break;
//            case "Catalan":
//                languageCode = TranslateLanguage.CATALAN;
//                break;
//            case "Czech":
//                languageCode = TranslateLanguage.CZECH;
//                break;
//            case "Welsh":
//                languageCode = TranslateLanguage.WELSH;
//                break;
//            case "Hindi":
//                languageCode = TranslateLanguage.HINDI;
//                break;
//            case "urdu":
//                languageCode = TranslateLanguage.URDU;
//                break;
//            default:
//                languageCode = "From";
//        }
//        return languageCode;
//    }
}
