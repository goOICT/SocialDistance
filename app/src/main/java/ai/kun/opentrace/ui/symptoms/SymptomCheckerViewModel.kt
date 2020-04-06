package ai.kun.opentrace.ui.symptoms

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel

// could be generic, but no idea how to use generics in data binding
class SelectableSymptom (val item: String, var isSelected: Boolean = false) {

    fun toggle() {
        this.isSelected = !this.isSelected
    }
}

interface SymptomCheckedListener {
    fun onSymptomChecked(symptom: SelectableSymptom)
}

class SymptomCheckerViewModel: ViewModel(), SymptomCheckedListener {

    private val selectedSymptoms = MutableLiveData<Set<String>>(HashSet())
    val numberOfSymptoms = Transformations.map(selectedSymptoms) {
        it.size
    }

    override fun onSymptomChecked(symptom: SelectableSymptom) {
        val currentSymptoms = selectedSymptoms.value ?: HashSet()
        symptom.toggle()
        if (symptom.isSelected) {
            selectedSymptoms.postValue(currentSymptoms.plus(symptom.item))
        } else {
            selectedSymptoms.postValue(currentSymptoms.minus(symptom.item))
        }
    }

}