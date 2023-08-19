package ru.veider.eventsreminder.presentation.ui

/**
 * Тег содержащий id вызвавшего фрагмента для передачи параметра через bundle, используемый
 *  для принятия решения о дальнейшей навигации в случае успешного завершения работы фрагмента
 * */
const val SOURCE_ID_TO_NAVIGATE = "SOURCE_ID_TO_NAVIGATE"
/**
 * Тег для передачи параметра через bundle во фрагмент,
 * содержащий id для дальнейшей навигации в случае выбора
 * item'а из списка в dashboard'е
 * */
const val EVENT_ID = "EVENT_ID"
/**
 * Максимальное значение года, которое можно задать в календаре,
 * для дня рождения эта влеичина соответствует отсутствию года в
 * дне рождения
 * */
const val MAX_YEAR = 2096
/**
 * Код запроса на результат выполнения активности по выбору пути и
 * имени файла (используется при сохранении файла)
 * */
const val CHOOSE_FILEPATH_REQUEST_CODE = 987654321