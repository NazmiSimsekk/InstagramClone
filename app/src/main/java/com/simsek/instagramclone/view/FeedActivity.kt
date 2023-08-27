package com.simsek.instagramclone.view

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.simsek.instagramclone.R
import com.simsek.instagramclone.adapter.FeedRecyclerAdapter
import com.simsek.instagramclone.databinding.ActivityFeedBinding
import com.simsek.instagramclone.model.Post

class FeedActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFeedBinding

    //Çıkış işlemi yapabilmek için
    private lateinit var auth: FirebaseAuth
    //Verileri çekmek için
    private lateinit var db: FirebaseFirestore

    private lateinit var postArrayList: ArrayList<Post>

    //Adapter bağlama
    private lateinit var feedAdapter: FeedRecyclerAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFeedBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        auth = Firebase.auth

        //Verileri çekmek için
        db = Firebase.firestore

        postArrayList = ArrayList<Post>()

        getData()

        binding.recyclerView.layoutManager = LinearLayoutManager(this)

        feedAdapter = FeedRecyclerAdapter(postArrayList)
        binding.recyclerView.adapter = feedAdapter
    }

    private fun getData(){

        //value bize dökümanlarımızı veriyor, error ise hata varsa onu veriyor
        //döküman dediğimiz oluşturduğumuz her bir gönderi
        //where ile filtreleme işlemleri yapılabilir.
        //where yerine orderBy'da kullanılabilir. Sıralamak, dizmek anlamına gelir
        //Bu işlem ile en yeni gönderiler ilk başta görülür.
        db.collection("Post").orderBy("date",Query.Direction.DESCENDING).addSnapshotListener { value, error ->

            if (error != null){
                Toast.makeText(this,error.localizedMessage,Toast.LENGTH_LONG).show()
            }else{

                if (value != null){
                    //value değeri boş dönebilir, value boş değilse manasına gelir
                    if (!value.isEmpty){
                        //Liste halinde dökümanları veriyor.
                        val documents = value.documents

                        //Görsellerin tekrar tekrar gösterilmemesi için
                        //Dizi, arrayList boş bir şekilde başlar
                        postArrayList.clear()

                        for (document in documents){
                            //hashMap içerisinde değişken türünü Any yaptığımız için Any döndürecek
                            //casting işlemi yaparak gelen verinin Stringe döndürülebilir olduğunu belirtiyoruz
                            val comment = document.get("comment") as String
                            val userEmail = document.get("userEmail") as String
                            val downloadUrl = document.get("downloadUrl") as String

                            println(comment)

                            val post = Post(userEmail,comment,downloadUrl)
                            postArrayList.add(post)
                        }
                        //canlı güncellemeler olacağı için
                        //veriler yenilendi kendine bi çeki düzen ver kardeşim demeye getiriyor
                        feedAdapter.notifyDataSetChanged()
                    }
                }

            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {

        val menuInflater = menuInflater
        menuInflater.inflate(R.menu.insta_menu,menu)

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if(item.itemId == R.id.add_post){
            val intent = Intent(this, UploadActivity::class.java)
            startActivity(intent)
        }else if (item.itemId == R.id.signout){

            //çıkış yapmak için
            auth.signOut()

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        return super.onOptionsItemSelected(item)
    }
}
