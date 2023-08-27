package com.simsek.instagramclone.view

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.simsek.instagramclone.databinding.ActivityUploadBinding
import java.util.UUID

class UploadActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUploadBinding

    //resultLauncher
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>

    var selectedPicture: Uri? = null
    
    //Firebase işlemleri
    private lateinit var auth: FirebaseAuth
    private lateinit var fireStore:FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUploadBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        registerLauncher()

        //Firebase işlemleri
        auth = Firebase.auth
        fireStore = Firebase.firestore
        storage = Firebase.storage
    }

    fun upload(view: View){

        //yükleyeceğimiz görsellere random id vermek için
        val uuid = UUID.randomUUID()
        val imageName = "$uuid.jpg"

        //nerde olduğumuzu takip etmemizi sağlar referance
        val reference = storage.reference
        //bir alt kademeye geçerken child, bir üst kademeye geçmek için parents kullanıyoruz.
        val imageReference = reference.child("images").child(imageName)

        if (selectedPicture != null){
            imageReference.putFile(selectedPicture!!).addOnSuccessListener {
                //download url'i alıp firestore'a kaydedeceğiz
                val uploadPictureReference = storage.reference.child("images").child(imageName)
                uploadPictureReference.downloadUrl.addOnSuccessListener {
                    val downloadUrl = it.toString()

                    if (auth.currentUser != null){
                        val postMap = hashMapOf<String,Any>()
                        postMap.put("downloadUrl",downloadUrl)
                        postMap.put("userEmail",auth.currentUser!!.email!!)
                        postMap.put("comment",binding.commentText.text.toString())
                        postMap.put("date",Timestamp.now())

                        //Verileri aldık şimdi verileri veritabanına koymaya geldi
                        fireStore.collection("Post").add(postMap).addOnSuccessListener {
                            finish()
                        }.addOnFailureListener {
                            Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
                        }
                    }
                }
            }.addOnFailureListener {
                Toast.makeText(this,it.localizedMessage,Toast.LENGTH_LONG).show()
            }
        }
    }

    fun selectImage(view: View){

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                Snackbar.make(view,"Permission needed for gallery",Snackbar.LENGTH_INDEFINITE).setAction("Giver Permission"){
                    //izin işlemi
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }.show()
            }else{
                //rasyoneli göstermek istemiyorsak
                permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
            }
        }else{
            //izni halihazırda aldıysak
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }

    }

    private fun registerLauncher(){

        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->

            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    selectedPicture = intentFromResult.data
                    selectedPicture?.let {
                        binding.imageView.setImageURI(it)
                    }
                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){result ->
            //result true dönerse izin verilmiş demektir.
            if (result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //İzin verilmemişse çalışır
                Toast.makeText(this,"Permission Needed",Toast.LENGTH_LONG).show()
            }
        }
    }
}