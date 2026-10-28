package com.demo.miniflicks.dramaseries.shortstory.base
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.demo.miniflicks.dramaseries.shortstory.util.SystemUtilOscar
import com.demo.miniflicks.dramaseries.shortstory.R


abstract class BaseFragmentOscar<VB : ViewBinding> : Fragment() {

    protected lateinit var binding: VB

    abstract fun setBinding(
        inflater: LayoutInflater?,
        container: ViewGroup?,
        saveInstanceState: Bundle?,
    ): VB

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = setBinding(inflater, container, savedInstanceState)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        SystemUtilOscar.setLocale(context)

        getData()
        initView()
        bindView()
    }

    open fun getData() {

    }

    open fun initView() {

    }

    open fun bindView() {

    }


    fun startNextActivity(actClass: Class<*>, bundle: Bundle?) {
        var bundleOscar = bundle
        val intentOscar = Intent(requireActivity(), actClass)
        if (bundleOscar == null) {
            bundleOscar = Bundle()
        }
        intentOscar.putExtras(bundleOscar)
        startActivity(intentOscar)
        requireActivity().overridePendingTransition(R.anim.in_right, R.anim.out_left)
    }

}