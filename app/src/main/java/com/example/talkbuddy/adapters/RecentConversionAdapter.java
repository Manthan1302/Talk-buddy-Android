package com.example.talkbuddy.adapters;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkbuddy.R;
import com.example.talkbuddy.databinding.UserDetailsRecentChatBinding;
import com.example.talkbuddy.listeners.ConversionListeners;
import com.example.talkbuddy.models.ChatMessage;
import com.example.talkbuddy.models.User;
import com.example.talkbuddy.utilities.Constants;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class RecentConversionAdapter extends RecyclerView.Adapter<RecentConversionAdapter.ConversionViewHolder>{


    private final List<ChatMessage> chatMessages;
    private final ConversionListeners conversionListeners;
    Context context;

    FirebaseFirestore firebaseFirestore;

    TextView TextName;


    public RecentConversionAdapter(List<ChatMessage> chatMessages,ConversionListeners conversionListeners) {
        this.chatMessages = chatMessages;
        this.conversionListeners=conversionListeners;
    }

    @NonNull
    @Override
    public ConversionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ConversionViewHolder(UserDetailsRecentChatBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false));

    }

    @Override
    public void onBindViewHolder(@NonNull ConversionViewHolder holder, int position) {
        holder.setData(chatMessages.get(position));

        ConstraintLayout CLRow;

        CLRow=holder.binding.CLRow;
        CLRow.findViewById(R.id.CLRow);

        CLRow.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(CLRow.getContext());

                dialog.setTitle("Delete");
                dialog.setMessage("Are you sure ?");
                dialog.setIcon(R.drawable.baseline_delete_24);


                dialog.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //chatMessages.remove(position);
                        if(position==chatMessages.size()){
                            int b=position;
                            b--;
                            holder.remove1(b);
//                            notifyItemRemoved(b);
                        }else {
                            holder.remove1(position);


                        }
                    }
                });

                dialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(CLRow.getContext(), "Cancel...", Toast.LENGTH_SHORT).show();

                    }
                });
                dialog.show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }


    public class ConversionViewHolder extends RecyclerView.ViewHolder{

        UserDetailsRecentChatBinding binding;

        ConversionViewHolder(UserDetailsRecentChatBinding userDetailsRecentChatBinding){
            super(userDetailsRecentChatBinding.getRoot());
            binding=userDetailsRecentChatBinding;

        }

        void remove1(int i1){
            chatMessages.remove(i1);
            notifyItemRemoved(i1);
        }

        void setData(ChatMessage chatMessage){
            binding.ProfileImage.setImageBitmap(getConversionImage(chatMessage.conversionImage));
            binding.TextName.setText(chatMessage.conversionName);
            binding.textRecentMessage.setText(chatMessage.message);
            binding.getRoot().setOnClickListener(v -> {
                User user= new User();
                user.id=chatMessage.conversionId;
                user.name=chatMessage.conversionName;
                user.image=chatMessage.conversionImage;
                conversionListeners.onConversionClicked(user);
            });
        }

    }
    private Bitmap getConversionImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }
}
