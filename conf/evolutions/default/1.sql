-- Users schema

-- !Ups

CREATE TABLE user
(
  id             INTEGER PRIMARY KEY AUTOINCREMENT,
  username       VARCHAR(255) NOT NULL,
  is_subscribed  BOOLEAN      NOT NULL DEFAULT FALSE,
  is_blacklisted BOOLEAN      NOT NULL DEFAULT FALSE
);

-- !Downs

DROP TABLE user;