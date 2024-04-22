package com.example.finalyearprojectdm

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.finalyearprojectdm.ui.BuilderActivity

class BuildProposalFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_build_proposal, container, false)

        val button: Button = view.findViewById(R.id.buttonBuildNew)
        button.setOnClickListener {
            val intent = Intent(activity, BuilderActivity::class.java)
            startActivity(intent)
        }

        return view
    }
}
