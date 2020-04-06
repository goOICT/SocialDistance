package ai.kun.opentrace.ui.symptoms

import ai.kun.opentrace.R
import ai.kun.opentrace.databinding.ItemSymptomBinding
import ai.kun.opentrace.ui.notifications.NotificationsViewModel
import android.content.Context
import android.os.Bundle
import android.text.Layout
import android.view.*
import android.widget.CheckBox
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_symptom.view.*

class SymptomsAdapter(private val symptoms: List<SelectableSymptom>, private val listener: SymptomCheckedListener) : RecyclerView.Adapter<SymptomsAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemSymptomBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SelectableSymptom) {
            binding.selectableItem = item
            binding.listener = listener
            binding.executePendingBindings()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemSymptomBinding.inflate(inflater, parent, false)

        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return symptoms.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(symptoms[position])
    }
}

class SymptomCheckerFragment: Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager

    private val viewModel: SymptomCheckerViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_symptom_checker, container, false)

        val symptoms = resources.getStringArray(R.array.symptoms_array)
        viewManager = LinearLayoutManager(activity)
        viewAdapter = SymptomsAdapter(symptoms.map { SelectableSymptom(it)}, viewModel)

        val recyclerView = root.findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.apply {
            layoutManager = viewManager
            adapter = viewAdapter
            addItemDecoration(DividerItemDecoration(activity, LinearLayoutManager.VERTICAL))
            setHasFixedSize(true)
        }

        return root
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        val actionBar = (activity as? AppCompatActivity)?.supportActionBar ?: return

        viewModel.numberOfSymptoms.observe(this, Observer<Int> {
            actionBar.title = "${getString(R.string.title_symptoms)} (${it}/${viewAdapter.itemCount})"
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_symptoms, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
        if (item.itemId == R.id.action_menu_done) {

        }
    }
}