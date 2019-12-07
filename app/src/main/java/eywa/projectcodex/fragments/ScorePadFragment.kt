package eywa.projectcodex.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import eywa.projectcodex.R
import eywa.projectcodex.ScoreListAdapter
import eywa.projectcodex.database.ScoresViewModel

class ScorePadFragment : Fragment() {

    private lateinit var scoresViewModel: ScoresViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.activity_view_scores, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerview)
        val adapter = ScoreListAdapter(context)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        scoresViewModel = ViewModelProvider(this).get(ScoresViewModel::class.java)
        scoresViewModel.allArrows.observe(
                viewLifecycleOwner,
                Observer { arrows -> arrows?.let { adapter.setArrows(arrows) } })

    }
}
