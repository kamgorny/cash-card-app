package example.cashcardapp;


import example.cashcardapp.model.CashCard;
import org.assertj.core.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class CashCardJsonTest
{

    @Autowired
    private JacksonTester<CashCard> json;

    @Autowired
    private JacksonTester<CashCard[]> jsonList;

    private CashCard[] cashCards;


    @BeforeEach
    void setUp()
    {
        cashCards = Arrays.array(
                new CashCard(99L, 123.45, "jack1"),
                new CashCard(21L, 37.69, "jack1"),
                new CashCard(101L, 150.00, "jack1"));
    }

    @Test
    void cashCardSerializationTest() throws IOException
    {
        CashCard cashCard = new CashCard(99L, 123.45, "jack1");
        assertThat(json.write(cashCard)).isStrictlyEqualToJson("single.json");
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.id");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.id")
                .isEqualTo(99);
        assertThat(json.write(cashCard)).hasJsonPathNumberValue("@.amount");
        assertThat(json.write(cashCard)).extractingJsonPathNumberValue("@.amount")
                .isEqualTo(123.45);
        assertThat(json.write(cashCard)).hasJsonPathStringValue("@.owner");
        assertThat(json.write(cashCard)).extractingJsonPathStringValue("@.owner")
                .isEqualTo("jack1");
    }

    @Test
    void cashCardDeserializationTest() throws IOException
    {
        String expected = """
                {
                    "id": 99,
                    "amount": 123.45,
                    "owner": "jack1"
                }
                """;
        assertThat(json.parse(expected))
                .isEqualTo(new CashCard(99L, 123.45, "jack1"));
        assertThat(json.parseObject(expected).id()).isEqualTo(99);
        assertThat(json.parseObject(expected).amount()).isEqualTo(123.45);
    }

    @Test
    void cashCardListSerializationTest() throws IOException
    {
        assertThat(jsonList.write(cashCards)).isStrictlyEqualToJson("list.json");
    }

    @Test
    void cashCardListDeserializationTest() throws IOException {
        String expected="""
         [
              { "id": 99, "amount": 123.45 , "owner": "jack1"},
              { "id": 21, "amount": 37.69, "owner": "jack1" },
              { "id": 101, "amount": 150.00, "owner": "jack1" }
         ]
         """;
        assertThat(jsonList.parse(expected)).isEqualTo(cashCards);
    }
}
