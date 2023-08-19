package ru.veider.eventsreminder.presentation.ui

import android.content.Context
import android.content.res.TypedArray
import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import android.util.AttributeSet
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import androidx.preference.R


class FontSizeSeekBarPreference : SeekBarPreference {
	constructor(
		context: Context, attrs: AttributeSet?,
		defStyleAttr: Int
	) : super(context, attrs, defStyleAttr, 0) {

	}

	constructor(context: Context, attrs: AttributeSet?)
			: this(context, attrs, R.attr.seekBarPreferenceStyle) {
	}

	constructor(context: Context)
			: this(context, null) {
	}

	override fun updateLabelValue(value: Int) {
		if (mSeekBarValueTextView != null) {
			mSeekBarValueTextView!!.text = value.toString()
			mSeekBarValueTextView!!.textSize = value.toFloat()
		}
	}
}

/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * Preference based on android.preference.SeekBarPreference but uses support preference as a base
 * . It contains a title and a [SeekBar] and an optional SeekBar value [TextView].
 * The actual preference layout is customizable by setting `android:layout` on the
 * preference widget layout or `seekBarPreferenceStyle` attribute.
 *
 *
 * The [SeekBar] within the preference can be defined adjustable or not by setting `adjustable` attribute. If adjustable, the preference will be responsive to DPAD left/right keys.
 * Otherwise, it skips those keys.
 *
 *
 * The [SeekBar] value view can be shown or disabled by setting `showSeekBarValue`
 * attribute to true or false, respectively.
 *
 *
 * Other [SeekBar] specific attributes (e.g. `title, summary, defaultValue, min,
 * max`)
 * can be set directly on the preference widget layout.
 */
