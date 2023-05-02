package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.models.Number;
import org.springframework.stereotype.Service;
import org.example.repositories.NumberRepository;

import java.util.Arrays;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class NumberServiceImpl implements NumberService {
    private final NumberRepository numberRepository;

    @Override
    public String getNextNumber() {
        Number lastNumber = numberRepository.findFirstByOrderByIdDesc();
        Number nextNumber = new Number();

        if(lastNumber.getFullNumber().equals("Х999ХХ 116 RUS")){
            return "В этом регионе номера закончились :(";
        }else {
            nextNumber.setNumbers(generateNextNumbers(lastNumber.getNumbers()));
            if (Arrays.toString(nextNumber.getNumbers()).equals("000")){
                nextNumber.setLetters(generateNextLetters(lastNumber.getLetters()));
            }else {
                nextNumber.setLetters(lastNumber.getLetters());
            }

            nextNumber.setFullNumber(buildFullNumber(nextNumber.getNumbers(), nextNumber.getLetters()));
            numberRepository.save(nextNumber);
            return nextNumber.getFullNumber();
        }
    }

    @Override
    public String getRandomNumber() {
        Number randomNumber;
        while (true){
            randomNumber = generateRandomNumber();
            if (!numberRepository.existsByFullNumber(randomNumber.getFullNumber())){
                break;
            }
        }
        numberRepository.save(randomNumber);
        return randomNumber.getFullNumber();
    }

    @Override
    public Number generateRandomNumber() {
        Random random = new Random();
        Character[] numbers = new Character[3];
        Character[] letters = new Character[3];

        for (int i = 0; i < 3; i++){
            numbers[i] = Character.forDigit(random.nextInt(10), 10);
            letters[i] =  Number.VALID_LETTERS.get(random.nextInt(Number.VALID_LETTERS.size()));
        }

        return Number.builder()
                .numbers(numbers)
                .letters(letters)
                .fullNumber(buildFullNumber(numbers, letters))
                .build();
    }

    public Character[] generateNextNumbers(Character[] numbers){
        if(!Arrays.toString(numbers).equals("999")){

            if(numbers[2] != '9'){
                numbers[2] =(char) (numbers[2] + 1);
            }else {
                numbers[2] = '0';
                if(numbers[1]!='9'){
                    numbers[1] =(char) (numbers[1] + 1);
                }else {
                    numbers[1] = '0';
                    numbers[0] =(char)  (numbers[0] + 1);
                }
            }
        }else {
            numbers = new Character[]{'0', '0', '0'};
        }
        return numbers;
    }

    public Character[] generateNextLetters(Character[] letters){
        if(!String.valueOf(letters).equals("ХХХ")){
            if(!letters[2].equals(Number.VALID_LETTERS.get(Number.VALID_LETTERS.size() - 1))){
                letters[2] = Number.VALID_LETTERS.get(Number.VALID_LETTERS.indexOf(letters[2]) + 1);
            }else {
                letters[2] = Number.VALID_LETTERS.get(0);
                if(!letters[1].equals(Number.VALID_LETTERS.get(Number.VALID_LETTERS.size() - 1))){
                    letters[1] = Number.VALID_LETTERS.get(Number.VALID_LETTERS.indexOf(letters[1]) + 1);
                }else {
                    letters[1] = Number.VALID_LETTERS.get(0);
                    letters[0] = Number.VALID_LETTERS.get(Number.VALID_LETTERS.indexOf(letters[0]) + 1);
                }
            }
        }else {
            letters = null;
        }
        return letters;

    }

    @Override
    public String buildFullNumber(Character[] numbers, Character[] letters) {
        return "" + letters[0] + numbers[0] + numbers[1] + numbers[2] + letters[1] + letters[2] + " " + Number.RUS116;
    }
}
