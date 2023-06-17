package com.unpas.kuliah.ui.dosen

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unpas.kuliah.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class DosenFragment : Fragment() {

    val db by lazy { DosenDatabase(requireContext()) }
    private lateinit var pendidikanAdapter: ArrayAdapter<DosenData.Pendidikan>

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_dosen, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        val refreshButton: FloatingActionButton = root.findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            refreshDosenList()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val dosenList = db.dosenDao().getAllDosens()

            requireActivity().runOnUiThread {
                for (index in dosenList.indices) {
                    val tableRow = TableRow(requireContext())
                    val dosen = dosenList[index]

                    val nidnCell = createTableCell(dosen.nidn)
                    val namaGelarDepanBelakang = "${dosen.gelar_depan} ${dosen.nama} ${dosen.gelar_belakang}"
                    val namaGelarDepanBelakangCell = createTableCell(namaGelarDepanBelakang)
                    val pendidikanCell = createTableCell(dosen.pendidikan)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#59CE8F"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#8BC34A"))
                    }

                    tableRow.addView(nidnCell)
                    tableRow.addView(namaGelarDepanBelakangCell)
                    tableRow.addView(pendidikanCell)

                    // Add edit icon
                    val editIcon = createEditIcon(dosen)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(dosen)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_dosen, container, false)

            val nidnext = bottomSheetView.findViewById<EditText>(R.id.nidnText)
            val namaText = bottomSheetView.findViewById<EditText>(R.id.namaText)
            val gelarDepanText = bottomSheetView.findViewById<EditText>(R.id.gelarDepanText)
            val gelarBelakangText = bottomSheetView.findViewById<EditText>(R.id.gelarBelakangText)
            val pendidikanText = bottomSheetView.findViewById<Spinner>(R.id.pendidikanText)
            val button = bottomSheetView.findViewById<Button>(R.id.dosenButton)

            // Inisialisasi Spinner
            val pendidikanAdapter = ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_item,
                DosenData.Pendidikan.values().map { it.name }
            )

            pendidikanText.adapter = pendidikanAdapter

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val dosenApi = retrofit.create(DosenApi::class.java)

            button.setOnClickListener {
                val nidn = nidnext.text.toString().trim()
                val nama = namaText.text.toString().trim()
                val gelarDepan = gelarDepanText.text.toString().trim()
                val gelarBelakang = gelarBelakangText.text.toString().trim()
                val pendidikan = pendidikanText.selectedItem.toString().trim()

                // Validasi data
                if (nidn.isEmpty() || nama.isEmpty() || gelarDepan.isEmpty() || gelarBelakang.isEmpty()) {
                    requireActivity().runOnUiThread {
                        showToast("Harap isi data terlebih dahulu")
                    }
                    return@setOnClickListener
                }

                val dosenData = DosenData(0, nidn, nama, gelarDepan, gelarBelakang, pendidikan)

                CoroutineScope(Dispatchers.IO).launch {
                    db.dosenDao().insertDosen(dosenData)

                    // Tambahkan data ke endpoint menggunakan Retrofit
                    try {
                        val response = dosenApi.addDosen(dosenData)
                        if (response.isSuccessful) {
                            bottomSheetDialog.dismiss()
                        } else {
                            requireActivity().runOnUiThread {
                                showToast("Gagal menambahkan data ke server")
                            }
                        }
                    } catch (e: Exception) {
                        requireActivity().runOnUiThread {
                            showToast("Gagal menambahkan data ke server: ${e.message}")
                        }
                    }
                }

                bottomSheetDialog.dismiss()
                requireActivity().runOnUiThread {
                    showToast("Data berhasil ditambahkan")
                }
            }

            bottomSheetDialog.setContentView(bottomSheetView)
            bottomSheetDialog.show()
        }

        return root
    }

    private fun refreshDosenList() {
        CoroutineScope(Dispatchers.IO).launch {
            val dosenList = db.dosenDao().getAllDosens()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (index in dosenList.indices) {
                    val tableRow = TableRow(requireContext())
                    val dosen = dosenList[index]

                    val nidnCell = createTableCell(dosen.nidn)
                    val namaGelarDepanBelakang = "${dosen.gelar_depan} ${dosen.nama} ${dosen.gelar_belakang}"
                    val namaGelarDepanBelakangCell = createTableCell(namaGelarDepanBelakang)
                    val pendidikanCell = createTableCell(dosen.pendidikan)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#59CE8F"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#8BC34A"))
                    }

                    tableRow.addView(nidnCell)
                    tableRow.addView(namaGelarDepanBelakangCell)
                    tableRow.addView(pendidikanCell)

                    // Add edit icon
                    val editIcon = createEditIcon(dosen)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(dosen)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }
    }

    private fun createTableCell(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(72, 16, 16, 16)
        return textView
    }

    private fun createEditIcon(dosen: DosenData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editDosen(dosen)
        }
        return imageView
    }

    @SuppressLint("MissingInflatedId")
    private fun editDosen(dosen: DosenData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_dosen, null)

        val nidnext = bottomSheetView.findViewById<EditText>(R.id.nidnText)
        val namaText = bottomSheetView.findViewById<EditText>(R.id.namaText)
        val gelarDepanText = bottomSheetView.findViewById<EditText>(R.id.gelarDepanText)
        val gelarBelakangText = bottomSheetView.findViewById<EditText>(R.id.gelarBelakangText)
        val pendidikanText = bottomSheetView.findViewById<Spinner>(R.id.pendidikanText)
        val button = bottomSheetView.findViewById<Button>(R.id.dosenButton)

        nidnext.setText(dosen.nidn)
        namaText.setText(dosen.nama)
        gelarDepanText.setText(dosen.gelar_depan)
        gelarBelakangText.setText(dosen.gelar_belakang)
        val pendidikanAdapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            DosenData.Pendidikan.values().map { it.name }
        )
        pendidikanText.adapter = pendidikanAdapter
        val pendidikanPosition = pendidikanAdapter.getPosition(dosen.pendidikan.toString())
        pendidikanText.setSelection(pendidikanPosition)

        button.text = "Update"

        button.setOnClickListener {
            val updatedInput1 = nidnext.text.toString().trim()
            val updatedInput2 = namaText.text.toString().trim()
            val updatedInput3 = gelarDepanText.text.toString().trim()
            val updatedInput4 = gelarBelakangText.text.toString().trim()
            val updatedInput5 = pendidikanText.selectedItem.toString().trim()

            // Validasi data
            if (updatedInput1.isEmpty() || updatedInput2.isEmpty() || updatedInput3.isEmpty() || updatedInput4.isEmpty()) {
                requireActivity().runOnUiThread {
                    showToast("Harap isi data terlebih dahulu")
                }
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val updatedDosen = dosen.copy(
                    nidn = updatedInput1,
                    nama = updatedInput2,
                    gelar_depan = updatedInput3,
                    gelar_belakang = updatedInput4,
                    pendidikan = updatedInput5
                )
                db.dosenDao().updateDosen(updatedDosen)
            }

            bottomSheetDialog.dismiss()
            requireActivity().runOnUiThread {
                showToast("Data telah diperbarui")
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(dosen: DosenData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteDosen(dosen)
        }
        return imageView
    }

    private fun deleteDosen(dosen: DosenData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.dosenDao().deleteDosen(dosen)
                }
                dialog.dismiss()
                showToast("Data telah dihapus") // Custom function to show a toast
            }
            .setNegativeButton("Tidak") { dialog, id ->
                dialog.dismiss()
            }

        val alert = dialogBuilder.create()
        alert.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

}