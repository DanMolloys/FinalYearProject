package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment

class BuiltProposalFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_built_proposal, container, false)

        val button: Button = view.findViewById(R.id.buttonCurrentPro)
        button.setOnClickListener {
            val intent = Intent(activity, StoredProposalsActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
