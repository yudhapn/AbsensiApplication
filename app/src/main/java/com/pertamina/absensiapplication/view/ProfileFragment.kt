package com.pertamina.absensiapplication.view


import android.app.Activity.RESULT_OK
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.webkit.MimeTypeMap
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.firebase.storage.FirebaseStorage
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentProfileBinding
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.fragment_profile.*
import org.koin.androidx.viewmodel.ext.android.viewModel


class ProfileFragment : Fragment() {
    private val userViewModel: UserViewModel by viewModel()
    private lateinit var binding: FragmentProfileBinding
    private lateinit var userObj: User
    private var imageUri: Uri? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false).apply {
            userViewModel.getListNetwork().observe(viewLifecycleOwner, Observer {
                profileRefresh.isRefreshing = it == NetworkState.RUNNING
                profileRefresh.setOnRefreshListener {
                    userViewModel.refreshUser()
                }
                when (it) {
                    NetworkState.SUCCESS -> {
                        rootLayout.visibility = View.VISIBLE
                        emptyListImage.visibility = View.GONE
                        emptyListText.visibility = View.GONE
                        emptyListButton.visibility = View.GONE
                    }
                    NetworkState.FAILED -> {
                        com.bumptech.glide.Glide.with(requireContext()).load(R.drawable.ic_healing_black_24dp)
                            .into(emptyListImage)
                        rootLayout.visibility = View.GONE
                        emptyListText.text = getString(R.string.technical_error)
                        emptyListImage.visibility = View.VISIBLE
                        emptyListText.visibility = View.VISIBLE
                        emptyListButton.visibility = View.VISIBLE
                        emptyListButton.setOnClickListener {
                            userViewModel.refreshUser()
                        }
                    }
                    else -> {
                        rootLayout.visibility = View.GONE
                    }
                }
            })
            userViewModel.getUser().observe(viewLifecycleOwner, Observer {
                userObj = it ?: User()
                user = userObj
                userName.text = userObj.name.trim().capitalizeWords()
                if (!userObj.operator) {
                    separator2.visibility = View.GONE
                    manageUserMenu.visibility = View.GONE
                }
                userViewModel.getUser(it.userId, it.senior, it.operationHead)
                    .observe(viewLifecycleOwner, Observer { list ->
                        valueSenior.text = if (list[1].name.isEmpty()) "-" else list[1].name.capitalizeWords().trim()
                        valueOperationHead.text =
                            if (list[2].name.isEmpty()) "-" else list[2].name.capitalizeWords().trim()
                    })
            })

            profileImage.setOnClickListener {
                CropImage.activity(imageUri)
                    .setOutputCompressQuality(70)
                    .setAspectRatio(2, 3)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .start(requireContext(), this@ProfileFragment)
            }


            manageUserMenu.setOnClickListener {
                val action = ProfileFragmentDirections.actionManageUsers()
                findNavController().navigate(action)
            }
            editPasswordMenu.setOnClickListener {
                val action = ProfileFragmentDirections.actionChangePassword()
                findNavController().navigate(action)
            }
            signOutMenu.setOnClickListener {
                userObj.token = ""
                userViewModel.signout(userObj)
                userViewModel.getNetwork().observe(viewLifecycleOwner, Observer {
                    when (it) {
                        NetworkState.SUCCESS -> {
                            progressbar.visibility = View.GONE
                            val action = ProfileFragmentDirections.actionLogout()
                            findNavController().navigate(action)
                        }
                        NetworkState.FAILED -> progressbar.visibility = View.GONE
                        NetworkState.RUNNING -> progressbar.visibility = View.VISIBLE
                    }
                })
            }
        }
        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == RESULT_OK) {
                imageUri = result.uri
                uploadImage()
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                result.error
            }
        }
    }

    private fun getFileExtension(uri: Uri): String? {
        val cR = activity?.contentResolver
        val mime = MimeTypeMap.getSingleton()
        return mime.getExtensionFromMimeType(cR?.getType(uri))
    }

    private fun uploadImage() {
        val storageRef = FirebaseStorage.getInstance().getReference("users/profileImage")
        progressbar.visibility = View.VISIBLE
        val fileReference = storageRef.child(
            (System.currentTimeMillis()).toString()
                    + "." + imageUri?.let { getFileExtension(it) }
        )
        val uploadTask = imageUri?.let { fileReference.putFile(it) }
        uploadTask?.continueWithTask { task ->
            activity?.window?.setFlags(
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
            if (!task.isSuccessful) {
                //                    throw task.getException()
            }
            fileReference.downloadUrl
        }?.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result
                val miUrlOk = downloadUri!!.toString()
                userObj.profileImage = miUrlOk
                userViewModel.updateUser(userObj)
                progressbar.visibility = View.GONE
                userViewModel.refreshUser()
            } else {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_SHORT).show()
            }
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }?.addOnFailureListener { e ->
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            Toast.makeText(requireContext(), e.message, Toast.LENGTH_SHORT).show()
        }

    }

    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}
