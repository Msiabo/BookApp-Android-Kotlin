package com.example.bookworm.adapters

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookworm.MyApp
import com.example.bookworm.R
import com.example.bookworm.databinding.RowCategoryBinding
import com.example.bookworm.databinding.RowCommentBinding
import com.example.bookworm.models.ModelComment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.ArrayList

class AdapterComment  : RecyclerView.Adapter<AdapterComment.HolderComment> {
    val context: Context

    val commentArrayList: ArrayList<ModelComment>

    private lateinit var mAuth : FirebaseAuth

    private lateinit var binding: RowCommentBinding

    constructor(context: Context, commentArrayList: ArrayList<ModelComment>) {
        this.context = context
        this.commentArrayList = commentArrayList

        mAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderComment {
        binding = RowCommentBinding.inflate(LayoutInflater.from(context),parent,false)
        return  HolderComment(binding.root)
    }

    override fun getItemCount(): Int {
        return commentArrayList.size

    }

    override fun onBindViewHolder(holder: HolderComment, position: Int) {
        //Get data
        val model =commentArrayList[position]
        val id = model.id
        val bookId = model.bookId
        val comment = model.comment
        val uid = model.uid
        val timestamp = model.timestamp

        val date = MyApp.formatTimestamp(timestamp.toLong())

        //Set data
        holder.dateTv.text = date
        holder.commentTv.text = comment

        loadUserDetails(model,holder)

        holder.itemView.setOnClickListener {

            if(mAuth.currentUser != null && mAuth.uid == uid){
                deleteCommentDialog(model,holder)
            }
        }
    }

    private fun deleteCommentDialog(model: ModelComment, holder: AdapterComment.HolderComment) {
        //Alert dialog
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete comment")
            .setMessage("Are you sure you want to delete this comment?")
            .setPositiveButton("DELETE"){d,e->
                val bookId = model.bookId
                val commentId = model.id

                val ref = FirebaseDatabase.getInstance().getReference("Books")
                ref.child(bookId).child("Comments").child(commentId)
                    .removeValue()
                    .addOnSuccessListener {

                        Toast.makeText(context,"Comment Deleted",Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener{e->
                        Toast.makeText(context,"Failed to delete comment due to ${e.message}",Toast.LENGTH_SHORT).show()
                    }

            }
            .setNegativeButton("CANCEL"){d,e->
                d.dismiss()
            }
            .show()

    }

    private fun loadUserDetails(model: ModelComment, holder: AdapterComment.HolderComment) {

        val uid = model.uid
        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(uid)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = "${snapshot.child("name").value}"
                    val profileImg = "${snapshot.child("profileImg").value}"

                    holder.nameTv.text = name
                    try {
                        Glide.with(context)
                            .load(profileImg)
                            .placeholder(R.drawable.baseline_person_24)
                            .into(holder.profileTv)
                    }
                    catch (e:Exception){

                        holder.profileTv.setImageResource(R.drawable.baseline_person_24)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
    }

    inner class HolderComment(itemView : View):RecyclerView.ViewHolder(itemView){
        //Init ui views
        val profileTv : ImageView = binding.profileSiv
        val nameTv : TextView = binding.nameTv
        val commentTv : TextView = binding.commentTv
        val dateTv : TextView = binding.dateTv

    }
}