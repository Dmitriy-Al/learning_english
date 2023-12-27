package al.dmitriy.dev.learning.apptexts

// Тексты уроков
val randomWords = listOf("I", "You", "he", "she", "they", "him", "her", "it", "them", "mine", "its", "our", "itself", "myself", "herself", "yourselves", "themselves", "ourselves", "himself", "those", "these",
    "that", "what", "which", "whose", "who", "whom", "me", "at", "is", "to", "am", "go", "for", "in", "us", "be", "then", "if", "yes", "went", "are", "will", "want")

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

val dateAndTime = listOf("Сейчас пять минут пятого*it’s five past four", "Сейчас полшестого*it’s half past five", "Сейчас без четверти восемь*it’s quarter to eight", "Сейчас без двух минут час*it’s two minutes to one",
    "Я приду к нему в понедельник*I will come to him on Monday", "Они придут к ним во вторник*they will come to them on Tuesday", "Он не придёт к ней в среду*he will not come to her on Wednesday",
    "Она приходила к нему в четверг*she came to him on Thursday", "Она приходила ко мне в пятницу*she came to me on Friday", "Я приду к нему в субботу*I will come to him on Saturday",
    "Они придут к ним в воскресенье*they will come to them on Sunday", "Они придут к ним зимой*they will come to them in Winter", "Она приходила к нему летом*she came to him in Summer",
    "Он не придёт к ней весной*he will not come to her in Spring", "Я приду к нему осенью*I will come to him in Autumn", "Я приду к нему в январе*I will come to him in January",
    "Он не придёт к ней в  марте*he will not come to her in March", "Они придут к ним в апреле*they will come to them in April", "Она приходила ко мне в июле*she came to me in July",
    "Я приду к нему в августе*I will come to him in August", "Они придут к ним в ноябре*hey will come to them in November", "Он не придёт к ней в декабре*he will not come to her in December",
    "Она приходила к нему в июне*she came to him in June", "Она приходила ко мне в феврале*she came to me in February", "Они придут к ним в октябре*they will come to them in October",
    "Я приду к нему в августе*I will come to him in August", "Она приходила к нему в сентябре*she came to him in September", "До истечения одного месяца*before one month", "Я приду сегодня*I will come today",
    "Всё изменилось в 2014 году*everything changed in 2014", "Они зажгли лампу в 9 часов*they lit a lamp at nine o'clock", "Мы пойдем домой в шесть часов десять минут*we will go home at ten past six",
    "Поезд поедет в 14:40*the train will leave at twenty to three", "Мой день рождения третьего мая*my birthday is on the third of May", "Мой день рождения десятого июня*my birthday is on the tenth of June",
    "Мой день рождения первого января*my birthday is on the first of January", "Первый*first", "Второй*second", "*Третий*third", "Четвертый*fourth", "Пятый*fifth", "Шестой*sixth", "Седьмой*seventh",
    "Восьмой*eighth", "Девятый*ninth", "Десятый*tenth", "Одиннадцатый*eleventh", "Двенадцатый*twelfth", "Двадцатый*twentieth", "Тридцатый*thirtieth", "Сороковой*fortieth", "307-й*three hundred and seventh",
    "600-й*six hundredth", "1000 000-й*one millionth", "Я хочу купить билет на завтрашний авиарейс*I want a ticket for tomorrow flight", "Вчера все мои беды казались такими далекими*yesterday all my troubles seemed so far away",
    "Купить сейчас*buy now", "За два дня до*two days ago", "После полудня*past noon", "После заката*after sunset", "День за днем*day by day", "Всю ночь*all through the night", "Это случилось осенью*it happened in the autumn")

val muchManyLot = listOf("Много столов*many tables", "Много ложек*many spoons", "Много яблок*many apples", "Много денег*much money", "Было найдено много информации*much information has been found",
    "Много столов*many tables", "У них мало денег*they have little money", "У них мало монет*they have few coins", "Они имеют много денег*they have got much money",
    "У них много монет*they have many coins", "Многие из них*a lot of them", "Она ему очень нравится*he likes her a lot", "Его фильмы получили множество призов*his films have won lots of prizes",
    "Много сахара*a lot of sugar", "Он не ест много мяса*he doesn't eat much meat", "Она ест много фруктов*She eats much fruits", "В городе немного старых зданий*there are a few old buildings in the city",
    "В музее было мало людей*there were few people in the museum", "У него мало свободного времени*he has little free time", "Прошлой зимой у нас было мало снега*we had little snow last winter",
    "У меня так много новостей для тебя*I have got so much news to tell you", "Я хочу задать тебе так много вопросов*there are so many things I want to ask you", "Много сахара*much sugar",
    "С тех пор утекло много воды*much of water has flown away since then")

