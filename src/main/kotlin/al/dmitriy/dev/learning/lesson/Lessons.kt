package al.dmitriy.dev.learning.lesson

enum class Lessons(val title: String, private val lesson: List<String>) {

  PRESENT_SIMPLE("Present Simple, спряжение глаголов", presentSimple),
  PRESENT_CONTINUOUS("Present Continuous, состояния", presentContinuous),
  PRONOUN_PREPOSITION("Pronoun and preposition", pronounAndPreposition),
  PASSIVE_VOICE("Passive Voice, пассивный залог", passiveVoice),
  PRESENT_SENTENCE("Present Sentence", perfectSentence),
  DATE_AND_TIME("Дата и время", dateAndTime),
  MUCH_MANY_LOT("Множества", muchManyLot),
  COMPARE_WORDS("Сравнения", compareWords),
  VARIOUS_WORDS("Разные", variousWords);


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


    val randomWords = listOf("I", "You", "he", "she", "they", "him", "her", "it", "them", "mine", "its", "our", "itself", "myself", "herself", "yourselves", "themselves", "ourselves", "himself", "those", "these",
        "that", "what", "which", "whose", "who", "whom", "me", "at", "is", "to", "am", "go", "for", "in", "us")

}

private val dateAndTime = listOf("Пять часов*the five o'clock", "3 июня*the third of July", "Пять часов*the five o'clock", "В понедельник*on Monday")
private val muchManyLot = listOf("Много денег*much money", "много карандашей*many pens", "Многие из них*the lot of them")
private val passiveVoice = listOf("Книга была написана*The book was written", "Ты любим*You are loved", "Книга была написана*The book was written", "Ты любим*You are loved")
private val compareWords = listOf("Ты выше других*you are taller than others", "Эта машина самая большая*this car is the biggest", "Эта машина самая большая*this car is the biggest")
private val presentSimple = listOf("Тёмный, мрачный коридор, я на цыпочках, как вор*a dark gloomy corridor I'm tiptoeing like a thief", "Замученный дорогой, я выбился из сил*tortured by the road I was exhausted", "В заросшем парке стоит старинный дом*there is an old house in an overgrown park",
    "За столом сидели мужики и ели*the peasants were sitting at the table and eating", "Дремлет за горой мрачный замок мой*my gloomy castle sleeps behind the mountain")
private val perfectSentence = listOf("Я видел его*I had seen him", "Я видел его*I had seen him", "Вечером я встречу его*by evening i will have met him")
private val presentContinuous = listOf("Ты бледный*You are pale", "Лондон столица Великобритании*London is the capital of Great Britain", "Я иду домой*I am going home")
private val pronounAndPreposition = listOf("Папа был зол на нас*dad was angry wit us", "Я хочу, чтобы ты пошёл*I want you to go", "Я хочу, чтобы ты пошёл*I want you to go")
private val variousWords = listOf("Я не знаю ничего*I don't know nothing", "Я не знаю*I don't know", "как собака*like a dog")