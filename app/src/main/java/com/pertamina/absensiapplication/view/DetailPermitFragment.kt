package com.pertamina.absensiapplication.view


import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
import android.content.Intent.FLAG_GRANT_WRITE_URI_PERMISSION
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.hendrix.pdfmyxml.PdfDocument
import com.hendrix.pdfmyxml.viewRenderer.AbstractViewRenderer
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.api.NetworkState
import com.pertamina.absensiapplication.databinding.FragmentDetailPermitBinding
import com.pertamina.absensiapplication.model.Permit
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.viewmodel.PermitViewModel
import com.pertamina.absensiapplication.viewmodel.UserViewModel
import kotlinx.android.synthetic.main.negotiate_layout.view.*
import kotlinx.android.synthetic.main.organic_sij_layout.view.*
import kotlinx.android.synthetic.main.tkjp_sij_layout.view.*
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class DetailPermitFragment : Fragment(), PdfDocument.Callback {
    private lateinit var binding: FragmentDetailPermitBinding
    private val permitViewModel: PermitViewModel by viewModel()
    private val userViewModel: UserViewModel by viewModel()
    private val auth by inject<FirebaseAuth>()
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailPermitBinding.inflate(inflater, container, false).apply {
            var permit = Permit()
            arguments?.let {
                permit = DetailPermitFragmentArgs.fromBundle(it).permit
            }
            permit.let { permitObj ->

                userViewModel.getListNetwork().observe(viewLifecycleOwner, Observer {
                    profileRefresh.isRefreshing = it == NetworkState.RUNNING
                    when (it) {
                        NetworkState.SUCCESS -> {
                            profileRefresh.isEnabled = false
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
                val isComplete = permitObj.status.complete
                val userId = permitObj.userId
                seeDocumentButton.visibility = View.GONE

                var listUser = listOf<User>()
                userViewModel.getUser(permit.userId, permit.senior, permit.operationHead)
                        .observe(viewLifecycleOwner, Observer { list ->
                            listUser = list
                            valuePositionApplicant.text = list[0].position
                            val seniorLeave = list[1].name.isEmpty()
                            val ohLeave = list[2].name.isEmpty()
                            if (userId.equals(auth.currentUser?.uid, false)) {
                                if (!(permitObj.operationHead.equals(auth.currentUser?.uid, true) || permitObj.senior.equals(auth.currentUser?.uid, true)) || seniorLeave || ohLeave) {
                                    buttonConfirmLayout.visibility = View.GONE
                                    negotiateButton.visibility = View.GONE
                                }
                            }
                        })

                if (isComplete || permit.senior.equals(auth.currentUser?.uid, true) || permit.operationHead.equals(auth.currentUser?.uid, true)) {
                    cancelButton.visibility = View.GONE
                    if (permitObj.status.negotiate) {
                        negotiateButton.isEnabled = false
                        rejectButton.isEnabled = false
                        acceptButton.isEnabled = false
                    }
                }

                permitObj.type.forEach {
                    when (it) {
                        pnDnCheckbox.text -> pnDnCheckbox.isChecked = true
                        pnLnCheckbox.text -> pnLnCheckbox.isChecked = true
                        sijCheckbox.text -> sijCheckbox.isChecked = true
                        mutasiCheckbox.text -> mutasiCheckbox.isChecked = true
                        cutiCheckbox.text -> cutiCheckbox.isChecked = true
                        tamuCheckbox.text -> tamuCheckbox.isChecked = true
                    }
                }

                // Jika userId di permit == userId user yang sedang aktif, tampilkan pesan status
                if (permitObj.userId.equals(auth.currentUser?.uid, true)) {
                    statusCardview.visibility = View.VISIBLE
                    with(permitObj.status) {
                        if (cancel) {
                            setStatusPermit(this@apply, getString(R.string.dibatalkan), "#E74C3C")
                        } else if (request && !negotiate) {
                            //status request
                            if (!confirmBySenior) {
                                //not yet confirm senior
                                setStatusPermit(this@apply, getString(R.string.tunggu_atasan), "#FFFF00")
                            } else if (!confirmByOH) {
                                //not yet confirm OH
                                setStatusPermit(this@apply, getString(R.string.tunggu_oh), "#FFFF00")
                            }
                        } else if (complete) {
                            //status approve or reject
                            if (!confirmBySenior) {
                                //reject by senior
                                setStatusPermit(this@apply, getString(R.string.tolak_atasan), "#E74C3C")
                            } else if (confirmBySenior && !confirmByOH) {
                                //reject by OH
                                setStatusPermit(this@apply, getString(R.string.tolak_oh), "#E74C3C")
                            } else if (confirmBySenior && confirmByOH) {
                                //approve
                                setStatusPermit(this@apply, getString(R.string.setuju_terbit), "#2ECC71")
                                seeDocumentButton.visibility = View.VISIBLE
                                seeDocumentButton.setOnClickListener {
                                    generateDocument(permit, listUser)
                                }
                            }
                        } else if (request && negotiate) {
                            var title = ""
                            if (!confirmBySenior) {
                                //negotiate by senior
                                title = "Surat izin kamu dinegosiasi oleh atasan menjadi:"
                                setStatusPermit(this@apply, getString(R.string.negosiasi_atasan), "#158CC1")
                            } else if (confirmBySenior && !confirmByOH) {
                                //negotiate by senior
                                title = "Surat izin kamu dinegosiasi oleh operation head menjadi:"
                                setStatusPermit(this@apply, getString(R.string.negosiasi_oh), "#158CC1")
                            }
                            // Menampilkan dialog jika terjadi negosiasi oleh atasan atau oh
                            val dialogView = inflater.inflate(R.layout.negotiate_layout, null)
                            dialogView.value_duration.text = permit.leaveDuration.toString()
                            dialogView.value_toDate.text = permit.dateTo
                            dialogView.value_backDate.text = permit.dateBack
                            dialogView.value_inDate.text = permit.dateIn

                            MaterialAlertDialogBuilder(context)
                                    .setView(dialogView)
                                    .setCancelable(true)
                                    .setTitle(title)
                                    .setNegativeButton("Tidak Setuju") { dialog, _ ->
                                        MaterialAlertDialogBuilder(context)
                                                .setTitle("Apakah anda yakin?")
                                                .setMessage("Jika anda tidak setuju, surat izin akan DIBATALKAN")
                                                .setNegativeButton("Tidak") { _, _ ->
                                                    dialog.cancel()
                                                }
                                                .setPositiveButton("Iya") { _, _ ->
                                                    permit.status.negotiate = false
                                                    permit.status.cancel = true
                                                    permit.status.complete = true
                                                    permit.status.request = false
                                                    permitViewModel.updatePermit(permit)
                                                    val action = DetailPermitFragmentDirections.actionShowDashboard()
                                                    findNavController().navigate(action)
                                                    dialog.dismiss()
                                                }.show()
                                    }
                                    .setPositiveButton("Setuju") { dialog2, _ ->
                                        if (!confirmBySenior) {
                                            permit.status.confirmBySenior = true
                                            permit.status.negotiate = false
                                        } else if (confirmBySenior && !confirmByOH) {
                                            //confirm by OH
                                            permit.status.confirmByOH = true
                                            permit.status.request = false
                                            permit.status.negotiate = false
                                            permit.status.complete = true
                                        }
                                        permitViewModel.updatePermit(permit)
                                        val action = DetailPermitFragmentDirections.actionShowDashboard()
                                        findNavController().navigate(action)
                                        dialog2.dismiss()
                                    }.show()
                        }
                    }
                }
            }
            actionClose.setOnClickListener {
                statusCardview.visibility = View.GONE
            }

            negotiateButton.setOnClickListener {
                val action = DetailPermitFragmentDirections.actionNegotiatePermit(permit)
                findNavController().navigate(action)
            }

            cancelButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MEMBATALKAN surat izin ini?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            permit.status.complete = true
                            permit.status.request = false
                            permit.status.cancel = true
                            permit.status.negotiate = false
                            permitViewModel.updatePermit(permit)
                            val action = DetailPermitFragmentDirections.actionShowDashboard()
                            findNavController().navigate(action)
                        }
                        .show()
            }
            rejectButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENOLAK surat izin ini?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            permit.status.complete = true
                            permit.status.request = false
                            permit.status.negotiate = false
                            permitViewModel.updatePermit(permit)
                            val action = DetailPermitFragmentDirections.actionShowDashboard()
                            findNavController().navigate(action)
                        }
                        .show()
            }
            acceptButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENYETUJUI surat izin ini?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            //confirm by senior
                            if (permit.senior.equals(auth.currentUser?.uid, true) && !permit.status.confirmBySenior) {
                                permit.status.confirmBySenior = true
                            } else if (permit.operationHead.equals(auth.currentUser?.uid, true)) {
                                permit.status.confirmByOH = true
                                permit.status.complete = true
                                permit.status.request = false
                                permit.status.negotiate = false
                            }
                            permitViewModel.updatePermit(permit)
                            val action = DetailPermitFragmentDirections.actionShowDashboard()
                            findNavController().navigate(action)
                        }
                        .show()
            }
            permitArg = permit
        }
        return binding.root
    }

    private fun generateDocument(permit: Permit, listUser: List<User>) {
        val letterNumber = "Nomor: ${permit.permitNumber}"
        val myFormat = "ddMMMMyyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        val createdOn = "Malang" + sdf.format(permit.createdOn)
        val nameFile = "SIJ$createdOn"
        val page: AbstractViewRenderer?
        if (listUser[0].organic) {
            page = object : AbstractViewRenderer(requireContext(), R.layout.organic_sij_layout) {
                override fun initView(view: View) {
                    permit.type.forEach {
                        when (it) {
                            view.check.text -> view.check.isChecked = true
                            view.check1.text -> view.check1.isChecked = true
                            view.check2.text -> view.check2.isChecked = true
                            view.check3.text -> view.check3.isChecked = true
                            view.check4.text -> view.check4.isChecked = true
                            view.check5.text -> view.check5.isChecked = true
                        }
                    }
                    view.tv_nosurat.text = letterNumber
                    view.tv_value_nama.text = permit.applicantName
                    view.tv_value_nopekerja.text = permit.employeeNumber
                    view.tv_value_jabatan.text = listUser[0].position
                    view.tv_value_asal.text = permit.from
                    view.tv_value_tujuan.text = permit.to
                    view.tv_value_tanggalmulai.text = permit.dateTo
                    view.tv_value_tanggalkembali.text = permit.dateBack
                    view.tv_value_tanggalmasuk.text = permit.dateIn
                    view.tv_value_berkendaraan.text = permit.drive
                    view.tv_value_biaya.text = permit.cost
                    view.tv_value_keterangan.text = permit.title
                    view.tv_tanggal.text = createdOn
                    view.tv_ybs1.text = listUser[0].position
                    view.tv_ybs2.text = permit.applicantName
                    view.tv_atasanybs1.text = listUser[1].position
                    view.tv_atasanybs2.text = listUser[1].name
                    view.tv_operationhead2.text = listUser[2].name
                }
            }
        } else {
            val berangkat = "Tgl Berangkat: ${permit.dateTo}"
            val kembali = "Tgl Kembali: ${permit.dateBack}"
            val masuk = "Masuk Kembali: ${permit.dateIn}"
            val title = permit.title
            page = object : AbstractViewRenderer(requireContext(), R.layout.tkjp_sij_layout) {
                @SuppressLint("SetTextI18n")
                override fun initView(view: View) {
                    view.tv_nosurattkjp.text = letterNumber
                    view.tv_value_nama_tkjp.text = permit.applicantName
                    view.tv_value_nopegawai_tkjp.text = permit.employeeNumber
                    view.tv_value_bagian_tkjp.text = listUser[0].position
                    view.tv_value_berangkat_tkjp.text = berangkat
                    view.tv_value_kembali_tkjp.text = kembali
                    view.tv_value_masuk_tkjp.text = masuk
                    view.tv_value_title_tkjp.text = title
                    view.tv_value_tujuan_tkjp.text = permit.to
                    view.tv_namapemohon.text = permit.applicantName
                    view.tv_disetujui2.text = listUser[1].position
                    view.tv_namapenyetuju.text = listUser[1].name
                    view.tv_value_hk.text = "HK: ${permit.leaveDuration}"
                    view.tv_value_hkr.text = "HLR: 0"
                    view.tv_value_sisa.text = "Sisa Cuti: ${listUser[0].leaveBalance}"

                }
            }
        }

        page.isReuseBitmap = true
        PdfDocument.Builder(requireContext()).addPage(page).orientation(PdfDocument.A4_MODE.PORTRAIT)
                .progressMessage(R.string.gen_pdf_file).progressTitle(R.string.gen_pdf_file)
                .renderWidth(1500).renderHeight(2115)
                .saveDirectory(requireContext().getExternalFilesDir(null)).filename(nameFile)
                .listener(this@DetailPermitFragment)
                .create()
                .createPdf(requireContext())
        val path = requireContext().getExternalFilesDir(null).absolutePath + "/" + nameFile + ".pdf"
        Log.d("testing", "path: $path")
        val file = File(path)
        val target = Intent(Intent.ACTION_VIEW)
        val apkURI = FileProvider.getUriForFile(requireContext(),
                requireContext().applicationContext
                        .packageName + ".provider", file)
        target.setDataAndType(apkURI, "application/pdf")
        target.flags = FLAG_GRANT_READ_URI_PERMISSION or FLAG_GRANT_WRITE_URI_PERMISSION
        val intent = Intent.createChooser(target, "Open File")
        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Instruct the user to install a PDF reader here, or something
        }

    }

    private fun setStatusPermit(binding: FragmentDetailPermitBinding, message: String, color: String) {
        binding.statusMessage.text = message
        binding.statusCardview.strokeColor = Color.parseColor(color)
        binding.statusCardview.setCardBackgroundColor(Color.parseColor(color))
    }

    override fun onComplete(file: File?) {}
    override fun onError(e: Exception?) {}
}