-- Añadir columna difficulty a la tabla flashcards
ALTER TABLE flashcards ADD COLUMN difficulty VARCHAR(255);

-- Actualizar flashcards existentes con un valor por defecto
UPDATE flashcards SET difficulty = 'MEDIUM' WHERE difficulty IS NULL; 