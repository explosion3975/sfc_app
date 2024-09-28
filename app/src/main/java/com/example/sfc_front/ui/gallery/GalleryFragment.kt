package com.example.sfc_front.ui.gallery
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.text.style.RelativeSizeSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.sfc_front.R
import com.example.sfc_front.databinding.FragmentSlideshowBinding

class GalleryFragment : Fragment() {
    private lateinit var slideshowViewModel: GalleryViewModel
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        slideshowViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)
        val root = inflater.inflate(R.layout.help, container, false)

        // 设置布局文件的内容
        slideshowViewModel.setLayoutContent(R.layout.help)
        val textView = root.findViewById<TextView>(R.id.text_help)
        val text = textView.text.toString()
        val builder = SpannableStringBuilder(text)
        val color1="#F9F6B1"
        val wordSize1=1.5f
        val color2="#76FFFF"
        val wordSize2=1.5f
        // 设置不同部分的颜色
        setTextColorAndSize(builder, "立即加密系統:", color1,wordSize1) // 蓝色
        setTextColorAndSize(builder, "升級:", color1,wordSize1) // 蓝色
        setTextColorAndSize(builder, "加密外部檔案:", color1,wordSize1) // 蓝色
        setTextColorAndSize(builder, "查看加密資料:", color1,wordSize1) // 蓝色
        setTextColorAndSize(builder, "子功能:", color1,wordSize1) // 蓝色

        setTextColorAndSize(builder, "CAMERA:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "VIDEO:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "NOTE:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "UPGRADE:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "FILE PROTECTION:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "BROWSE CYPHER:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "FORGET PASSWORD:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "雲端備份:", color2,wordSize2) // 红色
        setTextColorAndSize(builder, "Help:", color2,wordSize2) // 红色

        // 设置文本视图的文本内容
        textView.text = builder
        return root
    }
    private fun setTextColorAndSize(builder: SpannableStringBuilder, textToColor: String, color: String, textSizeRatio: Float) {
        val start = builder.toString().indexOf(textToColor)
        if (start != -1) {
            val end = start + textToColor.length
            builder.setSpan(ForegroundColorSpan(Color.parseColor(color)), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            builder.setSpan(RelativeSizeSpan(textSizeRatio), start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }


}