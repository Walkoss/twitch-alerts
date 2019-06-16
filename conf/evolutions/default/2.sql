-- Tips schema

-- !Ups

CREATE TABLE tip
(
  id      INTEGER PRIMARY KEY AUTOINCREMENT,
  amount  REAL    NOT NULL,
  user_id INTEGER NOT NULL,
  FOREIGN KEY (user_id) REFERENCES user(id)
);

-- !Downs

DROP TABLE tip;