package com.demo.miniflicks.dramaseries.shortstory.util
import android.content.Context
import com.demo.miniflicks.dramaseries.shortstory.model.LanguageModelOscar
import com.demo.miniflicks.dramaseries.shortstory.R
import java.util.Locale



object InsertListManager {

    fun getListLanguage(context: Context): MutableList<LanguageModelOscar> {
        val listLanguageOscar: MutableList<LanguageModelOscar> = ArrayList()
        listLanguageOscar.add(LanguageModelOscar("English", "en", R.drawable.ic_lang_en, false))
        listLanguageOscar.add(LanguageModelOscar("Hindi", "hi", R.drawable.ic_lang_hi, false))
        listLanguageOscar.add(LanguageModelOscar("Spanish", "es", R.drawable.ic_lang_es, false))
        listLanguageOscar.add(LanguageModelOscar("French", "fr", R.drawable.ic_lang_fr, false))
        listLanguageOscar.add(LanguageModelOscar("German", "de", R.drawable.ic_lang_de, false))
        listLanguageOscar.add(LanguageModelOscar("Korean", "ko", R.drawable.ic_lang_ko, false))
        listLanguageOscar.add(LanguageModelOscar("Portuguese", "pt", R.drawable.ic_lang_pt, false))
        listLanguageOscar.add(LanguageModelOscar("Japanese", "ja", R.drawable.ic_lang_jp, false))
        listLanguageOscar.add(LanguageModelOscar("Russian", "ru", R.drawable.ic_lang_ru, false))

        val deviceLanguageCodeOscar = Locale.getDefault().language
        val previousLanguageCodeOscar = SystemUtilOscar.getPreLanguage(context)
        if (SharedPreUtilsOscar.getInstance()
                .getCountOpenApp(context) > 1 && previousLanguageCodeOscar.isNotEmpty()
        ) {
            listLanguageOscar.sortWith { lang1: LanguageModelOscar, lang2: LanguageModelOscar ->
                lang1.name.compareTo(
                    lang2.name, ignoreCase = true
                )
            }
            for (i in listLanguageOscar.indices) {
                if (listLanguageOscar[i].code == previousLanguageCodeOscar) {
                    val selectedLanguageOscar = listLanguageOscar.removeAt(i)
                    listLanguageOscar.add(0, selectedLanguageOscar)
                    break
                }
            }
        } else {
            var isDeviceLangInListOscar = false
            for (i in listLanguageOscar.indices) {
                if (listLanguageOscar[i].code == deviceLanguageCodeOscar) {
                    val deviceLanguageOscar = listLanguageOscar.removeAt(i)
                    listLanguageOscar.add(0, deviceLanguageOscar)
                    isDeviceLangInListOscar = true
                    break
                }
            }
            if (!isDeviceLangInListOscar) {
                for (i in listLanguageOscar.indices) {
                    if (listLanguageOscar[i].code == "en") {
                        val englishLanguageOscar = listLanguageOscar.removeAt(i)
                        listLanguageOscar.add(0, englishLanguageOscar)
                        break
                    }
                }
            }
        }

        return listLanguageOscar
    }


}