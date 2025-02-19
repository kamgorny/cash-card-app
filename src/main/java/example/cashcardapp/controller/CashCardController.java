package example.cashcardapp.controller;

import example.cashcardapp.model.CashCard;
import example.cashcardapp.repository.CashCardRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/cashcards")
class CashCardController
{
    private final CashCardRepository cashCardRepository;

    CashCardController(CashCardRepository cashCardRepository)
    {
        this.cashCardRepository = cashCardRepository;
    }

    @GetMapping("/{requestedId}")
    private ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal)
    {
        CashCard cashCard = findCashCard(requestedId, principal);

        if (cashCard != null)
        {
            return ResponseEntity.ok(cashCard);
        } else
        {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping()
    private ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal)
    {
        Page<CashCard> page = cashCardRepository.findByOwner(principal.getName(),
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))));

        return ResponseEntity.ok(page.getContent());
    }

    @PostMapping
    private ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal)
    {
        CashCard savedCashCard = cashCardRepository.save(new CashCard(null, newCashCardRequest.amount(), principal.getName()));
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();

        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    private ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardUpdate, Principal principal)
    {
        CashCard foundCashCard = findCashCard(requestedId, principal);
        if(foundCashCard != null)
        {
            cashCardRepository.save(new CashCard(foundCashCard.id(), cashCardUpdate.amount(), principal.getName()));
            return ResponseEntity.noContent().build();
        }
        else
        {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    private ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal)
    {
        if(cashCardRepository.existsByIdAndOwner(id, principal.getName()))
        {
            cashCardRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        }
        return  ResponseEntity.notFound().build();
    }

    private CashCard findCashCard(Long requestedId, Principal principal)
    {
        return cashCardRepository.findByIdAndOwner(requestedId, principal.getName());
    }
}