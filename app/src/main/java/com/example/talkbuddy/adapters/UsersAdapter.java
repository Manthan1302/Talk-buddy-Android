package com.example.talkbuddy.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.talkbuddy.databinding.UserDetailsBinding;
import com.example.talkbuddy.listeners.UserListener;
import com.example.talkbuddy.models.User;

import java.util.List;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UserViewHolder>{


    private final List<User> users;
    private final UserListener userListener;

    public UsersAdapter(List<User> users,UserListener userListener) {

        this.users = users;
        this.userListener=userListener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        UserDetailsBinding userDetailsBinding=UserDetailsBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);

        return new UserViewHolder(userDetailsBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {

        holder.SetUserData(users.get(position));
    }

    @Override
    public int getItemCount() {
        return users.size();
    }


    //Decode Image
    private Bitmap getUserImage(String encodedImage){
        byte[] bytes= Base64.decode(encodedImage,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,0,bytes.length);
    }

    class UserViewHolder extends RecyclerView.ViewHolder{

        ///connect userdetail layout using binding
        UserDetailsBinding binding;
        UserViewHolder(UserDetailsBinding userDetailsBinding){
            super(userDetailsBinding.getRoot());
            binding=userDetailsBinding;
        }


        //setuser data on row
        void SetUserData(User user){
            binding.TextName.setText(user.name);
            binding.MobileNumber.setText(user.mobileNumber);
            binding.ProfileImage.setImageBitmap(getUserImage(user.image));
            binding.getRoot().setOnClickListener(v -> userListener.onUserClicked(user));
        }

    }

}