open class SeekBarPreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	defStyleAttr: Int = R.attr.seekBarPreferenceStyle,
	defStyleRes: Int = 0
) :
	Preference(context, attrs, defStyleAttr, defStyleRes) {
	val TAG = "SEEKBARPREFERENCE"
	var mSeekBarValue/* synthetic access */ = 0
	var mMin: /* synthetic access */Int = 0
	private var mMax = 100
	private var mSeekBarIncrement = 0
	var mTrackingTouch/* synthetic access */ = false
	var mSeekBar: /* synthetic access */SeekBar? = null
	var mSeekBarValueTextView: TextView? = null
	/**
	 * Gets whether the [SeekBar] should respond to the left/right keys.
	 *
	 * @return Whether the [SeekBar] should respond to the left/right keys
	 */
	/**
	 * Sets whether the [SeekBar] should respond to the left/right keys.
	 *
	 * @param adjustable Whether the [SeekBar] should respond to the left/right keys
	 */
	// Whether the SeekBar should respond to the left/right keys
	var isAdjustable: /* synthetic access */Boolean = true

	// Whether to show the SeekBar value TextView next to the bar
	private var mShowSeekBarValue: Boolean = false
	/**
	 * Gets whether the [SeekBarPreference] should continuously save the [SeekBar] value
	 * while it is being dragged. Note that when the value is true,
	 * [Preference.OnPreferenceChangeListener] will be called continuously as well.
	 *
	 * @return Whether the [SeekBarPreference] should continuously save the [SeekBar]
	 * value while it is being dragged
	 * @see .setUpdatesContinuously
	 */
	/**
	 * Sets whether the [SeekBarPreference] should continuously save the [SeekBar] value
	 * while it is being dragged.
	 *
	 * @param updatesContinuously Whether the [SeekBarPreference] should continuously save
	 * the [SeekBar] value while it is being dragged
	 * @see .getUpdatesContinuously
	 */
	// Whether the SeekBarPreference should continuously save the Seekbar value while it is being
	// dragged.
	var updatesContinuously: /* synthetic access */Boolean = false

	/**
	 * Listener reacting to the [SeekBar] changing value by the user
	 */
	private val mSeekBarChangeListener: OnSeekBarChangeListener = object : OnSeekBarChangeListener {
		override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
			if (fromUser && (updatesContinuously || !mTrackingTouch)) {
				syncValueInternal(seekBar)
			} else {
				// We always want to update the text while the seekbar is being dragged
				updateLabelValue(progress + mMin)
			}
		}

		override fun onStartTrackingTouch(seekBar: SeekBar) {
			mTrackingTouch = true
		}

		override fun onStopTrackingTouch(seekBar: SeekBar) {
			mTrackingTouch = false
			if (seekBar.progress + mMin != mSeekBarValue) {
				syncValueInternal(seekBar)
			}
		}
	}

	/**
	 * Listener reacting to the user pressing DPAD left/right keys if `adjustable` attribute is set to true; it transfers the key presses to the [SeekBar]
	 * to be handled accordingly.
	 */
	private val mSeekBarKeyListener: View.OnKeyListener = object : View.OnKeyListener {
		override fun onKey(v: View, keyCode: Int, event: KeyEvent): Boolean {
			if (event.action != KeyEvent.ACTION_DOWN) {
				return false
			}
			if (!isAdjustable && (keyCode == KeyEvent.KEYCODE_DPAD_LEFT
						|| keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
			) {
				// Right or left keys are pressed when in non-adjustable mode; Skip the keys.
				return false
			}

			// We don't want to propagate the click keys down to the SeekBar view since it will
			// create the ripple effect for the thumb.
			if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER || keyCode == KeyEvent.KEYCODE_ENTER) {
				return false
			}
			if (mSeekBar == null) {
				Log.e(
					"SEEKBARPREFERENCE",
					"SeekBar view is null and hence cannot be adjusted."
				)
				return false
			}
			return mSeekBar!!.onKeyDown(keyCode, event)
		}
	}

	/**
	 * Gets the lower bound set on the [SeekBar].
	 *
	 * @return The lower bound set
	 */
	/**
	 * Sets the lower bound on the [SeekBar].
	 *
	 * @param min The lower bound to set
	 */
	var min: Int
		get() = mMin
		set(min) {
			var min = min
			if (min > mMax) {
				min = mMax
			}
			if (min != mMin) {
				mMin = min
				notifyChanged()
			}
		}
	/**
	 * Returns the amount of increment change via each arrow key click. This value is derived from
	 * user's specified increment value if it's not zero. Otherwise, the default value is picked
	 * from the default mKeyProgressIncrement value in [android.widget.AbsSeekBar].
	 *
	 * @return The amount of increment on the [SeekBar] performed after each user's arrow
	 * key press
	 */
	/**
	 * Sets the increment amount on the [SeekBar] for each arrow key press.
	 *
	 * @param seekBarIncrement The amount to increment or decrement when the user presses an
	 * arrow key.
	 */
	var seekBarIncrement: Int
		get() = mSeekBarIncrement
		set(seekBarIncrement) {
			if (seekBarIncrement != mSeekBarIncrement) {
				mSeekBarIncrement = Math.min(mMax - mMin, Math.abs(seekBarIncrement))
				notifyChanged()
			}
		}
	/**
	 * Gets the upper bound set on the [SeekBar].
	 *
	 * @return The upper bound set
	 */
	/**
	 * Sets the upper bound on the [SeekBar].
	 *
	 * @param max The upper bound to set
	 */
	var max: Int
		get() = mMax
		set(max) {
			var max = max
			if (max < mMin) {
				max = mMin
			}
			if (max != mMax) {
				mMax = max
				notifyChanged()
			}
		}

	/**
	 * Gets whether the current [SeekBar] value is displayed to the user.
	 *
	 * @return Whether the current [SeekBar] value is displayed to the user
	 * @see .setShowSeekBarValue
	 */
	/**
	 * Sets whether the current [SeekBar] value is displayed to the user.
	 *
	 * @param showSeekBarValue Whether the current [SeekBar] value is displayed to the user
	 * @see .getShowSeekBarValue
	 */
	var showSeekBarValue: Boolean
		get() = mShowSeekBarValue
		set(showSeekBarValue) {
			mShowSeekBarValue = showSeekBarValue
			notifyChanged()
		}


	init {
		val a = context.obtainStyledAttributes(
			attrs, R.styleable.SeekBarPreference, defStyleAttr, defStyleRes
		)

		// The ordering of these two statements are important. If we want to set max first, we need
		// to perform the same steps by changing min/max to max/min as following:
		// mMax = a.getInt(...) and setMin(...).
		mMin = a.getInt(R.styleable.SeekBarPreference_min, 0)
		max = a.getInt(R.styleable.SeekBarPreference_android_max, 100)
		seekBarIncrement = a.getInt(R.styleable.SeekBarPreference_seekBarIncrement, 0)
		isAdjustable = a.getBoolean(R.styleable.SeekBarPreference_adjustable, true)
		mShowSeekBarValue = a.getBoolean(R.styleable.SeekBarPreference_showSeekBarValue, false)
		updatesContinuously = a.getBoolean(
			R.styleable.SeekBarPreference_updatesContinuously,
			false
		)
		a.recycle()
	}

	override fun onBindViewHolder(holder: PreferenceViewHolder) {
		super.onBindViewHolder(holder)
		holder.itemView.setOnKeyListener(mSeekBarKeyListener)
		mSeekBar = holder.findViewById(R.id.seekbar) as SeekBar
		mSeekBarValueTextView = holder.findViewById(R.id.seekbar_value) as TextView
		if (mShowSeekBarValue) {
			mSeekBarValueTextView!!.visibility = View.VISIBLE
		} else {
			mSeekBarValueTextView!!.visibility = View.GONE
			mSeekBarValueTextView = null
		}
		if (mSeekBar == null) {
			Log.e(SeekBarPreference.Companion.TAG, "SeekBar view is null in onBindViewHolder.")
			return
		}
		mSeekBar!!.setOnSeekBarChangeListener(mSeekBarChangeListener)
		mSeekBar!!.max = mMax - mMin
		// If the increment is not zero, use that. Otherwise, use the default mKeyProgressIncrement
		// in AbsSeekBar when it's zero. This default increment value is set by AbsSeekBar
		// after calling setMax. That's why it's important to call setKeyProgressIncrement after
		// calling setMax() since setMax() can change the increment value.
		if (mSeekBarIncrement != 0) {
			mSeekBar!!.keyProgressIncrement = mSeekBarIncrement
		} else {
			mSeekBarIncrement = mSeekBar!!.keyProgressIncrement
		}
		mSeekBar!!.progress = mSeekBarValue - mMin
		updateLabelValue(mSeekBarValue)
		mSeekBar!!.isEnabled = isEnabled
	}

	override fun onSetInitialValue(defaultValue: Any?) {
		var defaultValue = defaultValue
		if (defaultValue == null) {
			defaultValue = 0
		}
		value = getPersistedInt((defaultValue as Int?)!!)
	}

	override fun onGetDefaultValue(a: TypedArray, index: Int): Any? {
		return a.getInt(index, 0)
	}

	private fun setValueInternal(seekBarValue: Int, notifyChanged: Boolean) {
		var seekBarValue = seekBarValue
		if (seekBarValue < mMin) {
			seekBarValue = mMin
		}
		if (seekBarValue > mMax) {
			seekBarValue = mMax
		}
		if (seekBarValue != mSeekBarValue) {
			mSeekBarValue = seekBarValue
			updateLabelValue(mSeekBarValue)
			persistInt(seekBarValue)
			if (notifyChanged) {
				notifyChanged()
			}
		}
	}
	/**
	 * Gets the current progress of the [SeekBar].
	 *
	 * @return The current progress of the [SeekBar]
	 */
	/**
	 * Sets the current progress of the [SeekBar].
	 *
	 * @param seekBarValue The current progress of the [SeekBar]
	 */
	var value: Int
		get() = mSeekBarValue
		set(seekBarValue) {
			setValueInternal(seekBarValue, true)
		}

	/**
	 * Persist the [SeekBar]'s SeekBar value if callChangeListener returns true, otherwise
	 * set the [SeekBar]'s value to the stored value.
	 */
	fun  /* synthetic access */syncValueInternal(seekBar: SeekBar) {
		val seekBarValue = mMin + seekBar.progress
		if (seekBarValue != mSeekBarValue) {
			if (callChangeListener(seekBarValue)) {
				setValueInternal(seekBarValue, false)
			} else {
				seekBar.progress = mSeekBarValue - mMin
				updateLabelValue(mSeekBarValue)
			}
		}
	}

	/**
	 * Attempts to update the TextView label that displays the current value.
	 *
	 * @param value the value to display next to the [SeekBar]
	 */
	open fun  /* synthetic access */updateLabelValue(value: Int) {
		if (mSeekBarValueTextView != null) {
			mSeekBarValueTextView!!.text = value.toString()
		}
	}

	override fun onSaveInstanceState(): Parcelable? {
		val superState = super.onSaveInstanceState()
		if (isPersistent) {
			// No need to save instance state since it's persistent
			return superState
		}

		// Save the instance state
		val myState = SeekBarPreference.SavedState(superState)
		myState.mSeekBarValue = mSeekBarValue
		myState.mMin = mMin
		myState.mMax = mMax
		return myState
	}

	override fun onRestoreInstanceState(state: Parcelable?) {
		if (state == null || state.javaClass != SeekBarPreference.SavedState::class.java) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state)
			return
		}

		// Restore the instance state
		val myState = state as SeekBarPreference.SavedState
		super.onRestoreInstanceState(myState.getSuperState())
		mSeekBarValue = myState.mSeekBarValue
		mMin = myState.mMin
		mMax = myState.mMax
		notifyChanged()
	}

	/**
	 * SavedState, a subclass of [BaseSavedState], will store the state of this preference.
	 *
	 *
	 * It is important to always call through to super methods.
	 */
	private class SavedState : BaseSavedState {
		var mSeekBarValue = 0
		var mMin = 0
		var mMax = 0

		internal constructor(source: Parcel) : super(source) {

			// Restore the click counter
			mSeekBarValue = source.readInt()
			mMin = source.readInt()
			mMax = source.readInt()
		}

		internal constructor(superState: Parcelable?) : super(superState) {}

		override fun writeToParcel(dest: Parcel, flags: Int) {
			super.writeToParcel(dest, flags)

			// Save the click counter
			dest.writeInt(mSeekBarValue)
			dest.writeInt(mMin)
			dest.writeInt(mMax)
		}

		override fun describeContents(): Int {
			return 0
		}

		companion object CREATOR : Creator<SavedState> {
			override fun createFromParcel(parcel: Parcel): SavedState {
				return SavedState(parcel)
			}

			override fun newArray(size: Int): Array<SavedState?> {
				return arrayOfNulls(size)
			}
		}
	}

	companion object {
		private const val TAG = "SeekBarPreference"
	}
}