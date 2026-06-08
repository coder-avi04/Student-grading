CREATE TABLE students (
  id SERIAL PRIMARY KEY,
  name TEXT NOT NULL,
  subject1 INTEGER NOT NULL,
  subject2 INTEGER NOT NULL,
  subject3 INTEGER NOT NULL,
  subject4 INTEGER NOT NULL,
  average REAL NOT NULL,
  grade TEXT NOT NULL,
  performance_message TEXT NOT NULL,
  created_at TIMESTAMP DEFAULT NOW()
);
