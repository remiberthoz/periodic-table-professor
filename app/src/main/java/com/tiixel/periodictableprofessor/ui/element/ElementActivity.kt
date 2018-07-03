package com.tiixel.periodictableprofessor.ui.element

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.graphics.Color
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.TypedValue
import android.widget.ImageView
import android.widget.LinearLayout
import com.mikepenz.community_material_typeface_library.CommunityMaterial
import com.mikepenz.google_material_typeface_library.GoogleMaterial
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.IIcon
import com.tiixel.periodictableprofessor.R
import com.tiixel.periodictableprofessor.presentation.base.MviView
import com.tiixel.periodictableprofessor.presentation.element.ElementIntent
import com.tiixel.periodictableprofessor.presentation.element.ElementViewModel
import com.tiixel.periodictableprofessor.presentation.element.ElementViewState
import com.tiixel.periodictableprofessor.presentation.element.model.ElementModel
import dagger.android.AndroidInjection
import io.reactivex.Observable
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.element_activity.*
import kotlinx.android.synthetic.main.view_element_property.view.*
import kotlinx.android.synthetic.main.view_element_property_group.view.*
import ru.noties.markwon.Markwon
import javax.inject.Inject

class ElementActivity : AppCompatActivity(), MviView<ElementIntent, ElementViewState> {

    // Used to manage the data flow lifecycle and avoid memory leak
    private val disposable = CompositeDisposable()

    // View model
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: ElementViewModel

    // Starting intent extra params
    enum class Extra(val key: String) {
        ELEMENT("element")
    }

    //<editor-fold desc="Life cycle methods">
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.element_activity)
        AndroidInjection.inject(this)

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ElementViewModel::class.java)

        // Subscribe to the ViewModel and call render for every emitted state
        disposable.add(viewModel.states().subscribe(this::render))
        // Pass the UI's intents to the ViewModel
        viewModel.processIntents(intents())
    }

    override fun onDestroy() {
        super.onDestroy()
        disposable.dispose()
    }
    //</editor-fold>

    //<editor-fold desc="MVI methods">
    override fun intents(): Observable<ElementIntent> {
        val element = intent.getByteExtra(Extra.ELEMENT.key, 0)
        return Observable.just(ElementIntent.InitialIntent(element))
    }

    override fun render(state: ElementViewState) {
        when {
            state.loadingInProgress -> {
            }
            state.loadingFailed -> {
            }
            state.element == null -> {
            }
            else -> {
                displayProperties(state.element)
            }
        }
    }
    //</editor-fold>

    private fun displayProperties(element: ElementModel) {

        details_name.text = element.name
        details_atomic_number.text = element.atomicNumber.toString()
        details_symbol.text = element.symbol

        // General properties
        val generalGroup = makeGroup("General properties", GoogleMaterial.Icon.gmd_description)
        generalGroup.addProperty(R.string.element_data_point_name_description, element.description)
        generalGroup.addProperty(R.string.element_data_point_name_sources, element.sources)
        generalGroup.addProperty(R.string.element_data_point_name_uses, element.uses)
        generalGroup.addProperty(R.string.element_data_point_name_discovery_year, element.discoveryYear)
        generalGroup.addProperty(R.string.element_data_point_name_discovery_location, element.discoveryLocation)
        generalGroup.addProperty(R.string.element_data_point_name_discoverers, element.discoverers)
        generalGroup.addProperty(R.string.element_data_point_name_name_origin, element.nameOrigin)

        // Physical and chemical
        val physicalGroup = makeGroup("Physical & chemical properties", CommunityMaterial.Icon.cmd_temperature_celsius)
        physicalGroup.addProperty(
            R.string.element_data_point_name_electronic_configuration,
            element.electronicConfiguration
        )
        physicalGroup.addProperty(R.string.element_data_point_name_en_pauling, element.enPauling)
        physicalGroup.addProperty(R.string.element_data_point_name_atomic_weight, element.atomicWeight)
        physicalGroup.addProperty(R.string.element_data_point_name_atomic_radius, element.atomicRadius)
        physicalGroup.addProperty(R.string.element_data_point_name_vdw_radius, element.vdwRadius)

        // Geological properties
        val geologicalGroup = makeGroup("Geological properties", CommunityMaterial.Icon.cmd_earth)
        geologicalGroup.addProperty(R.string.element_data_point_name_abundance_crust, element.abundanceCrust)
        geologicalGroup.addProperty(R.string.element_data_point_name_abundance_sea, element.abundanceSea)

        // Mnemonic
        if (element.mnemonicPhrase != null && element.mnemonicPicture != null) {
            val mnemonicGroup = makeGroup("Mnemonic", CommunityMaterial.Icon.cmd_lightbulb_on)

            val mnemonicPhrase =
                layoutInflater.inflate(R.layout.view_element_property, mnemonicGroup, false) as LinearLayout
            mnemonicPhrase.property_title.text = getString(R.string.element_property_name_mnemonic_phrase)
            Markwon.setMarkdown(mnemonicPhrase.property_value, element.mnemonicPhrase.orEmpty())
            mnemonicGroup.addView(mnemonicPhrase)

            val mnemonicPicture = ImageView(this)
            mnemonicPicture.minimumHeight = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                details_atomic_number.textSize,
                resources.displayMetrics
            ).toInt()
            mnemonicPicture.setImageBitmap(element.mnemonicPicture)
            mnemonicGroup.addView(mnemonicPicture)
        }
    }

    private fun makeGroup(title: String, icon: IIcon): LinearLayout {
        val group =
            layoutInflater.inflate(R.layout.view_element_property_group, element_properties, false) as LinearLayout
        group.element_property_group_icon.icon = IconicsDrawable(this).icon(icon).color(Color.BLACK)
        group.element_property_group_title.text = title
        element_properties.addView(group)
        return group
    }

    private fun LinearLayout.addProperty(id: Int, value: String?) {
        if (value == null) return

        val propertyView = layoutInflater.inflate(R.layout.view_element_property, this, false) as LinearLayout
        propertyView.property_title.text = getString(id)
        propertyView.property_value.text = value
        addView(propertyView)
    }
}