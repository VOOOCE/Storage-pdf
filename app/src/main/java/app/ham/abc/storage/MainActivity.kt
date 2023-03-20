package app.ham.abc.storage

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.core.view.isVisible
import app.ham.abc.storage.databinding.ActivityMainBinding
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.progress.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val PICK_PDF_REQUEST = 101
    private lateinit var storage : FirebaseStorage
    private lateinit var ref : StorageReference
    private lateinit var myPdf : StorageReference
    private var uri : Uri? = null
    private var thePdf : Uri? = null
    private var picked = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = Firebase.storage
        ref = storage.reference
        myPdf = ref.child("Files/MyPDF")
        binding.pick.setOnClickListener {
            val i = Intent(Intent.ACTION_GET_CONTENT)
            i.type = "application/pdf"
            startActivityForResult(i, PICK_PDF_REQUEST)
        }
        binding.upload.setOnClickListener {
            if (picked){
                val dialog = Dialog(this)
                dialog.setContentView(R.layout.progress)
                dialog.setCancelable(false)
                dialog.window!!.setLayout(1000,500)
                dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                dialog.titleDialog.text = "Uploading"
                dialog.messageDialog.isVisible = false
                dialog.show()
                val inputStream = contentResolver.openInputStream(uri!!)
                val arrayBytes = inputStream!!.readBytes()
                myPdf.putBytes(arrayBytes).addOnSuccessListener{
                    myPdf.downloadUrl.addOnSuccessListener { uri ->
                        dialog.dismiss()
                        Toast.makeText(this,"Uploading Success :)",Toast.LENGTH_LONG).show()
                        thePdf = uri
                    }.addOnFailureListener {
                        dialog.dismiss()

                    }
                }.addOnFailureListener {
                    dialog.dismiss()
                    Toast.makeText(this,"Something Went Wrong ! : $it",Toast.LENGTH_LONG).show()
                }
            }
        }
        binding.download.setOnClickListener {
            if (thePdf != null){
                val intent = Intent(Intent.ACTION_VIEW)
                intent.data = Uri.parse(thePdf.toString())
                startActivity(intent)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            uri = data.data!!
            picked = true
        }
    }
}