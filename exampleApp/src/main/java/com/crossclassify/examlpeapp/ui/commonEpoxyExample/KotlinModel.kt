package com.crossclassify.examlpeapp.ui.commonEpoxyExample

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import com.airbnb.epoxy.EpoxyModel
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

abstract class KotlinModel(
    @LayoutRes private val layoutRes: Int
) : EpoxyModel<View>() {

    private var view: View? = null

    abstract fun bind()

    override fun bind(view: View) {
        this.view = view
        bind()
    }

    override fun unbind(view: View) {
        this.view = null
    }

    override fun getDefaultLayout() = layoutRes

    protected fun <V : View> bind(@IdRes id: Int) = object : ReadOnlyProperty<KotlinModel, V> {
        override fun getValue(thisRef: KotlinModel, property: KProperty<*>): V {
            @Suppress("UNCHECKED_CAST")
            return view?.findViewById(id) as V?
                ?: throw IllegalStateException("View ID $id for '${property.name}' not found.")
        }
    }
}