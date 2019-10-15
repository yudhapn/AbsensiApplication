package com.pertamina.absensiapplication.view


import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.pertamina.absensiapplication.R
import com.pertamina.absensiapplication.databinding.FragmentCreatePermitBinding
import com.pertamina.absensiapplication.model.Permit
import com.pertamina.absensiapplication.model.StatusPermit
import com.pertamina.absensiapplication.model.User
import com.pertamina.absensiapplication.util.InputFilterMinMax
import com.pertamina.absensiapplication.viewmodel.PermitViewModel
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.SimpleDateFormat
import java.util.*

class CreatePermitFragment : Fragment(), TextWatcher, View.OnClickListener {
    private lateinit var binding: FragmentCreatePermitBinding
    private val myViewModel: PermitViewModel by viewModel()
    private var cal = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCreatePermitBinding.inflate(inflater, container, false).apply {
            //inisialisasi data untuk dropdown deskripsi
            val user = arguments?.let { CreatePermitFragmentArgs.fromBundle(it).user }
            val descriptionsKeys = resources.getStringArray(R.array.description)
            val descriptionsValues = arrayOf(3, 5, 3, 2, 2, 3, 2, 2, 2, 2, 2, 1, 3, 2, 1)
//            val list = descriptionsKeys.zip(descriptionsValues).toMap()

            descriptionDropdown.addTextChangedListener(this@CreatePermitFragment)
            descriptionDropdown.setOnItemClickListener { _, _, position, _ ->
                // langsung isi kolom durasi, karetena jumlah izin HK sudah tetap
                if (position < descriptionsValues.size) {
                    durasiInput.setText(descriptionsValues[position].toString())
                    durasiInput.isEnabled = false
                    durasiInput.error = null
                } else {
                    durasiInput.isEnabled = true
                    durasiInput.setText("")
                }
            }
            anotherInput.addTextChangedListener(this@CreatePermitFragment)
            fromInput.addTextChangedListener(this@CreatePermitFragment)
            toInput.addTextChangedListener(this@CreatePermitFragment)
            durasiInput.addTextChangedListener(this@CreatePermitFragment)
            dateToInput.addTextChangedListener(this@CreatePermitFragment)
            dateBackInput.addTextChangedListener(this@CreatePermitFragment)
            dateInInput.addTextChangedListener(this@CreatePermitFragment)
            costDropdown.addTextChangedListener(this@CreatePermitFragment)
            driveDropdown.addTextChangedListener(this@CreatePermitFragment)
            descriptionDropdown.setOnClickListener(this@CreatePermitFragment)
            costDropdown.setOnClickListener(this@CreatePermitFragment)
            driveDropdown.setOnClickListener(this@CreatePermitFragment)
            // karna saat di awal kondisi checkbox sij, edittext durasi didisable
            durasiInput.isEnabled = false
            durasiInput.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "100"))
            // kondisi checkbox sij selalu ter-check, sehingga perlu handler untuk checkbox cuti
            cutiCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    durasiInput.filters = arrayOf<InputFilter>(InputFilterMinMax("1", user?.leaveBalance.toString()))
                    durasiInput.isEnabled = true
                    durasiInput.setText("")
                    descriptionDropdownlayout.visibility = View.GONE
                    anotherInputlayout.visibility = View.VISIBLE
                    anotherInput.setText("")
                    dateToInput.setText("")
                    dateBackInput.setText("")
                    dateInInput.setText("")
                    submitButton.isEnabled = false
                } else {
                    durasiInput.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "100"))
                    durasiInput.isEnabled = false
                    durasiInput.setText("")
                    dateToInput.setText("")
                    dateBackInput.setText("")
                    dateInInput.setText("")
                    descriptionDropdownlayout.visibility = View.VISIBLE
                    anotherInputlayout.visibility = View.GONE
                    descriptionDropdown.setText("")
                }
            }
            descriptionDropdown.setAdapter(configureAdapter(descriptionsKeys))
            costDropdown.setAdapter(configureAdapter(resources.getStringArray(R.array.cost)))
            driveDropdown.setAdapter(configureAdapter(resources.getStringArray(R.array.drive)))
            val dateToListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                updateDateInView("to", year, monthOfYear, dayOfMonth)
            }
            val dateBackListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                updateDateInView("back", year, monthOfYear, dayOfMonth)
            }
            val dateInListener = DatePickerDialog.OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                updateDateInView("in", year, monthOfYear, dayOfMonth)
            }
            dateToInput.setOnClickListener { showCalendar(dateToListener) }
            dateBackInput.setOnClickListener { showCalendar(dateBackListener) }
            dateInInput.setOnClickListener { showCalendar(dateInListener) }

            submitButton.setOnClickListener {view->
                MaterialAlertDialogBuilder(context)
                    .setTitle("Apakah anda yakin?")
                    .setMessage("Apakah anda yakin ingin MENGAJUKAN surat izin ini?")
                    .setNegativeButton("Tidak", null)
                    .setPositiveButton("Iya") { _, _ ->
                        user?.let { it1 -> submitPermit(it1) }
                        Snackbar.make(view, "Surat izin telah berhasil dibuat", Snackbar.LENGTH_SHORT).show()
                        val action = CreatePermitFragmentDirections.actionShowDashboard()
                        findNavController().navigate(action)
                    }
                    .show()
            }
        }
        return binding.root
    }

    private fun configureAdapter(stringArray: Array<String>) =
        ArrayAdapter(
            requireContext(),
            R.layout.dropdown_item,
            stringArray
        )

    private fun submitPermit(user: User) {
        with(binding) {
            var title = descriptionDropdown.text.toString()
            if (anotherInputlayout.visibility == View.VISIBLE) {
                title = anotherInput.text.toString()
            }
            val cost = costDropdown.text.toString()
            val drive = driveDropdown.text.toString()
            val from = fromInput.text.toString()
            val to = toInput.text.toString()
            val duration = durasiInput.text.toString().toInt()
            val dateTo = dateToInput.text.toString()
            val dateBack = dateBackInput.text.toString()
            val dateIn = dateInInput.text.toString()

            val typePermit = ArrayList<String>()
            if (pnDnCheckbox.isChecked) {
                typePermit.add(pnDnCheckbox.text.toString())
            }
            if (pnLnCheckbox.isChecked) {
                typePermit.add(pnLnCheckbox.text.toString())
            }
            if (sijCheckbox.isChecked) {
                typePermit.add(sijCheckbox.text.toString())
            }
            if (mutasiCheckbox.isChecked) {
                typePermit.add(mutasiCheckbox.text.toString())
            }
            if (cutiCheckbox.isChecked) {
                typePermit.add(cutiCheckbox.text.toString())
            }
            if (tamuCheckbox.isChecked) {
                typePermit.add(tamuCheckbox.text.toString())
            }

            val status = StatusPermit(
                    negotiate = false,
                    confirmBySenior = false,
                    confirmByOH = false,
                    complete = false,
                    request = true
            )

            if (user.senior.isEmpty()) {
                status.confirmBySenior = true
            }
            if (user.operationHead.isEmpty()) {
                status.confirmByOH = true
                status.complete = true
                status.request = false
            }
            // Get User First

            myViewModel.createPermit(
                Permit(
                    "",
                    "",
                    title,
                    user.employeeNumber,
                    from,
                    to,
                    duration,
                    dateTo,
                    dateBack,
                    dateIn,
                    cost,
                    drive,
                    status,
                    user.userId,
                    user.senior,
                    user.operationHead,
                    user.profileImage,
                    user.name.capitalizeWords().trim(),
                    typePermit
                )
            )
        }
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

    private fun showCalendar(dateListener: DatePickerDialog.OnDateSetListener) {
        clearFocus()
        if (binding.durasiInput.text.isEmpty()) {
            binding.durasiInput.requestFocus()
            binding.durasiInputlayout.error = "Lama cuti tidak boleh kosong"
        } else {
            DatePickerDialog(
                requireContext(),
                dateListener,
                // set DatePickerDialog to point to today's date when it loads up
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun clearFocus() {
        binding.durasiInput.clearFocus()
        binding.fromInput.clearFocus()
        binding.toInput.clearFocus()
        hideKeyboard(binding.durasiInput)
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

    override fun afterTextChanged(s: Editable?) {
        with(binding) {
            if (anotherInputlayout.visibility == View.VISIBLE) {
                setVisibilitySubmitButton(anotherInput.text.isEmpty())
            } else {
                setVisibilitySubmitButton(descriptionDropdown.text.isEmpty())
            }
        }
    }

    private fun setVisibilitySubmitButton(emptyDescription: Boolean) {
        with(binding) {
            submitButton.isEnabled = !(emptyDescription || costDropdown.text.isEmpty()
                    || driveDropdown.text.isEmpty() || fromInput.text.isEmpty() || toInput.text.isEmpty()
                    || durasiInput.text.isEmpty() || dateToInput.text.isEmpty() || dateBackInput.text.isEmpty()
                    || dateInInput.text.isEmpty())
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    override fun onClick(v: View?) {clearFocus()}
    private fun String.capitalizeWords(): String = split(" ").joinToString(" ") { it.capitalize() }
}