package com.adityastudios.buyerseller

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.activity.result.contract.ActivityResultContracts
import com.adityastudios.buyerseller.databinding.ActivityLoginOptionsBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.FirebaseDatabase
import java.lang.Exception

class LoginOptionsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginOptionsBinding
    
    private companion object{
        private const val TAG = "LOGIN_OPTIONS_TAG"
    }

    private lateinit var progressDialog: ProgressDialog
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mGoogleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginOptionsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait...")
        progressDialog.setCanceledOnTouchOutside(false)

        firebaseAuth = FirebaseAuth.getInstance()
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this,gso)
        binding.closeBtn.setOnClickListener {
            onBackPressed()
        }
        binding.loginEmailBtn.setOnClickListener {
            startActivity(Intent(this,LoginEmailActivity::class.java))
        }
        binding.loginGoogleBtn.setOnClickListener {
            googleLogin()
        }
    }
    private fun googleLogin(){
        Log.d(TAG, "googleLogin: ")
        progressDialog.setMessage("Loading...")
        progressDialog.show()
        val googleSignInIntent = mGoogleSignInClient.signInIntent
        googleSignInARL.launch(googleSignInIntent)
    }

    private val googleSignInARL = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()){result->
        Log.d(TAG, "googleSignInARL: ")
        progressDialog.dismiss()
        if(result.resultCode== RESULT_OK){
            val data = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)

            try {
                val account = task.getResult(ApiException::class.java)
                Log.d(TAG, "googleSignInARL: Account ID: ${account.id}")
                firebaseAuthWithGoogleAccount(account.idToken)
            }
            catch (e: Exception){
                Log.e(TAG, "googleSignInARL: ", e)
                Utils.toast(this,"${e.message}")
            }
        }
        else{
            Utils.toast(this,"Cancelled...!")
        }
    }

    private fun firebaseAuthWithGoogleAccount(idToken: String?) {
        Log.d(TAG, "firebaseAuthWithGoogleAccount: idToken: $idToken")

        val credential = GoogleAuthProvider.getCredential(idToken,null)
        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener { authResult->
                if(authResult.additionalUserInfo!!.isNewUser){
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: New User, Account Created")
                    updateUserInfo()
                }
                else{
                    Log.d(TAG, "firebaseAuthWithGoogleAccount: Existing User Logged in...")
                    progressDialog.setMessage("Logging In...")
                    progressDialog.show()
                    startActivity(Intent(this,MainActivity::class.java))
                    finishAffinity()
                }

            }
            .addOnFailureListener{e->
                Log.e(TAG, "firebaseAuthWithGoogleAccount: ", e)
                Utils.toast(this,"${e.message}")
            }
    }
    private fun updateUserInfo(){
        Log.d(TAG, "updateUserInfo: ")
        progressDialog.setMessage("Saving User Info")
        progressDialog.show()
        val timestamp = Utils.getTimeStamp()
        val registeredUserEmail = firebaseAuth.currentUser!!.email
        val registeredUserUid = firebaseAuth.uid
        val name = firebaseAuth.currentUser?.displayName
        val hashMap = HashMap<String,Any?>()
        hashMap["name"] = name
        hashMap["phoneCode"] = ""
        hashMap["phoneNumber"] = ""
        hashMap["profileImageUrl"] = ""
        hashMap["dob"] = ""
        hashMap["userType"] = "Email"
        hashMap["typingTo"] = ""
        hashMap["timestamp"] = timestamp
        hashMap["onlineStatus"] = true
        hashMap["email"] = registeredUserEmail
        hashMap["uid"] = registeredUserUid

        val reference = FirebaseDatabase.getInstance().getReference("Users")
        reference.child(registeredUserUid!!)
            .setValue(hashMap)
            .addOnSuccessListener {
                Log.d(TAG, "updateUserInfo: User Registered...")
                progressDialog.dismiss()

                startActivity(Intent(this,MainActivity::class.java))
                finishAffinity() //to remove this as well as all activities below this
            }
            .addOnFailureListener {e ->
                Log.e(TAG, "updateUserInfo: ", e)
                progressDialog.dismiss()
                Utils.toast(this, "Failed to save User info due to ${e.message}")
            }
    }
}