package com.example.sfc_front



import android.annotation.SuppressLint
import android.view.View
import android.widget.Switch
import com.example.sfc_front.R


class SwitchStatus(private val rootView: View) {

    private val switch: Switch = rootView.findViewById(R.id.switchButton)

    // 在这个类中可以通过 switch 访问 Switch 按钮的状态
    fun readSwitchState(): Boolean {
        return switch.isChecked
    }
}
