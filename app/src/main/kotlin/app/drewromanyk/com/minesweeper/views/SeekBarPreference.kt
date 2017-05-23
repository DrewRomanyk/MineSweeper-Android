package app.drewromanyk.com.minesweeper.views

import android.content.Context
import android.content.res.TypedArray
import android.preference.Preference
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.TextView

import app.drewromanyk.com.minesweeper.R

/**
 * SeekBar Preference
 * Class used for discrete seek bar values
 * Created by Drew on 12/15/2014.
 */

class SeekBarPreference : Preference, OnSeekBarChangeListener {
    companion object {

        private val ANDROID_NS = "http://schemas.android.com/apk/res/android"
        private val APPLICATION_NS = "http://robobunny.com"
        private val DEFAULT_VALUE = 50
        private var mRowValue: Int = 0
        private var mColumnValue: Int = 0
        // Need access to this as if rowSeek or colSeek are used, then it will impact mineSeek as well.
        private var mineSeek: SeekBar? = null
    }

    private val TAG = javaClass.name

    private var mMaxValue = 100
    private var mMinValue = 0
    private var mInterval = 1
    private var mCurrentValue: Int = 0
    private var mUnitsLeft = ""
    private var mUnitsRight = ""
    private var mSeekBar: SeekBar? = null
    private var mStatusText: TextView? = null

