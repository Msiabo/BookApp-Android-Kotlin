package com.example.bookworm.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.example.bookworm.BooksUserFragment
import com.example.bookworm.databinding.ActivityUserDashboardBinding
import com.example.bookworm.models.ModelCategory
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class UserDashboardActivity : AppCompatActivity() {
    private lateinit var binding : ActivityUserDashboardBinding

    private lateinit var mAuth : FirebaseAuth

    private lateinit var categoryArrayList : ArrayList<ModelCategory>

    private lateinit var viewPagerAdapter : ViewPagerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUserDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //Initialise mAuth
        mAuth = FirebaseAuth.getInstance()

        setupWithViewPagerAdapter(binding.viewPager)
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        //Handle logout button click
        binding.logoutBtn.setOnClickListener {
            mAuth.signOut()
            checkUser()
            val myIntent = Intent(this, LoginActivity::class.java)
            startActivity(myIntent)
            finish()

        }
        //Handle click open profile button
        binding.openProfileButton.setOnClickListener {
            val intent = Intent(this,ProfileActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
    private fun setupWithViewPagerAdapter(viewPager : ViewPager){

        viewPagerAdapter = ViewPagerAdapter(
            supportFragmentManager ,
            FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT ,
            this)

        categoryArrayList = ArrayList()

        val ref =FirebaseDatabase.getInstance().getReference("Categories")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                //Clear list before adding data to it
                categoryArrayList.clear()

                val modelAll = ModelCategory("01" ,"All",1,"")
                val modelMostViewed = ModelCategory("01" ,"Most Viewed",1,"")
                val modelDownloaded = ModelCategory("01" ,"Most Downloaded",1,"")

                //Add to list
                categoryArrayList.add(modelAll)
                categoryArrayList.add(modelMostViewed)
                categoryArrayList.add(modelDownloaded)

                //Add to viewPagerAdapter
                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelAll.id}",
                        "${modelAll.category}",
                        "${modelAll.uid}"
                    ),modelAll.category
                )

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelMostViewed.id}",
                        "${modelMostViewed.category}",
                        "${modelMostViewed.uid}"
                    ),modelMostViewed.category
                )

                viewPagerAdapter.addFragment(
                    BooksUserFragment.newInstance(
                        "${modelDownloaded.id}",
                        "${modelDownloaded.category}",
                        "${modelDownloaded.uid}"
                    ),modelDownloaded.category
                )
                //Refresh list
                viewPagerAdapter.notifyDataSetChanged()

                //Load from firebase db
                for (ds in snapshot.children) {
                    val model = ds.getValue(ModelCategory::class.java)

                    //Add to list
                    categoryArrayList.add(model!!)

                    viewPagerAdapter.addFragment(
                        BooksUserFragment.newInstance(
                            "${model.id}",
                            "${model.category}",
                            "${model.uid}"
                        ), model.category
                    )
                    //Refresh list
                    viewPagerAdapter.notifyDataSetChanged()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    class ViewPagerAdapter(fm : FragmentManager ,behavior: Int ,context : Context) : FragmentPagerAdapter(fm,behavior){
        //Holds list of fragments
        private val fragmentsList : ArrayList<BooksUserFragment> = ArrayList()

        private val fragmentTitleList: ArrayList<String> = ArrayList()

        private val context : Context

        init {
            this.context = context
        }

        override fun getCount(): Int {
            return  fragmentsList.size
        }

        override fun getItem(position: Int): Fragment {
            return fragmentsList[position]
        }

        override fun getPageTitle(position: Int): CharSequence {
            return fragmentTitleList[position]
        }
        public fun addFragment(fragment: BooksUserFragment, title : String){

            fragmentsList.add(fragment)

            fragmentTitleList.add(title)
        }
    }

    private fun checkUser() {
        //Get current user
        val firebaseUser = mAuth.currentUser
        if (firebaseUser == null) {

            //Not logged ,user can stay in dashboard even when not logged in
            binding.userSubtitleTv.text = "Not logged in"

            binding.logoutBtn.visibility = View.GONE
            binding.openProfileButton.visibility = View.GONE
        } else {
            //Logged in,show user email
            val email = firebaseUser.email
            binding.userSubtitleTv.text = email

            binding.logoutBtn.visibility = View.VISIBLE
            binding.openProfileButton.visibility = View.VISIBLE
        }
    }
}