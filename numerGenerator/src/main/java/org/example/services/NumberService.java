package org.example.services;

import org.example.models.Number;

public interface NumberService {
    String getRandomNumber();

    Number generateRandomNumber();

    String getNextNumber();

    String buildFullNumber(Character[] numbers, Character[] letters);
}
