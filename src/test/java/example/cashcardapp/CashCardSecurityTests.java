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
class CashCardSecurityTests
{

	@Autowired
	TestRestTemplate restTemplate;

	@Test
	void shouldNotReturnACashCardWhenUsingBadUsername()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("BAD-USER", "abc123")
				.getForEntity("/cashcards/99", String.class);

		//then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldNotReturnACachCardWhenUsingBadPassword()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("jack1", "BAD-PASSWORD")
				.getForEntity("/cashcards/99", String.class);

		//then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
	}

	@Test
	void shouldRejectUsersWhosRoleIsNonOwner()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("hank-owns-no-cards", "qrs456")
				.getForEntity("/cashcards/99", String.class);

		//then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
	}

	@Test
	void shouldNotAllowAccessToCashCardsTheyDoNotOwnEvenWithRoleCardOwner()
	{
		//given & when
		ResponseEntity<String> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.getForEntity("/cashcards/102", String.class); // dude1's data

		//then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateACashCardThatDoesNotExist()
	{
		//given & when
		CashCard unknownCard = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.exchange("/cashcards/99999", HttpMethod.PUT, request, Void.class);

		//then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotUpdateACashCardThatDoesNotBelongToUser()
	{
		//given & when
		CashCard unknownCard = new CashCard(null, 19.99, null);
		HttpEntity<CashCard> request = new HttpEntity<>(unknownCard);
		ResponseEntity<Void> response = restTemplate
				.withBasicAuth("jack1", "abc123")
				.exchange("/cashcards/102", HttpMethod.PUT, request, Void.class);

		//then
		assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}

	@Test
	void shouldNotAllowDeletionOfCashCardsTheyDoNotOwn()
	{
		//given & when
		ResponseEntity<Void> deleteResponse = restTemplate
				.withBasicAuth("jack1", "abc123")
				.exchange("/cashcards/102", HttpMethod.DELETE, null, Void.class);

		//then
		assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
	}
}