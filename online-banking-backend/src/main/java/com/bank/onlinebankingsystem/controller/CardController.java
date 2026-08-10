package com.bank.onlinebankingsystem.controller;

import com.bank.onlinebankingsystem.entity.Card;
import com.bank.onlinebankingsystem.repository.CardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "*", allowedHeaders = "*")
// React Frontend එකට Access දීමට
public class CardController {

    @Autowired
    private CardRepository cardRepository;

    // 1. Get All Cards
    @GetMapping
    public List<Card> getAllCards() {
        return cardRepository.findAll();
    }

    // 2. Customer: Submit Block Request with Reason
    @PutMapping("/{id}/block-request")
    public Card requestBlock(@PathVariable Long id, @RequestBody Map<String, String> body) {
        Card card = cardRepository.findById(id).orElseThrow();
        card.setPendingReq("Block Requested");
        card.setBlockReason(body.get("blockReason"));
        return cardRepository.save(card);
    }

    // 3. Customer: Submit Unblock Request
    @PutMapping("/{id}/unblock-request")
    public Card requestUnblock(@PathVariable Long id) {
        Card card = cardRepository.findById(id).orElseThrow();
        card.setPendingReq("Unblock Requested");
        return cardRepository.save(card);
    }

    // 4. Officer: Toggle Block / Unblock Approval
    @PutMapping("/{id}/toggle-block")
    public Card toggleBlock(@PathVariable Long id) {
        Card card = cardRepository.findById(id).orElseThrow();

        if ("Blocked".equalsIgnoreCase(card.getStatus())) {
            card.setStatus("Active");
            card.setBlockReason(""); // Unblock කළ පසු Reason එක අයින් වේ
        } else {
            card.setStatus("Blocked");
        }

        card.setPendingReq("None");
        return cardRepository.save(card);
    }

    // 5. Apply / Issue New Card
    @PostMapping
    public Card createCard(@RequestBody Card card) {
        if (card.getStatus() == null) card.setStatus("Pending Approval");
        if (card.getPendingReq() == null) card.setPendingReq("New Card Request");
        return cardRepository.save(card);
    }
}