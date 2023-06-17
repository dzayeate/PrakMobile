package com.unpas.kuliah.ui.matakuliah

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.unpas.kuliah.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MataKuliahFragment : Fragment() {

    val db by lazy { MataKuliahDatabase(requireContext()) }
    private var isPraktikum: Boolean = true

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_matkul, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)
        val horizontalScrollView: HorizontalScrollView = root.findViewById(R.id.horizontalScrollView)

        val refreshButton: FloatingActionButton = root.findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            refreshMatkulList()
        }

        CoroutineScope(Dispatchers.IO).launch {
            val matkulList = db.mataKuliahDao().getAllMataKuliahs()

            requireActivity().runOnUiThread {
                for (index in matkulList.indices) {
                    val tableRow = TableRow(requireContext())
                    val matkul = matkulList[index]

                    val kodeCell = createTableCell(matkul.kode)
                    val namaCell = createTableCell(matkul.nama)
                    val sksCell = createTableCell(matkul.sks.toString())
                    val praktikumCell = createTableCell(matkul.praktikum.toString())
                    val deskripsiCell = createTableCell(matkul.deskripsi)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#59CE8F"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#8BC34A"))
                    }

                    tableRow.addView(kodeCell)
                    tableRow.addView(namaCell)
                    tableRow.addView(sksCell)
                    tableRow.addView(praktikumCell)
                    tableRow.addView(deskripsiCell)

                    // Add edit icon
                    val editIcon = createEditIcon(matkul)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(matkul)
                    tableRow.addView(deleteIcon)

                    tableLayout.addView(tableRow)
                }
            }
        }

        val fab: FloatingActionButton = requireActivity().findViewById(R.id.fab)
        fab.setOnClickListener {
            val bottomSheetDialog = BottomSheetDialog(requireContext())
            val bottomSheetView = inflater.inflate(R.layout.bottom_sheet_matkul, container, false)

            val kodeText = bottomSheetView.findViewById<TextInputEditText>(R.id.kodeText)
            val namaText = bottomSheetView.findViewById<TextInputEditText>(R.id.namaText)
            val sksText = bottomSheetView.findViewById<TextInputEditText>(R.id.sksText)
            val praktikumText = bottomSheetView.findViewById<CheckBox>(R.id.praktikumText)
            val deskripsiText = bottomSheetView.findViewById<TextInputEditText>(R.id.deskripsiText)
            val button = bottomSheetView.findViewById<Button>(R.id.matkulButton)

            val retrofit = Retrofit.Builder()
                .baseUrl("https://ppm-api.gusdya.net/api/")
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val matkulApi = retrofit.create(MataKuliahApi::class.java)

            button.setOnClickListener {
                val kode = kodeText.text.toString()
                val nama = namaText.text.toString()
                val sksText = sksText.text.toString()
                val isPraktikum = praktikumText.isChecked
                val deskripsi = deskripsiText.text.toString()

                // Validasi input kosong
                if (kode.isEmpty() || nama.isEmpty() || sksText.isEmpty() || deskripsi.isEmpty()) {
                    showToast("Harap isi data terlebih dahulu")
                    return@setOnClickListener
                }

                // Validasi input sks harus integer
                val sks: Int
                try {
                    sks = sksText.toInt()
                } catch (e: NumberFormatException) {
                    requireActivity().runOnUiThread {
                        showToast("Data harus berupa angka integer")
                    }
                    return@setOnClickListener
                }

                val matkulData = MataKuliahData(0, kode, nama, sks, isPraktikum, deskripsi)

                CoroutineScope(Dispatchers.IO).launch {
                    db.mataKuliahDao().insertMataKuliah(matkulData)

                    // Tambahkan data ke endpoint menggunakan Retrofit
                    try {
                        val response = matkulApi.addMataKuliah(matkulData)
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

    private fun refreshMatkulList() {
        CoroutineScope(Dispatchers.IO).launch {
            val matkulList = db.mataKuliahDao().getAllMataKuliahs()

            requireActivity().runOnUiThread {
                val tableLayout: TableLayout = requireView().findViewById(R.id.tableLayout)
                val childCount = tableLayout.childCount

                // Remove all views except the header row
                tableLayout.removeViews(1, childCount - 1)

                for (index in matkulList.indices) {
                    val tableRow = TableRow(requireContext())
                    val matkul = matkulList[index]

                    val kodeCell = createTableCell(matkul.kode)
                    val namaCell = createTableCell(matkul.nama)
                    val sksCell = createTableCell(matkul.sks.toString())
                    val praktikumCell = createTableCell(matkul.praktikum.toString())
                    val deskripsiCell = createTableCell(matkul.deskripsi)

                    // Set warna latar belakang
                    if (index % 2 == 0) {
                        tableRow.setBackgroundColor(Color.parseColor("#59CE8F"))
                    } else {
                        tableRow.setBackgroundColor(Color.parseColor("#8BC34A"))
                    }

                    tableRow.addView(kodeCell)
                    tableRow.addView(namaCell)
                    tableRow.addView(sksCell)
                    tableRow.addView(praktikumCell)
                    tableRow.addView(deskripsiCell)

                    // Add edit icon
                    val editIcon = createEditIcon(matkul)
                    tableRow.addView(editIcon)

                    // Add delete icon
                    val deleteIcon = createDeleteIcon(matkul)
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

    private fun createEditIcon(matkul: MataKuliahData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_edit_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            editMatkul(matkul)
        }
        return imageView
    }

    private fun editMatkul(matkul: MataKuliahData) {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        val bottomSheetView = layoutInflater.inflate(R.layout.bottom_sheet_matkul, null)

        val kodeText = bottomSheetView.findViewById<TextInputEditText>(R.id.kodeText)
        val namaText = bottomSheetView.findViewById<TextInputEditText>(R.id.namaText)
        val sksText = bottomSheetView.findViewById<TextInputEditText>(R.id.sksText)
        val praktikumText = bottomSheetView.findViewById<CheckBox>(R.id.praktikumText)
        val deskripsiText = bottomSheetView.findViewById<TextInputEditText>(R.id.deskripsiText)
        val button = bottomSheetView.findViewById<Button>(R.id.matkulButton)

        kodeText.setText(matkul.kode)
        namaText.setText(matkul.nama)
        sksText.setText(matkul.sks.toString())
        praktikumText.isChecked = matkul.praktikum
        deskripsiText.setText(matkul.deskripsi)

        button.text = "Update"

        button.setOnClickListener {
            val updatedInput1 = kodeText.text.toString()
            val updatedInput2 = namaText.text.toString()
            val updatedInput3Text = sksText.text.toString()
            val updatedInput4 = praktikumText.isChecked
            val updatedInput5 = deskripsiText.text.toString()

            // Validasi input kosong
            if (updatedInput1.isEmpty() || updatedInput2.isEmpty() || updatedInput3Text.isEmpty() || updatedInput5.isEmpty()) {
                requireActivity().runOnUiThread {
                    showToast("Harap isi data terlebih dahulu")
                }
                return@setOnClickListener
            }

            // Validasi input sks harus integer
            val updatedInput3: Int
            try {
                updatedInput3 = updatedInput3Text.toInt()
            } catch (e: NumberFormatException) {
                requireActivity().runOnUiThread {
                    showToast("Data harus berupa angka integer")
                }
                return@setOnClickListener
            }

            CoroutineScope(Dispatchers.IO).launch {
                val updatedMatkul = matkul.copy(
                    kode = updatedInput1,
                    nama = updatedInput2,
                    sks = updatedInput3,
                    praktikum = updatedInput4,
                    deskripsi = updatedInput5
                )
                db.mataKuliahDao().updateMataKuliah(updatedMatkul)
            }

            bottomSheetDialog.dismiss()
            requireActivity().runOnUiThread {
                showToast("Data telah diperbarui")
            }
        }

        bottomSheetDialog.setContentView(bottomSheetView)
        bottomSheetDialog.show()
    }

    private fun createDeleteIcon(matkul: MataKuliahData): ImageView {
        val imageView = ImageView(requireContext())
        val layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(16, 16, 16, 16)
        imageView.setImageResource(R.drawable.baseline_delete_24)
        imageView.layoutParams = layoutParams
        imageView.setOnClickListener {
            deleteMatkul(matkul)
        }
        return imageView
    }

    private fun deleteMatkul(matkul: MataKuliahData) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setMessage("Apakah Anda yakin ingin menghapus data ini?")
            .setCancelable(false)
            .setPositiveButton("Ya") { dialog, id ->
                CoroutineScope(Dispatchers.IO).launch {
                    db.mataKuliahDao().deleteMataKuliah(matkul)
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