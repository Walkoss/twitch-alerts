-- Tips schema

-- !Ups

CREATE TABLE giveaway
(
  id                  INTEGER PRIMARY KEY AUTOINCREMENT,
  name                VARCHAR(255) NOT NULL,
  is_subscribers_only BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE giveaway_registration
(
  giveaway_id INTEGER NOT NULL,
  user_id     INTEGER NOT NULL,
  FOREIGN KEY (giveaway_id) REFERENCES giveaway (id),
  FOREIGN KEY (user_id) REFERENCES user (id)
);

-- !Downs

DROP TABLE giveaway_registration;
DROP TABLE giveaway;
