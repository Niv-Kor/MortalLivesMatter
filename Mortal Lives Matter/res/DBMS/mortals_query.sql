SELECT *
FROM mortals;

-- create a new mortal
INSERT INTO mortals (name, type, size, playable)
VALUES ('Orc 6', 'ground', 'M', false);

-- insert mortal sprites
INSERT INTO sprites(path, grid_width, grid_height, sprites.rows, sprites.columns,
					row_1, row_2, row_3, row_4, row_5, row_6, row_7, row_8,
                    row_9, row_10, row_11, row_12)
VALUES (
     '/Sprites/Enemies/Desert/1/Zombie_3/Sprite_sheet_180x180.png',
     34, 36, 1, 1,
     1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
);

INSERT INTO mortal_graphics(mortal_id, sprite_id)
VALUES(
	(SELECT id
    FROM mortals
    WHERE name = 'Zombie 3'),
    (SELECT id
    FROM sprites
    WHERE path = '/Sprites/Enemies/Desert/1/Zombie_3/Sprite_sheet_180x180.png')
);

-- update a sprite
UPDATE sprites
SET grid_width = 512, grid_height = 512
WHERE sprites.id = (SELECT mortal_graphics.sprite_id
					FROM mortal_graphics
					WHERE mortal_graphics.mortal_id = (SELECT mortals.id
													   FROM mortals
													   WHERE mortals.name = 'Gatekeeper 1'));
                                                       
-- ןinsert mortal sound
INSERT INTO sound(type, description, path, loopable)
VALUES('SFX', 'M-move', '/Sound/Player/SFX/RunningStep.wav', false);

INSERT INTO mortal_sound(mortal_id, sound_id)
VALUES(
	(SELECT id
	 FROM mortals
     WHERE name = 'Astrid'),
	(SELECT id
     FROM sound
     WHERE path = '/Sound/Player/SFX/Step.wav')
);

-- insert immune system entry
INSERT INTO immune_system(mortal_id, immunity)
VALUES((SELECT id
		FROM mortals
		WHERE name = 'Zombie 3'), 'Poison');

-- show mortals and their sounds
SELECT m.name AS name, s.description AS type, s.path AS sound
FROM mortal_sound ms
INNER JOIN mortals m ON m.id = ms.mortal_id
INNER JOIN sound s ON s.id = ms.sound_id
ORDER BY name, type, sound;

-- show sprite
SELECT s.path
FROM mortal_graphics mg
INNER JOIN mortals m ON m.id = mg.mortal_id
INNER JOIN sprites s ON s.id = mg.sprite_id
WHERE m.name = 'Tyler';

SELECT m.name, isy.immunity
FROM immune_system isy
INNER JOIN mortals m ON m.id = isy.mortal_id;

UPDATE mortals
SET element_1 = LOWER(element_1),
element_2 = LOWER(element_2),
element_3 = LOWER(element_3);

UPDATE mortals
SET stats_offense = stats_offense + 10
WHERE playable = false;