-- Fixtures

-- !Ups

-- Users
INSERT INTO user ("id", "username", "is_subscribed", "is_blacklisted"
)
VALUES (1, 'Walid', TRUE, FALSE),
       (2, 'Pierre', FALSE, FALSE),
       (3, 'Alexis', FALSE, TRUE),
       (4, 'Christophe', FALSE, TRUE);

-- Tips
INSERT INTO tip ("amount", "user_id"
)
VALUES (2.99, 1),
       (6.99, 1),
       (40, 2),
       (5, 3),
       (5, 3),
       (5, 3);

-- Giveaways
INSERT INTO giveaway ("id", "name", "is_subscribers_only"
)
VALUES (1, 'Logitech G Pro', FALSE),
       (2, 'PC ZT Indépendant By LDLC', TRUE),
       (3, 'CSGO knife', FALSE);

INSERT INTO giveaway_registration ("giveaway_id", "user_id"
)
VALUES (1, 1),
       (1, 2),
       (2, 1);

-- Polls

INSERT INTO poll ("id", "question", "first_choice", "second_choice"
)
VALUES (1, 'Should I play fortnite?', 'yes', 'no'),
       (2, 'CarlJR or Bren?', 'CarlJR', 'Bren');

INSERT INTO poll_vote ("poll_id", "user_id", "choice"
)
VALUES (1, 2, 'yes'),
       (1, 3, 'no'),
       (2, 3, 'Bren');

-- !Downs

DELETE
FROM user;

DELETE
FROM tip;

DELETE
FROM giveaway;

DELETE
FROM giveaway_registration;

DELETE
FROM poll;

DELETE
FROM poll_vote;