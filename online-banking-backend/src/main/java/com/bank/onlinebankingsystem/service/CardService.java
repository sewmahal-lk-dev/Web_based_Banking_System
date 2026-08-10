package com.bank.onlinebankingsystem.service;

import com.bank.onlinebankingsystem.entity.Card;
import java.util.List;

public interface CardService {
    Card createCard(Card card);
    List<Card> getCardsByUserId(Long userId);
}