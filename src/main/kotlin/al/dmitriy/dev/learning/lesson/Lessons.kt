package al.dmitriy.dev.learning.lesson

import al.dmitriy.dev.learning.apptexts.*
import al.dmitriy.dev.learning.service.config

enum class Lessons(val pictureUrl: String, val title: String, private val lesson: List<String>) {

  PRESENT_SIMPLE("https://disk.yandex.ru/i/nPh5U1bT517ocw", "Present Simple, спряжение глаголов", presentSimple),
  PRESENT_CONTINUOUS("https://disk.yandex.ru/i/3VDNulKXQmc3Hw", "Present Continuous, состояния", presentContinuous),
  PASSIVE_VOICE("https://disk.yandex.ru/i/Bw74cS0p2u7jww", "Passive Voice, пассивный залог", passiveVoice),
  PERFECT_TENSE("https://disk.yandex.ru/i/pc8saaKjl42img", "Perfect tense", perfectTense),
  DATE_AND_TIME("https://disk.yandex.ru/i/V_0ZZeTDZ-18AA", "Дата и время", dateAndTime),
  MUCH_MANY_LOT("https://disk.yandex.ru/i/KbHG8BL4NXKygw", "Множества", muchManyLot),
  COMPARE_WORDS("https://disk.yandex.ru/i/-lHXrk9V8wjBBA", "Сравнения", compareWords),
  VARIOUS_WORDS(config.bottomBillboardUrl, "Разные", variousWords),
  LEARN_WORDS(config.bottomBillboardUrl, "Учить новые слова", listOf(""));

    fun getLessonsTitles(): List<String> {
      val titles = mutableListOf<String>()
      Lessons.values().forEach { elem -> titles.add(elem.title) }
      return titles
  }


  fun createLessonText(userLessonText: String): MutableList<String> {
      val list: MutableList<String> = mutableListOf()
      list.addAll(this.lesson)
      return if (userLessonText.isNotEmpty()) {
          list.addAll(userLessonText.split("#"))
          list // return
      } else list // return
  }

}