    private var isRowSeek: Boolean = false
    private var isColumnSeek: Boolean = false

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        initPreference(context, attrs)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle) {
        initPreference(context, attrs)
    }

    private fun initPreference(context: Context, attrs: AttributeSet) {
        setValuesFromXml(attrs)
        mSeekBar = SeekBar(context, attrs)
        mSeekBar!!.max = mMaxValue - mMinValue
        mSeekBar!!.setOnSeekBarChangeListener(this)

        widgetLayoutResource = R.layout.seek_bar_preference
    }

    private fun setValuesFromXml(attrs: AttributeSet) {
        mMaxValue = attrs.getAttributeIntValue(ANDROID_NS, "max", 100)
        mMinValue = attrs.getAttributeIntValue(APPLICATION_NS, "min", 0)

        mUnitsLeft = attrs.getAttributeStringValue(APPLICATION_NS, "unitsLeft", "")
        val units = attrs.getAttributeStringValue(APPLICATION_NS, "units", "")
        mUnitsRight = attrs.getAttributeStringValue(APPLICATION_NS, "unitsRight", units)

        try {
            val newInterval = attrs.getAttributeValue(APPLICATION_NS, "interval")
            if (newInterval != null)
                mInterval = Integer.parseInt(newInterval)
        } catch (e: Exception) {
            Log.e(TAG, "Invalid interval value", e)
        }

    }

    fun AttributeSet.getAttributeStringValue(namespace: String, name: String, defaultValue: String): String {
        var value: String? = getAttributeValue(namespace, name)
        if (value == null)
            value = defaultValue

        return value
    }

    /*@Override
    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);

        // The basic preference layout puts the widget frame to the right of the title and summary,
        // so we need to change it a bit - the seekbar should be under them.
        LinearLayout layout = (LinearLayout) view;
        layout.setOrientation(LinearLayout.VERTICAL);

        return view;
    }*/

    public override fun onBindView(view: View?) {
        super.onBindView(view)

        try {
            // move our seekbar to the new view we've been given
            val oldContainer = mSeekBar!!.parent
            val newContainer = view!!.findViewById(R.id.seekBarPrefBarContainer) as ViewGroup

            if (oldContainer !== newContainer) {
                // remove the seekbar from the old view
                if (oldContainer != null) {
                    (oldContainer as ViewGroup).removeView(mSeekBar)
                }
                // remove the existing seekbar (there may not be one) and add ours
                newContainer.removeAllViews()
                newContainer.addView(mSeekBar, ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "Error binding view: " + ex.toString())
        }

        //if dependency is false from the beginning, disable the seek bar
        if (!(view?.isEnabled as Boolean)) {
            mSeekBar!!.isEnabled = false
        }

        updateView(view)
    }

    /**
     * Update a SeekBarPreference view with our current state

     * @param view Status text of seek bar
     */
    private fun updateView(view: View?) {

        try {
            mStatusText = view!!.findViewById(R.id.seekBarPrefValue) as TextView

            mStatusText!!.text = (mUnitsLeft + mCurrentValue + mUnitsRight)
            mStatusText!!.minimumWidth = 30

            mSeekBar!!.progress = mCurrentValue - mMinValue
            /*
            TextView unitsRight = (TextView)view.findViewById(R.id.seekBarPrefUnitsRight);
            unitsRight.setText(mUnitsRight);

            TextView unitsLeft = (TextView)view.findViewById(R.id.seekBarPrefUnitsLeft);
            unitsLeft.setText(mUnitsLeft);
            */

        } catch (e: Exception) {
            Log.e(TAG, "Error updating seek bar preference", e)
        }

    }

    override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
        var newValue = progress + mMinValue

        if (newValue > mMaxValue)
            newValue = mMaxValue
        else if (newValue < mMinValue)
            newValue = mMinValue
        else if ((mInterval != 1) and (newValue % mInterval != 0))
            newValue = Math.round(newValue.toFloat() / mInterval) * mInterval

        // change rejected, revert to the previous value
        if (!callChangeListener(newValue)) {
            seekBar.progress = mCurrentValue - mMinValue
            return
        }

        // change accepted, store it
        updateCurrentValue(newValue)
        //mStatusText.setText(String.valueOf(newValue));
        mStatusText!!.text = (mUnitsLeft + newValue + mUnitsRight)
        persistInt(newValue)

    }

    override fun onStartTrackingTouch(seekBar: SeekBar) {}

    override fun onStopTrackingTouch(seekBar: SeekBar) {
        notifyChanged()
    }


    override fun onGetDefaultValue(ta: TypedArray, index: Int): Any {
        return ta.getInt(index, DEFAULT_VALUE)
    }

    override fun onSetInitialValue(restoreValue: Boolean, defaultValue: Any?) {

        if (restoreValue) {
            updateCurrentValue(getPersistedInt(mCurrentValue))
        } else {
            var temp = 0
            try {
                temp = defaultValue as Int
            } catch (ex: Exception) {
                Log.e(TAG, "Invalid default value: " + defaultValue.toString())
            }

            persistInt(temp)
            updateCurrentValue(temp)
        }

    }

    /**
     * make sure that the seekbar is disabled if the preference is disabled
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        mSeekBar!!.isEnabled = enabled
    }

    override fun onDependencyChanged(dependency: Preference, disableDependent: Boolean) {
        super.onDependencyChanged(dependency, disableDependent)

        //Disable movement of seek bar when dependency is false
        if (mSeekBar != null) {
            mSeekBar!!.isEnabled = !disableDependent
        }
    }

    private fun updateCurrentValue(newValue: Int) {
        mCurrentValue = newValue
        if (isRowSeek) {
            mRowValue = mCurrentValue
            mineSeek!!.max = (mRowValue + mColumnValue - 18) * 8 + 54
        }
        if (isColumnSeek) {
            mColumnValue = mCurrentValue
            mineSeek!!.max = (mRowValue + mColumnValue - 18) * 8 + 54
        }
        if (mineSeek === mSeekBar) {
            mMaxValue = mSeekBar!!.max + mMinValue
        }
    }

    fun setMineSeek() {
        mineSeek = mSeekBar
    }

    fun setRowSeek(isRowSeek: Boolean) {
        this.isRowSeek = isRowSeek
    }

    fun setColumnSeek(isColumnSeek: Boolean) {
        this.isColumnSeek = isColumnSeek
    }
}

