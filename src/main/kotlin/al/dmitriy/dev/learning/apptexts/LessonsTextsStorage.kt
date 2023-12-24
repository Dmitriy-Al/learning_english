package al.dmitriy.dev.learning.apptexts

val randomWords = listOf("I", "You", "he", "she", "they", "him", "her", "it", "them", "mine", "its", "our", "itself", "myself", "herself", "yourselves", "themselves", "ourselves", "himself", "those", "these",
    "that", "what", "which", "whose", "who", "whom", "me", "at", "is", "to", "am", "go", "for", "in", "us", "be", "then", "if", "do", "does", "are", "will", "was")

val presentSimple = listOf("Я люблю*I love", "Я не люблю*I do not love", "Я люблю?*do I love", "Я буду любить*I will love", "Я не буду любить*I will not love", "Я буду любить?*will I love", "Я любил*I loved",
    "Я не любил*I did not love", "Я любил?*did I love", " Я иду*I go", "Я не иду*I do not go", "Я иду?*do I go", "Я буду ходить*I will go", "Я не буду ходить*I will not go", "Я буду ходить?*will I go", "Я ходил*I went",
    "Я не ходил*I did not go", "Я ходил?*did I go", " Я смотрю*I look", "Я не смотрю*I do not look", "Я смотрю?*do I look", "Я буду смотреть*I will look", "Я не буду смотреть*I will not look", "Я буду смотреть?*will I look",
    "Я смотрел*I looked", "Я не смотрел*I did not look", "Я смотрел?*did I look", "Мы слышим*we hear", "Мы не слышим*we do not hear", "Мы слышим?*do we hear", "Мы будем слышать*we will hear",
    "Мы не будем слышать*we will not hear", "Мы будем слышать?*will we hear", "Мы слышали*we heard", "Мы не слышали*we did not hear", "Мы слышали?*did we hear", "Я хочу*I want", "Я не хочу*I do not want",
    "Я хочу?*do I want", "Я буду хотеть*I will want", "Я не буду хотеть*I will not want", "Я буду хотеть?*will I want", "Я хотел*I wanted", "Я не хотел*I did not want", "Я хотел?*did I want", "Я вижу*I see",
    "Я не вижу*I do not see", "Я вижу?*do I see", "Я буду видеть*I will see", "Я не буду видеть*I will not see", "Я буду видеть?*will I see", "Я видел*I saw", "Я не видел*I did not see", "Я видел?*did I see",
    "Он слушает*he listens", "Он не слушает*he does not listen", "Он слушает?*does he listen", "Он будет слушать*he will listen", "Он не будет слушать*he will not listen", "Он будет слушать?*will he listen",
    "Он слушал*he listened", "Он не слушал*he did not listen", "Он слушал?*did he listen", "Он спит*he sleeps", "Он не спит*he does not sleep", "Он спит?*does he sleep", "Он будет спать*he will sleep",
    "Он не будет спать*he will not sleep", "Он будет спать?*will he sleep", "Он спал*he slept", "Он не спал*he did not sleep", "Он спал?*did he sleep", "Она улыбается*she smiles", "Она не улыбается*she does not smile",
    "Она улыбается?*does she smile", "Она будет улыбаться*she will smile", "Она не будет улыбаться*she will not smile", "Она будет улыбаться?*will she smile", "Она улыбалась*she smiled", "Она не улыбалась*she did not smile",
    "Она улыбалась?*did she smile", "Она читает книгу*she reads a book", "Она не читает книгу*she does not read a book", "Она читает книгу?*does she read a book", "Она будет читать книгу*she will read a book",
    "Она не будет читать книгу*she will not read a book", "Она будет читать книгу?*will she read a book", "Она читала книгу*she read a book", "Она не читала книгу*she did not read a book",
    "Она читала книгу?*did she read a book", "Он хочет бежать*he wants to run", "Он не хочет бежать*he doesn't want to run", "Он хочет бежать?*does he want to run", "Он захочет бежать*he will want to run",
    "Он не захочет бежать*he will not want to run", "Он захочет бежать?*will he want to run?", "Он хотел бежать*he wanted to run", "Он не хотел бежать*he didn't want to run", "Он хотел бежать?*did he want to run",
    "Они любят выигрывать*they like to win", "Они не любят проигрывать*they don't like to lose", "Они любят выигрывать?*do they like to win", "Они полюбят выигрывать*they will like to win",
    "Они не будут любить проигрывать*they will not like to lose", "Они полюбят выигрывать?*will they like to win", "Они любили выигрывать*they liked to win", "Они не любили проигрывать*they didn't like to lose",
    "Они любили выигрывать?*did they like to win", "Тебе надо идти домой*You need to go home", "Тебе не надо идти домой*You don't need to go home", "Тебе надо идти домой?*do You need to go home",
    "Тебе надо будет идти домой*You will need to go home", "Тебе не надо будет идти домой*You will not need to go home", "Тебе надо будет идти домой?*will You need to go home", "Тебе надо было идти домой*You needed to go home",
    "Тебе не надо было идти домой*You didn't need to go home", "Тебе надо было идти домой?*did You need to go home", "Ты учишь Английский язык*You learn English", "Ты не учишь Английский язык*You don't learn English",
    "Ты учишь Английский язык?*do You learn English", "Ты будешь учить Английский язык*You will learn English", "Ты не будешь учить Английский язык*You won't learn English",
    "Ты будешь учить Английский язык?*will You learn English", "Ты учил Английский язык*You learned English", "Ты не учил Английский язык*You didn't learn English", "Ты учил Английский язык?*did You learn English",
    "Это выглядит как яблоко*It looks like an apple", "Это не выглядит как яблоко*It doesn't look like an apple", "Это выглядит как яблоко?*does it look like an apple", "Это будет выглядеть как яблоко*It will look like an apple",
    "Это не будет выглядеть как яблоко*It won't look like an apple", "Это будет выглядеть как яблоко?*will it look like an apple", "Это выглядело как яблоко*It looked like an apple",
    "Это не выглядело как яблоко*It didn't look like an apple", "Это выглядело как яблоко?*did it look like an apple", "Мы смотрим на него*we look at him", "Мы не смотрим на него*we don't look at him",
    "Мы смотрим на него?*do we look at him", "Мы будем смотреть на него*we will look at him", "Мы не будем смотреть на него*we won't look at him", "Мы будем смотреть на него?*will we look at him",
    "Мы смотрели на него*we looked at him", "Мы не смотрели на него*we didn't look at him", "Мы смотрели на него?*did we look at him")

val dateAndTime = listOf("Пять часов*the five o'clock", "3 июня*the third of July", "Пять часов*the five o'clock", "В понедельник*on Monday")

val muchManyLot = listOf("Много денег*much money", "много карандашей*many pens", "Многие из них*the lot of them")

val passiveVoice = listOf("Книга была написана*The book was written", "Ты любим*You are loved", "Книга была написана*The book was written", "Ты любим*You are loved")

val compareWords = listOf("Ты выше других*you are taller than others", "Эта машина самая большая*this car is the biggest", "Эта машина самая большая*this car is the biggest")

val perfectTense = listOf("Я видел его*I had seen him", "Я видел его*I had seen him", "Вечером я встречу его*by evening i will have met him")

val presentContinuous = listOf("Ты бледный*You are pale", "Лондон столица Великобритании*London is the capital of Great Britain", "Я иду домой*I am going home")

val pronounPreposition = listOf("Папа был зол на нас*dad was angry wit us", "Я хочу, чтобы ты пошёл*I want you to go", "Я хочу, чтобы ты пошёл*I want you to go")

val variousWords = listOf("Я не знаю ничего*I know nothing", "Я не знаю*I don't know", "как собака*like a dog")