val passiveVoice = listOf("Ты любим*You are loved", "Ты не любим*You aren't loved", "Ты любим?*are You loved", "Ты будешь любим*You will be loved", "Ты не будешь любим*You won't be loved", "Ты будешь любим?*will You be loved",
    "Ты был любим*You were loved", "Ты не был любим*You weren't loved", "Ты был любим?*were You loved", "Письма пишут каждый день*letters are written every day", "Письмо было написано вчера*the letter was written yesterday",
    "Письмо будет написано завтра*the letter will be written tomorrow", "Письмо написанное сейчас*the letter is being written now", "Вас встретят в гостинице*You will be met at the hotel",
    "Письмо писали вчера в пять часов*the letter was being written at five o'clock yesterday", "Письмо уже написано*the letter has already been written", "Комната была убрана ужасно*the room was cleaned horribly",
    "Письмо было написано вчера к пяти часам*the letter had been written by five o'clock yesterday", "Письмо будет написано завтра к пяти часам*the letter will have been written by five o'clock tomorrow",
    "Тысячи долларов тратятся на кофе*thousands of dollars are spent on coffee", "Собаку кормят каждый день*the dog is fed every day", "Собака была накормлена вчера*the dog was fed yesterday",
    "Радио изобрели 150 лет назад*the radio was invented 150 years ago", "Машину заправляют сейчас*the car is being refueled now", "Экзамен сдавали вчера утром*the exam was being taken yesterday morning",
    "Эти цветы уже полили*this flowers have already been watered", "Статью перепишут к завтрашнему утру*the article will have been rewritten by tomorrow morning", "Марли был мертв*Marley was dead",
    "Моя машина уже угнана*my car has been stolen", "Эта книга была написана не им*this book was not written by him", "Меня нечасто приглашают на вечеринки*I am not often invited to the parties",
    "Подарок еще не привезли*The gift has not been bought yet", "Кот не был накормлен им вчера*the cat was not fed by him yesterday", "Кота не часто оставляли голодным*the cat was not often left hungry",
    "Вам объяснили это домашнее задание?*has this homework been explained to you", "Задача обсуждалась, когда я вошел в класс*the task was being discussed when I entered the class",
    "Письмо будет написанным к следующему утру*the letter will have been written by the next morning", "Письмо будет написано к на следующий день*the letter will be written the next day",
    "Собака была накормлена вчера*the dog was fed yesterday", "Собака будет накормленной завтра»*the dog will be fed tomorrow", "Собаку кормят прямо в этот момент*the dog is being fed at the moment",
    "Собаку кормили, когда мы пришли*the dog was being fed when we came", "В 9 вечера собака будет накормлена*at nine o'clock tonight the dog will be being fed", "Собака недавно покормлена*the dog has been fed recently",
    "Собака была покормлена до того, как я пришёл»*the dog had been fed before I came", "Собака будет покормленной к 9 часам*the dog will have been fed by nine o’clock")

val compareWords = listOf("Ты выше других*you are taller than others", "Эта машина самая быстрая*this car is the fastest", "Анна самая умная в классе*Anna is the cleverest in the class",
    "Моя машина быстрее твоей*My car is faster than your", "Эта дорога была самой короткой*this road was the shortest", "Сегодня она выглядит лучше, чем вчера*today she looks better than yesterday",
    "Вчера она выглядела хуже, чем сегодня*yesterday she looked worse than today", "Он был его лучшим другом*he was the best his friend", "Эта задача будет самой сложной*this task will be the most difficult",
    "Этот цветок удивительнее других*this flower is more wonderful than others", "Эта самая добрая собака в мире*this is the kindest dog in the world",
    "Становится всё жарче и жарче*it is getting hotter and hotter", "Я работал всё усерднее и усерднее, пока не завершил этот проект*I worked harder and harder until we finished this project",
    "Фильм был самым скучным*the movie was the most boring", "Меркурий не больше Земли*Mercury isn't bigger than Earth", "Вчера был самый жаркий день года*yesterday was the hottest day of the year",
    "Один из самых милых людей, которых я знаю*one of the nicest people I know", "Ты старше меня*You are older than me")

