package ru.veider.eventsreminder.presentation.ui.settings

import android.content.Context
import android.content.res.ColorStateList
import android.util.AttributeSet
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.ColorInt
import androidx.core.view.isVisible
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.card.MaterialCardView
import ru.veider.eventsreminder.R
import ru.veider.eventsreminder.domain.SettingsData


class WidgetPreviewPreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet,
	defStyleAttr: Int = 0
) : Preference(context, attrs, defStyleAttr) {
	lateinit var settings: SettingsData

	fun applySettings(settingsData: SettingsData) {
		settings = settingsData
	}

	fun renew() {
		super.notifyChanged()
	}

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		try {
			with(holder) {
				setWidgetBackgroundColor()
				setWidgetItemsBackgroundColor()
				setFirstPreviewItemAppearance()
				setSecondPreviewItemAppearance()
				setThirdPreviewItemAppearance()
				setForthPreviewItemAppearance()
				setFifthPreviewItemAppearance()
			}
		} catch (t: Throwable) {
			try {
				Log.e(this::class.java.toString(), "", t)
				Toast.makeText(
					context,
					t.toString(),
					Toast.LENGTH_LONG
				).show()
			} catch (_: Throwable) {
			}
		}
	}

	private fun PreferenceViewHolder.setTextViewsStyle(itemList: Array<Int>, textSize: Int, @ColorInt textColor: Int) {
		itemList.forEach {
			(findViewById(it) as TextView).run {
				this.textSize = textSize.toFloat()
				this.setTextColor(textColor)
			}
		}
	}
	private fun PreferenceViewHolder.setTextViewVisibility(item:Int, isVisible:Boolean){
		(findViewById(item) as TextView).isVisible = isVisible
	}
	private fun PreferenceViewHolder.setFirstPreviewItemAppearance() {
		setTextViewsStyle(
			arrayOf(R.id.firstPreviewItemDaysTo, R.id.firstPreviewItemDate, R.id.firstPreviewItemTitle),
			settings.widgetFontSize,
			settings.widgetFontColorHoliday
		)
		setTextViewVisibility(R.id.firstPreviewItemDate, settings.showDateEvent)
	}
	private fun PreferenceViewHolder.setSecondPreviewItemAppearance() {
		setTextViewsStyle(
			arrayOf(R.id.secondPreviewItemDaysTo, R.id.secondPreviewItemDate, R.id.secondPreviewItemTitle, R.id.secondPreviewItemAge),
			settings.widgetFontSize,
			settings.widgetFontColorBirthday
		)
		setTextViewVisibility(R.id.secondPreviewItemDate, settings.showDateEvent)
		setTextViewVisibility(R.id.secondPreviewItemAge, settings.showAge)
	}
	private fun PreferenceViewHolder.setThirdPreviewItemAppearance() {
		setTextViewsStyle(
			arrayOf(R.id.thirdPreviewItemDaysTo, R.id.thirdPreviewItemDate, R.id.thirdPreviewItemTitle, R.id.thirdPreviewItemAge),
			settings.widgetFontSize,
			settings.widgetFontColorBirthday
		)
		setTextViewVisibility(R.id.thirdPreviewItemDate, settings.showDateEvent)
		setTextViewVisibility(R.id.thirdPreviewItemAge, settings.showAge)
	}
	private fun PreferenceViewHolder.setForthPreviewItemAppearance() {
		setTextViewsStyle(
			arrayOf(R.id.forthPreviewItemDaysTo, R.id.forthPreviewItemDate, R.id.forthPreviewItemTitle, R.id.forthPreviewItemTime),
			settings.widgetFontSize,
			settings.widgetFontColorSimpleEvent
		)
		setTextViewVisibility(R.id.forthPreviewItemDate, settings.showDateEvent)
		setTextViewVisibility(R.id.forthPreviewItemTime, settings.showTimeEvent)
	}
	private fun PreferenceViewHolder.setFifthPreviewItemAppearance() {
		setTextViewsStyle(
			arrayOf(R.id.fifthPreviewItemDaysTo, R.id.fifthPreviewItemDate, R.id.fifthPreviewItemTitle),
			settings.widgetFontSize,
			settings.widgetFontColorHoliday
		)
		setTextViewVisibility(R.id.fifthPreviewItemDate, settings.showDateEvent)
	}

	private fun PreferenceViewHolder.setWidgetBackgroundColor(){
		(findViewById(R.id.background) as? MaterialCardView)?.run{
			setCardBackgroundColor(ColorStateList.valueOf(settings.widgetBackgroundColor))
			radius = settings.widgetBorderCornerRadius.toFloat()
			setStrokeColor(ColorStateList.valueOf(settings.widgetBorderColor))
			strokeWidth = settings.widgetBorderWidth
		}

	}

	private fun PreferenceViewHolder.setWidgetItemsBackgroundColor() {
		findViewById(R.id.firstItemPreviewLinearLayout)?.setBackgroundColor(settings.widgetLine1Color)
		findViewById(R.id.secondItemPreviewLinearLayout)?.setBackgroundColor(settings.widgetLine2Color)
		findViewById(R.id.thirdItemPreviewLinearLayout)?.setBackgroundColor(settings.widgetLine1Color)
		findViewById(R.id.forthItemPreviewLinearLayout)?.setBackgroundColor(settings.widgetLine2Color)
		findViewById(R.id.fifthItemPreviewLinearLayout)?.setBackgroundColor(settings.widgetLine1Color)
	}
}