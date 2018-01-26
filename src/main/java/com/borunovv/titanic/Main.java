package com.borunovv.titanic;

/**
 * Это вариант решение задачи машинного обучения с сайта Kaggle (https://www.kaggle.com/c/titanic) 
 * с помощью нейронной сети, обученной с помощью генетического алгоритма.
 *
 * Что делать с этим:
 * 1. Убедитесь что относительно текущего каталока есть директория titanic 
 * с файликами train.csv (обучающая выборка) и  test.csv (контрольная выборка)
 * 
 * 2. Запустите этот класс на исполнение. 
 * 3. Дождатесь завершения работы (минуты полторы-две) и появится файлик result.csv с предсказанием.
 * 4. В логах (консоль) будет процент точности обучения.
 */
public class Main {

    public static void main(String[] args) throws Exception {
        String trainCsvResource = "/titanic/train.csv";
        String testCsvResource = "/titanic/test.csv";
        String outputCsvFile = "result.csv";

        new TitanicSolver().solve(trainCsvResource, testCsvResource, outputCsvFile);
    }

}
