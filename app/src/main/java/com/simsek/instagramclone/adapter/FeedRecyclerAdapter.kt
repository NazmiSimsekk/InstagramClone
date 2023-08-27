package com.simsek.instagramclone.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.simsek.instagramclone.databinding.ActivityUploadBinding
import com.simsek.instagramclone.databinding.RecyclerRowBinding
import com.simsek.instagramclone.model.Post
import com.squareup.picasso.Picasso

class FeedRecyclerAdapter(val postList: ArrayList<Post>) : RecyclerView.Adapter<FeedRecyclerAdapter.PostHolder>() {

    class PostHolder(val recyclerRowBinding: RecyclerRowBinding) : RecyclerView.ViewHolder(recyclerRowBinding.root){}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {

        val recyclerRowBinding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return PostHolder(recyclerRowBinding)
    }

    override fun getItemCount(): Int {
        return postList.size

    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.recyclerRowBinding.recyclerEmailText.text = postList.get(position).email
        holder.recyclerRowBinding.recyclerCommentText.text = postList.get(position).comment
        //Picasso kütüphanesi ile görselleri recyclerView'da gösterebiliyoruz
        Picasso.get().load(postList.get(position).downloadUrl).into(holder.recyclerRowBinding.recyclerImageView)
    }

}