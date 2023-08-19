package ru.veider.eventsreminder.presentation.ui

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.ViewTreeObserver
import androidx.fragment.app.Fragment


tailrec fun Context.findActivity(): Activity {
	if (this is Activity) {
		return this
	} else {
		if (this is ContextWrapper) {
			return this.baseContext.findActivity()
		}
		throw java.lang.IllegalStateException("Context chain has no activity")
	}
}

/**
 * Синтаксический сахар для извлечения parcelable из bundle
 * */
inline fun <reified T : Parcelable> Bundle.parcelable(key: String): T? = when {
	Build.VERSION.SDK_INT >= 33 -> getParcelable(key, T::class.java)
	else -> @Suppress("DEPRECATION") getParcelable(key) as? T
}
/**
 * Синтаксический сахар для извлечения parcelable из intent
 * */
inline fun <reified T : Parcelable> Intent.parcelable(key: String): T? = when {
    Build.VERSION.SDK_INT >= 33 -> getParcelableExtra(key, T::class.java)
    else -> @Suppress("DEPRECATION") getParcelableExtra(key) as? T
}

/**
 * Вызывает переданную в качестве параметра функцию единожды
 * после полной отрисовки разметки,
 * удаляет коллбэк после вызова функции
 * @param func2Call функция, вызываемая единожды после отрисовки разметки
 * */
fun Fragment.callAfterRedrawViewTree(func2Call: () -> Unit) {
	val vto = requireView().viewTreeObserver
	if (vto.isAlive) {
		vto.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
			override fun onGlobalLayout() {
				try {
					func2Call()
					val vto2 = requireView().viewTreeObserver
					if (vto2.isAlive) {
						vto2.removeOnGlobalLayoutListener(this)
					}
				} catch (t: Throwable) {
					Log.e(this::class.java.toString(), "", t)
				}
			}
		})
	}
}

