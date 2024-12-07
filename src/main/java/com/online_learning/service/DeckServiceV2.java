package com.online_learning.service;


import com.online_learning.dao.CardDao;
import com.online_learning.dao.DeckDao;
import com.online_learning.dto.deckv2.*;
import com.online_learning.entity.Card;
import com.online_learning.entity.Deck;
import com.online_learning.entity.User;
import com.online_learning.util.Helper;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.A;
import org.hibernate.mapping.Join;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DeckServiceV2 {
    private final DeckDao deckRepository;
    private final CardDao cardRepository;
    private final Helper helper;
    @Transactional
    public void createDeckV2(CreateDeck createDeck) {


        User user = helper.getUser();
        Deck deck = new Deck();
        deck.setName(createDeck.getName());
        deck.setDescription(createDeck.getDescription());
        deck.setIsPublic(createDeck.getIsPublic());
        deck.setConfigLanguage(createDeck.getConfigLanguage());
        deck.setQuantityClones(0);
        deck.setUser(user);

        List<Card> cards = createDeck.getCards().stream().map(createCard -> {
            System.out.println(createCard.getTerm());
            Card card = Card.builder()
                    .term(createCard.getTerm())
                    .definition(createCard.getDefinition())
                    .example(createCard.getExample())
                    .image(createCard.getImage())
                    .audio(createCard.getAudio())
                    .build();
            card.setDeck(deck); // Gán Deck cho mỗi Card
            return card;
        }).collect(Collectors.toList());

        deck.setCards(cards);
        deckRepository.save(deck);
        cardRepository.saveAll(cards);
    }


    @Transactional
    public boolean updateDeckV2(UpdateDeck updateDeck) {

        Deck deck = deckRepository.findById(updateDeck.getId()).orElseThrow(() -> new RuntimeException("Deck not found"));

        deck.setName(updateDeck.getName());
        deck.setDescription(updateDeck.getDescription());
        deck.setIsPublic(updateDeck.getIsPublic());
        deck.setConfigLanguage(updateDeck.getConfigLanguage());

        deckRepository.save(deck);

        List<Long> incomingCardIds = updateDeck.getCards().stream()
                .map(UpdateCard::getId)
                .filter(Objects::nonNull) // Loại bỏ các ID null (thẻ mới)
                .collect(Collectors.toList());

        // Lấy danh sách các ID hiện tại trong Deck
        List<Long> currentCardIds = deck.getCards().stream()
                .map(Card::getId)
                .collect(Collectors.toList());

        // Tìm các ID không nằm trong danh sách được gửi xuống (các thẻ bị xóa)
        List<Long> cardIdsToDelete = currentCardIds.stream()
                .filter(id -> !incomingCardIds.contains(id))
                .collect(Collectors.toList());

        cardRepository.deleteAllById(cardIdsToDelete);

        List<UpdateCard> cardsToUpdate = updateDeck.getCards().stream()
                .filter(card -> card.getId() != null) // Các thẻ cần cập nhật
                .collect(Collectors.toList());

        List<UpdateCard> cardsToAdd = updateDeck.getCards().stream()
                .filter(card -> card.getId() == null) // Các thẻ cần thêm mới
                .collect(Collectors.toList());


        for (UpdateCard cardRequest : cardsToUpdate) {
            Card existingCard = cardRepository.findById(cardRequest.getId())
                    .orElseThrow(() -> new RuntimeException("Card not found"));
            existingCard.setTerm(cardRequest.getTerm());
            existingCard.setDefinition(cardRequest.getDefinition());
            existingCard.setExample(cardRequest.getExample());
            existingCard.setImage(cardRequest.getImage());
            existingCard.setAudio(cardRequest.getAudio());
            cardRepository.save(existingCard);
        }

        for (UpdateCard cardRequest : cardsToAdd) {
            Card newCard = new Card();
            newCard.setTerm(cardRequest.getTerm());
            newCard.setDefinition(cardRequest.getDefinition());
            newCard.setExample(cardRequest.getExample());
            newCard.setImage(cardRequest.getImage());
            newCard.setAudio(cardRequest.getAudio());
            newCard.setDeck(deck);
            cardRepository.save(newCard);
        }

        return true;
    }


    public JoinCardResponse joinCards(long id, boolean isOnlyFavorite) {
        Deck deck = deckRepository.findById(id).orElseThrow();
        JoinCardResponse joinCardResponse = new JoinCardResponse();

        List<Card> cards = isOnlyFavorite
                ? deck.getCards().stream().filter(Card::getIsFavourite).toList()
                : deck.getCards();

        // Xử lý danh sách thẻ
        List<JoinCardTerm> joinCardTerms = new ArrayList<>();
        List<JoinCardDefinition> joinCardDefinitions = new ArrayList<>();

        cards.forEach(card -> {
            String randomUUID = UUID.randomUUID().toString();

            JoinCardTerm joinCardTerm = createJoinCardTerm(card.getTerm(), randomUUID);
            JoinCardDefinition joinCardDefinition = createJoinCardDefinition(card.getDefinition(), randomUUID);

            joinCardTerms.add(joinCardTerm);
            joinCardDefinitions.add(joinCardDefinition);
        });

        joinCardResponse.setNumberCard(cards.size());
        joinCardResponse.setJoinCardTerms(joinCardTerms);
        joinCardResponse.setJoinCardDefinitions(joinCardDefinitions);

        return joinCardResponse;
    }

    // Phương thức tạo JoinCardTerm
    private JoinCardTerm createJoinCardTerm(String term, String key) {
        JoinCardTerm joinCardTerm = new JoinCardTerm();
        joinCardTerm.setTerm(term);
        joinCardTerm.setKey(key);
        return joinCardTerm;
    }

    // Phương thức tạo JoinCardDefinition
    private JoinCardDefinition createJoinCardDefinition(String definition, String key) {
        JoinCardDefinition joinCardDefinition = new JoinCardDefinition();
        joinCardDefinition.setDefinition(definition);
        joinCardDefinition.setKey(key);
        return joinCardDefinition;
    }

    // Làm bài kiểm tra
    public List<Question> generateQuiz(long id, int numberOfQuestions, String optionType) {
        int maxQuestion = 15;
        numberOfQuestions = Math.min(numberOfQuestions, maxQuestion);

        Deck deck = deckRepository.findById(id).orElseThrow();
        List<Card> cards = deck.getCards();

        // Shuffle cards để random hóa
        Collections.shuffle(cards);

        // Danh sách câu hỏi đầu ra
        List<Question> questions = new ArrayList<>();

        // Lặp qua số lượng câu hỏi yêu cầu
        for (int i = 0; i < numberOfQuestions && i < cards.size(); i++) {
            Card card = cards.get(i);

            // Tạo câu hỏi tùy theo optionType
            String questionContent;
            String correctAnswer;

            if ("TERM".equalsIgnoreCase(optionType)) {
                questionContent = card.getTerm();
                correctAnswer = card.getDefinition();
            } else if ("DEFINITION".equalsIgnoreCase(optionType)) {
                questionContent = card.getDefinition();
                correctAnswer = card.getTerm();
            } else {
                // Random chọn TERM hoặc DEFINITION khi optionType là "BOTH"
                if (new Random().nextBoolean()) {
                    questionContent = card.getTerm();
                    correctAnswer = card.getDefinition();
                } else {
                    questionContent = card.getDefinition();
                    correctAnswer = card.getTerm();
                }
            }

            // Tạo danh sách đáp án
            List<String> answers = new ArrayList<>();
            answers.add(correctAnswer);

            // Thêm 3 đáp án sai
            List<Card> otherCards = cards.stream().filter(c -> !c.equals(card)).collect(Collectors.toList());
            Collections.shuffle(otherCards);

            for (int j = 0; j < 3 && j < otherCards.size(); j++) {
                Card wrongCard = otherCards.get(j);
                String wrongAnswer = "TERM".equalsIgnoreCase(optionType) || new Random().nextBoolean()
                        ? wrongCard.getDefinition()
                        : wrongCard.getTerm();
                answers.add(wrongAnswer);
            }

            Collections.shuffle(answers);

            // Tạo câu hỏi
            Question question = new Question(questionContent, answers, correctAnswer);
            questions.add(question);
        }
        return questions;
    }


}