package com.unpas.kuliah.ui.anggota

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.unpas.kuliah.R

class AnggotaFragment : Fragment() {

    data class Person(val npm: String, val nama: String)

    private val peopleList: List<Person> = listOf(
        Person("203040012", "Ibnu Rusdianto"),
        Person("203040003", "M. Faishal Thariqulhaq"),
        Person("203040042", "Ikhsan Rachmat"),
        Person("203040098", "Agam Ramdhan Kamil Atmaja"),
        Person("203040019", "Ikhsan Ardiansyah"),
        Person("203040010", "Naufal Rafif")
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val root = inflater.inflate(R.layout.fragment_anggota, container, false)
        val tableLayout: TableLayout = root.findViewById(R.id.tableLayout)

        for (person in peopleList) {
            val tableRow = TableRow(requireContext())

            val npmCell = createTableCell(person.npm)
            val namaCell = createTableCell(person.nama)

            tableRow.addView(npmCell)
            tableRow.addView(namaCell)

            tableLayout.addView(tableRow)
        }

        return root
    }

    override fun onResume() {
        super.onResume()

        // Menyembunyikan tombol appBarMain
        (requireActivity() as AppCompatActivity).findViewById<FloatingActionButton>(R.id.fab).hide()
    }

    override fun onPause() {
        super.onPause()

        // Menampilkan kembali tombol appBarMain
        (requireActivity() as AppCompatActivity).findViewById<FloatingActionButton>(R.id.fab).show()
    }

    private fun createTableCell(text: String): TextView {
        val textView = TextView(requireContext())
        textView.text = text
        textView.setPadding(65, 16, 16, 16)
        textView.layoutParams = TableRow.LayoutParams(
            0,
            TableRow.LayoutParams.WRAP_CONTENT,
            1f
        )
        return textView
    }

}