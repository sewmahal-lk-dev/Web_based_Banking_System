package com.bank.onlinebankingsystem.service.impl;

import com.bank.onlinebankingsystem.entity.Card;
import com.bank.onlinebankingsystem.repository.CardRepository;
import com.bank.onlinebankingsystem.service.CardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CardServiceImpl implements CardService {

    @Autowired
    private CardRepository cardRepository;

    @Override
    public Card createCard(Card card) {
        return cardRepository.save(card);
    }

    @Override
    public List<Card> getCardsByUserId(Long userId) {
        return cardRepository.findByUserId(userId);
    }
}