val perfectTense = listOf("Я (уже) посмотрел фильм*I have seen the movie", "Я (когда-то в прошлом) смотрел этот фильм*I had seen this movie", "Я увижу этот фильм к вечеру*I will have seen this movie by evening",
    "У него (уже) была собака*he has had a dog", "У него (когда-то в прошлом) была собака*he had had a dog", "У него будет собака (он уже будет владельцем собаки)*he will have had a dog",
    "Ты находишься здесь год*You have been here for a year", "Ты находился здесь год*You had been here for a year", "Ты будешь находиться здесь год*You will have been here for a year", "Он - сбежавший*he has escaped",
    "Он сбежал раньше, чем они нашли его*he had escaped before they found him", "Он убежит (будет сбежавшим) к полуночи*he will have escaped by midnight", "Они (когда-то в прошлом) работали*they had worked",
    "Они работали (сейчас)*they have worked", "Они отработают*they will have worked", "Я только завершил мою работу*I have just finished my work", "Я закончил мою работу вчера*I had finished my work yesterday",
    "Я закончу работу (моя работа будет законченной) завтра к 5 часам*I will have finished my work tomorrow by 5 o’clock", "Они уже приехали*they have arrived already", "Мы не услышали*we haven't heard",
    "Я (к настоящему моменту) писал книгу*I have written a book", "Я (к настоящему моменту) никогда не писал книгу*I have newer written a book", "Я (к настоящему моменту) писал книгу?*have I written a book",
    "Я буду писать книгу*I will have written a book", "Я не буду писать книгу*I won't have written a book", "Я буду писать книгу?*will I have written a book", "Мы услышали*we have heard",
    "Я (до последующих событий) писал книгу*I had written a book", "Я (до последующих событий) не писал книгу*I hadn't written a book", "Я писал книгу? (до последующих событий)*had I written a book",
    "Мы услышали?*have we heard", "Мы будем слышать*we will have heard", "Мы не будем слышать*we won't heard", "Мы будем слышать?*will we have heard", "Мы слышали (когда-то до)*we had heard",
    "Мы не слышали (когда-то до)*we didn't have heard", "Мы слышали? (когда-то до)*did we have heard", "Она (уже) читала книгу *she has read a book", "Она не читала книгу (сейчас)*she doesn't have read a book",
    "Она (уже) читала книгу?*has she read a book", "Она будет читать книгу*she will have read a book", "Она не будет читать книгу*she won't have read a book", "Она будет читать книгу?*will she have read a book",
    "Она прочитала книгу (когда-то до)*she had read a book", "Она не прочитала книгу (когда-то до)*she hadn't read a book", "Она прочитала книгу? (когда-то до)*had she read a book",
    "Ты (уже) учил Английский язык*You have learned English", "Ты не учил Английский язык (сейчас)*You don't have learned English", "Ты учил Английский язык? (уже)*have You learned English",
    "Ты будешь учить Английский язык*You will have learned English", "Ты не будешь учить Английский язык*You won't have learned English", "Ты будешь учить Английский язык?*will You have learned English",
    "Ты выучил Английский язык (когда-то до)*You had learned English", "Ты не выучил Английский язык (когда-то до)*You hadn't learned English", "Ты учил Английский язык? (когда-то до)*had You learned English")

