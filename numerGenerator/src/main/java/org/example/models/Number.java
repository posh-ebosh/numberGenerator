package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
@Table(name = "numbers")
public class Number {

    public static final String RUS116 = "116 RUS";
    public static final List<Character> VALID_LETTERS = new ArrayList<>(List.of('А', 'В', 'Е', 'К', 'М', 'Н', 'О', 'Р', 'С', 'Т', 'У', 'Х'));
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Character[] letters;

    private Character[] numbers;
    private String fullNumber;


}
