--
-- data for table `user`
--

-- INSERT INTO `user` (`id`, `name`, `pwd`, `email`, `confirm_id`, `confirmed`, `created`) 
-- VALUES (1,'admin','4efb9509f3c67009c2d51fc54186fbd9','your.mail@your.host','',1,'2015-09-30 12:36:43');

--
-- table `site_text`
--

INSERT INTO `site_text` (`id`, `lang`, `val`) VALUES
('about', 'uk', 
'<p><b>Основна мета проекту</b> — створення двомовних онлайн-словників.
Редагувати дані словників можуть всі зареєстровані користувачі.
Принцип досить схожий на Вікіпедію.
Також є набір інструментів, які використовують дані словників.
</p> 
<hr/>
<h2>Короткий опис інструментів та функцій</h2>
<p><b>Створення облікового запису (реєстрація).</b> Для перегляду словників реєстрація не потрібна,
але для редагування словників і доступу до більшості інструментів необхідно створити обліковий запис.
Для цього необхідно ввести ім''я, під яким ви хочете зареєструватись, адресу електронної пошти та пароль.
Після цього на вашу електронну пошту надійде лист з проханням підтвердити реєстрацію. Цей лист часто розпізнається як
спам, тому будьте уважні. Після підтвердження реєстрації ви отримаєте доступ до всіх наявних інструментів.
</p>
<p><b>Cловники.</b> Зареєстровані користувачі можуть редагувати словники.
Редагуючи словники намагайтеся дотримуватися словникового стилю, бути конструктивними й покращувати словники.
</p>
<p><b>Вивчені слова.</b> Цей інструмент надає користувачу можливість позначати вже вивчені слова певного словника.
Дані про вже вивчені слова використовуються інструментом ''Навчальні тексти''.
Детальніший опис знаходиться в закладці ''Допомога'' інструменту ''Вивчені слова''.
<b>Незареєстровані користувачі не можуть використовувати цей інструмент.</b> 
</p>
<p><b>Навчальні тексти.</b> Підготовка тексту за допомогою обраного словника.
Детальніший опис знаходиться в закладці ''Допомога'' інструменту ''Навчальні тексти''.
</p>
<p><b>Тест словникового запасу</b> допоможе вам оцінити ваш словниковий запас.
Тест складається з двох частин: грубої оцінки та уточнення. В обох частинах необхідно поставити галочки біля відомих вам слів.
</p>');

--
-- table `publications`
--

INSERT INTO `publications` (`id`, `lang`, `visible`, `title`, `header`, `content`, `author`, `publish_date`) VALUES
(1, 'uk', 'y', 'publication 1', 'header publication 1', '<h>publication 1</h>
<p>Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.
</p> 
<hr/>
<h2>Subtitle</h2>
<p>Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.    
</p>
<p>Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.  
</p>', 'linguisto', '2017-03-02 10:27:44');

INSERT INTO `publications` (`id`, `lang`, `visible`, `title`, `header`, `content`, `author`, `publish_date`) VALUES
(2, 'uk', 'y', 'publication 2', 'header publication 2', '<h>publication 2</h>
<p>Lorem Ipsum не є випадковим набором літер.
</p> 
<hr/>
<h2>Subtitle</h2>
<p>Існує багато варіацій уривків з Lorem Ipsum, але більшість з них зазнала певних змін на кшталт жартівливих вставок або змішування слів, які навіть не виглядають правдоподібно.    
</p>
<p>Більшість відомих генераторів Lorem Ipsum в Мережі генерують текст шляхом повторення наперед заданих послідовностей Lorem Ipsum.  
</p>', 'linguisto', '2017-03-02 10:27:44');


--
-- table `daily_word`
--

INSERT INTO `daily_word` (`day`, `lang`, `word`) VALUES
(0, 'de', 'und'),
(0, 'en', 'and');

