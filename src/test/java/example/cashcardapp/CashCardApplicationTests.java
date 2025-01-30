package example.cashcardapp;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import example.cashcardapp.model.CashCard;
import net.minidev.json.JSONArray;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CashCardApplicationTests
{

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void findById_shouldReturnACashCardWhenDataIsSaved()
	{
		//given & when
		ResponseEntity<String> response = restTemplate.withBasicAuth("jack1", "abc123").getForEntity("/cashcards/99", String.class);

		//then
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		String owner = documentContext.read("$.owner");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(123.45);
		assertThat(owner).isEqualTo("jack1");
	}

	@Test
	void findAll_shouldReturnAPageOfCashCards()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards?page=0&size=1", String.class);

		//then
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(page.size()).isEqualTo(1);
	}

	@Test
	void findAll_shouldReturnASortedPageOfCashCards()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards?page=0&size=1&sort=amount,desc", String.class);

		//then
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray read = documentContext.read("$[*]");
		double amount = documentContext.read("$[0].amount");
		String owner = documentContext.read("$[0].owner");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(read.size()).isEqualTo(1);
		assertThat(amount).isEqualTo(150.00);
		assertThat(owner).isEqualTo("jack1");
	}

	@Test
	void findAll_shouldReturnASortedPageOfCashCardsWithNoParametersAndUseDefaultValues()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards", String.class);

		//then

		DocumentContext documentContext = JsonPath.parse(response.getBody());
		JSONArray page = documentContext.read("$[*]");
		JSONArray amounts = documentContext.read("$..amount");

		assertThat(page.size()).isEqualTo(3);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(amounts).containsExactly(37.69, 123.45, 150.00);
	}

	@Test
	void findAll_shouldReturnAllCashCardsWhenListIsRequested()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards", String.class);


		//then
		DocumentContext documentContext = JsonPath.parse(response.getBody());
		int cashCardCount = documentContext.read("$.length()");
		JSONArray ids = documentContext.read("$..id");
		JSONArray amounts = documentContext.read("$..amount");

		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(cashCardCount).isEqualTo(3);
		assertThat(ids).containsExactlyInAnyOrder(99, 21, 101);
		assertThat(amounts).containsExactlyInAnyOrder(123.45, 37.69, 150.00);
	}

	@Test
	@DirtiesContext
	void createCashCard_shouldCreateANewCashCard()
	{
		//given & when
		CashCard newCashCard = new CashCard(null, 250.00, null);
		ResponseEntity<Void> createResponse = restTemplate
				.withBasicAuth("jack1", "abc123")
				.postForEntity("/cashcards", newCashCard, Void.class);
		URI locationOfNewCashCard = createResponse.getHeaders().getLocation();
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity(locationOfNewCashCard, String.class);


		//then
		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");

		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
		assertThat(id).isNotNull();
		assertThat(amount).isEqualTo(250.00);
	}

	@Test
	@DirtiesContext
	void putCashCard_shouldUpdateAnExistingCashCard()
	{
		//given
		CashCard cashCardUpdate = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(cashCardUpdate);

		//when
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.exchange("/cashcards/99", HttpMethod.PUT, request, Void.class);


		//then
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards/99", String.class);

		DocumentContext documentContext = JsonPath.parse(getResponse.getBody());
		Number id = documentContext.read("$.id");
		Double amount = documentContext.read("$.amount");
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
		assertThat(id).isEqualTo(99);
		assertThat(amount).isEqualTo(19.99);
	}

	@Test
	@DirtiesContext
	void shouldDeleteAnExistingCashCard()
	{
		//given & when
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.exchange("/cashcards/99", HttpMethod.DELETE, null, Void.class);

		//then
		ResponseEntity<String> getResponse = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards/99", String.class);
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
		assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotDeleteACashCardThatDoesNotExist()
	{
		//given & when
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("jack1", "abc123")
				.exchange("/cashcards/99999", HttpMethod.DELETE, null, Void.class);

		//then
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

}