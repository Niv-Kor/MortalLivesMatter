-- create a new map
INSERT INTO maps(name, habitat, min_deg, max_deg, natural_humidity)
VALUES (
	'field', 'birds', -4, 33, 30
);

-- create a new level
INSERT INTO levels(name, level, right_wall)
VALUES (
	'field', 1, 'none', 'birds', 'mess', 100000
);

-- create obstacle
INSERT INTO obstacles(map_name, level, x, y, sheet_code)
VALUES (
	'field', 4, 2274, 289, 'ss$a$field_4_1'
);

-- create waves
INSERT INTO waves(map_name, level, waves.index, min_amount, max_amount, min_times, max_times)
VALUES (
	'field', 1, 6, 3, 6, 2, 3
);

INSERT INTO spawns(map_name, level, spawns.index, mortal_id, is_minion)
VALUES (
	'field', 1, 6, (SELECT id
					FROM mortals
                    WHERE name = 'Goblin 3'), false
);

-- ןinsert map sound
INSERT INTO sound(type, description, path, loopable)
VALUES('BGM', 'A-bgm', '/Sound/Levels/Field/BGM/main.wav', true);

INSERT INTO map_sound(map_name, level, sound_id)
VALUES (
	'field', 1,
	(SELECT id
     FROM sound
     WHERE path = '/Sound/Levels/Field/BGM/main.wav')
);

INSERT INTO inclines(map_name, level, x, angle)
VALUES (
	'field', 1, 4130, -4
);

-- select inclines
SELECT i.x, i.angle
FROM inclines i
WHERE i.map_name = 'field'
AND level = 1
ORDER BY i.x ASC;


UPDATE waves
SET min_times = 2, max_times = 3;

SELECT *
FROM waves;