val presentContinuous = listOf("Лондон - столица Великобритании*London is the capital of Great Britain", "Манчестер - не столица Великобритании*Manchester is not the capital of Great Britain",
    "Лондон - столица Великобритании?*is London the capital of Great Britain", "Лондон будет столицей Великобритании*London will be the capital of Great Britain",
    "Манчестер - не будет столицей Великобритании*Manchester will not be the capital of Great Britain", "Будет Лондон столицей Великобритании?*will be London the capital of Great Britain",
    "Лондон был столицей Великобритании*London was the capital of Great Britain", "Манчестер - не был столицей Великобритании*Manchester was not the capital of Great Britain",
    "Лондон был столицей Великобритании?*was London the capital of Great Britain", "Я голодный*I'm hungry", "Я не голодный*I'm not hungry", "Я голодный?*am I hungry", "Я буду голодным*I'll be hungry",
    "Я не буду голодным*I won't be hungry", "Я буду голодным?*will I be hungry", "Я был голодный*I was hungry", "Я не был голодный*I wasn't hungry", "Я был голодный?*was I hungry",
    "Я сплю сейчас*I am sleeping now", "Я не сплю сейчас*I am not sleeping now", "Я сплю сейчас?*am I sleeping now", "Я - иду*I am going", "Я не иду*I'm not going", "Я иду?*am I going",
    "Я буду идти*I will be going", "Я не буду идти*I will not be going", "Я буду идти?*will I be going", "Я шёл*I was going", "Я не шёл*I was not going", "Я шёл?*was I going",
    "Он бледный*he is pale", "Он не бледный*he is not pale", "Он бледный?*is he pale", "Он будет бледный*he will be pale", "Он не будет бледный*he will not be pale", "Он будет бледный?*will he be pale",
    "Он был бледный*he was pale", "Он не был бледный*he was not pale", "Он был бледный?*was he pale", "Он поет песню*he is singing a song", "Он не поет песню*he is not singing a song", "Он поет песню?*is he singing a song",
    "Он будет петь песню*he will be singing a song", "Он не будет петь песню*he won't be singing a song", "Он будет петь песню?*will he be singing a song", "Он пел песню*he was singing a song",
    "Он не пел песню*he wasn't singing a song", "Он пел песню?*was he singing a song", "Мы счастливы*we are happy", "Мы не счастливы*we are not happy", "Мы счастливы?*are we happy", "Мы будем счастливы*we will be happy",
    "Мы не будем счастливы*we will not be happy", "Мы будем счастливы?*will we be happy", "Мы были счастливы*we were happy",  "Мы не были счастливы*we were not happy", "Мы были счастливы?*were we happy",
    "Они улыбаются тебе*they are smiling at you", "Они не улыбаются тебе*they aren't smiling at you", "Они улыбаются тебе?*are they smiling at you", "Они будут улыбаться тебе*they will be smiling at you",
    "Они не будут улыбаться тебе*they won't be smiling at you", "Они будут улыбаться тебе?*will they be smiling at you", "Они улыбались тебе*they were smiling at you", "Они не улыбались тебе*they weren't smiling at you",
    "Они улыбались тебе?*were they smiling at you", "Она прекрасна*she is beautiful", "Она не прекрасна*she isn't beautiful", "Она прекрасна?*is she beautiful", "Она будет прекрасна*she will be beautiful",
    "Она не будет прекрасна*she won't be beautiful", "Она будет прекрасна?*will she be beautiful", "Она была прекрасна*she was beautiful", "Она не была прекрасна*she wasn't beautiful", "Она была прекрасна?*was she beautiful",
    "Ты тренируешься*You are training", "Ты не тренируешься*You aren't training", "Ты тренируешься?*are You training", "Ты будешь тренироваться*You will be training", "Ты не будешь тренироваться*You won't be training",
    "Ты будешь тренироваться?*will You be training", "Ты тренировался*You were training", "Ты не тренировался*You weren't training", "Ты тренировался?*were You training", "Я сплю сейчас*I am sleeping now",
    "Я не сплю сейчас*I am not sleeping now", "Я сплю сейчас?*am I sleeping now")

val variousWords = listOf("Я люблю яблоки*I like apples", "как собака*like a dog", "Я не знаю ничего*I know nothing", "Если он захочет\n(предложения с условиями if и when всегда употребляются в настоящем времени)*if he wants",
    "Когда ты пойдёшь в лес, не забудь компас\n(предложения с условиями if и when всегда употребляются в настоящем времени)*when you go to the forest don't forget the compass",
    "Что ты обычно делаешь утром?*What do you usually do in the morning", "Ночью все кошки серые*all cats are grey at night", "Это был холодный вечер*it was a cold evening", "Не опаздывай на ланч*don’t be late for lunch",
    "Включи свет!*switch on the light", "Я могу дать тебе много советов*I can give you a lot of advice", "Я не люблю покупать новую одежду*I don’t like to by new clothes",
    "Она одевается очень вызывающе*she dresses very provocatively", "Ты вел себя отвратительно*You behaved disgustingly", "Я хочу, чтобы ты пошёл*I want you to go", "Я хочу, чтобы он ответил*I want him to answer",
    "За столом сидели мужики и ели*men were sitting and eating at the table", "Это жена моего брата*it’s my brother’s wife", "Кусочек сыра на столе – твой*the piece of cheese on the table is yours",
    "Двухдневное путешествие*two days’ trip", "Это - моё*it's mine", "Они видели их*they saw themselves", "Мы увидели сбя*we saw ourselves", "Он нравится сбе*he likes himself", "Ты нравишься сбе*You like yourselves",
    "Он бежал так быстро, как мог*he ran as fast as he could", "Театр Лондона*London’s theatre")