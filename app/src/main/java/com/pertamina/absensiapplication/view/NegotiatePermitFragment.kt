package com.pertamina.absensiapplication.view


import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pertamina.absensiapplication.databinding.FragmentNegotiatePermitBinding
import com.pertamina.absensiapplication.model.Permit
import com.pertamina.absensiapplication.viewmodel.PermitViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class NegotiatePermitFragment : Fragment() {
    private lateinit var binding: FragmentNegotiatePermitBinding
    private val permitViewModel: PermitViewModel by viewModel()
    private var cal = Calendar.getInstance()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentNegotiatePermitBinding.inflate(inflater, container, false).apply {
            var permit = Permit()
            arguments?.let {
                permit = DetailPermitFragmentArgs.fromBundle(it).permit
            }

            permit.let {
                permitArg = it
                val dateToListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    updateDateInView("to", year, monthOfYear, dayOfMonth)
                }
                val dateBackListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    updateDateInView("back", year, monthOfYear, dayOfMonth)
                }
                val dateInListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                    updateDateInView("in", year, monthOfYear, dayOfMonth)
                }
                dateToInput.setOnClickListener { showCalendar(dateToListener, permit.dateTo) }
                dateBackInput.setOnClickListener { showCalendar(dateBackListener, permit.dateBack) }
                dateInInput.setOnClickListener { showCalendar(dateInListener, permit.dateIn) }
            }

            submitButton.setOnClickListener {
                MaterialAlertDialogBuilder(context)
                        .setTitle("Apakah anda yakin?")
                        .setMessage("Apakah anda yakin ingin MENEGOSIASI surat izin ini?")
                        .setNegativeButton("Tidak", null)
                        .setPositiveButton("Iya") { _, _ ->
                            permit.status.negotiate = true
                            permit.leaveDuration = durasiInput.text.toString().toInt()
                            permit.dateTo = dateToInput.text.toString()
                            permit.dateBack = dateBackInput.text.toString()
                            permit.dateIn = dateInInput.text.toString()
                            permitViewModel.updatePermit(permit)
                            val action = NegotiatePermitFragmentDirections.actionShowDetailPermit(permit)
                            findNavController().navigate(action)
                        }
                        .show()
            }
        }
        return binding.root
    }

    private fun updateDateInView(type: String, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val currentDate = Calendar.getInstance()
        currentDate.add(Calendar.DATE, -1)
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthOfYear)
        cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
        val myFormat = "dd-MM-yyyy" // mention the format you need
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        if (cal.before(currentDate)) {
            when (type) {
                "to" -> binding.dateToInputlayout.error = "Tanggal harus saat ini atau setelahnya"
                "back" -> binding.dateBackInputlayout.error = "Tanggal harus saat ini atau setelahnya"
                "in" -> binding.dateInInputlayout.error = "Tanggal harus saat ini atau setelahnya"
            }
        } else {
            binding.dateToInputlayout.error = null
            binding.dateBackInputlayout.error = null
            binding.dateBackInputlayout.error = null
            when (type) {
                "to" -> {
                    val duration = binding.durasiInput.text.toString().toInt()
                    binding.dateToInput.setText(sdf.format(cal.time))
                    cal.add(Calendar.DATE, (duration - 1))
                    binding.dateBackInput.setText(sdf.format(cal.time))
                    cal.add(Calendar.DATE, 1)
                    binding.dateInInput.setText(sdf.format(cal.time))
                }
                "back" -> {
                    binding.dateBackInput.setText(sdf.format(cal.time))
                    cal.add(Calendar.DATE, 1)
                    binding.dateInInput.setText(sdf.format(cal.time))
                }
                "in" -> binding.dateInInput.setText(sdf.format(cal.time))
            }
        }
    }

    private fun showCalendar(dateListener: DatePickerDialog.OnDateSetListener, dateParam: String) {
        val date = dateParam.split("-")
        Log.d("testing", "size: ${date.size}, day: ${date[0]}, month: ${date[1]}, year: ${date[2]}")
//        clearFocus()
        DatePickerDialog(
                requireContext(),
                dateListener,
                // set DatePickerDialog to point to today's date when it loads up
                date[2].toInt(),
                date[1].toInt(),
                date[0].toInt()
        ).show()
    }
}