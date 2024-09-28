package com.example.sfc_front.ui.gallery

//import androidx.lifecycle.LiveData
//import androidx.lifecycle.MutableLiveData
//import androidx.lifecycle.ViewModel
//
//class GalleryViewModel : ViewModel() {
//
//    // 创建一个 LiveData 对象来存储布局文件中的内容
//    private val _layoutContent = MutableLiveData<Int>().apply {
//        // 初始值可以为空，因为您将在布局中设置它
//        value = null
//    }
//    val layoutContent: LiveData<Int> = _layoutContent
//
//    // 创建一个方法，以便在需要时设置布局内容
//    fun setLayoutContent(layoutResId: Int) {
//        _layoutContent.value = layoutResId
//    }
//}
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class GalleryViewModel : ViewModel() {
    // 创建一个 LiveData 对象来存储文本消息
    private val _layoutContent = MutableLiveData<Int>().apply {
        // 初始值可以为空，因为您将在布局中设置它
        value = null
    }
    val layoutContent: LiveData<Int> = _layoutContent

    // 创建一个方法，以便在需要时设置布局内容
    fun setLayoutContent(layoutResId: Int) {
        _layoutContent.value = layoutResId
    }
}
