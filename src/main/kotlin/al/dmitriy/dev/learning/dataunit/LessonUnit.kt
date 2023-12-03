package al.dmitriy.dev.learning.dataunit

data class LessonUnit(val russianText: String, val englishText: String, val forButtonText: List<String>, var inputText: String = "", val lessonTitle: String = "")
