SELECT *
FROM projectiles;

ALTER TABLE proj_versions
DROP COLUMN ammo,
DROP COLUMN sight;

SELECT *
FROM proj_graphics;

UPDATE projectiles
SET aftershock = 'explosion'
WHERE name = 'Blast Arrow';

INSERT INTO proj_graphics(proj_ver_name, proj_ver_version, sprite_id)
VALUES (
	'Throwing Knife', 2,
    (SELECT id
     FROM sprites
     WHERE path = '/Sprites/Projectiles/ThrowingKnife/ver2/Sprite_sheet_82x82.png')
);

UPDATE proj_versions
SET width = 82, height = 82
WHERE proj_name LIKE 'Throwing Knife' AND version = 2;

SELECT main_type
FROM projectiles
INNER JOIN proj_versions pn ON pn.proj_name = projectiles.name
WHERE proj_name = 'Arrow' AND version = 1;

SELECT *
FROM sprites;

-- insert projectile
INSERT INTO projectiles (name, main_type, sub_type, power, speed, magazine, thrown_amount, stamina_cost)
VALUES ('Rock', 'bullet', 'aim', 10, 15, 1, 1, 5);

-- insert projectile version
INSERT INTO proj_versions(proj_name, version)
VALUES('Rock', 1);

-- insert projectile sprites
INSERT INTO sprites(path, grid_width, grid_height, sprites.rows, sprites.columns, row_1, row_2)
VALUES (
     '/Sprites/Projectiles/Rock/ver1/Sprite_sheet_50x50.png',
     50, 50, 1, 1,
     1, NULL
);

INSERT INTO proj_graphics(proj_version_id, sprite_id)
VALUES (
	(SELECT id
    FROM proj_versions
    WHERE proj_name = 'Rock' AND version = 5),
    (SELECT id
    FROM sprites
    WHERE path = '/Sprites/Elements/Rocks/5.png')
);

-- update a sprite
UPDATE sprites
SET grid_width = 700, grid_height = 700
WHERE sprites.id = (SELECT proj_graphics.sprite_id
					FROM proj_graphics
					WHERE proj_graphics.proj_version_id = (SELECT proj_versions.id
														   FROM proj_versions
														   WHERE proj_name = 'Earth Energy Wave' AND version = 1));
               
DELETE FROM proj_sound;
               
-- ןinsert projectile sound
INSERT INTO sound(type, description, path, loopable)
VALUES('SFX', 'P-hit', '/Sound/Projectile/ThrowingKnifeHit.wav', false);

INSERT INTO proj_sound(proj_name, sound_id)
VALUES(
	'Gun Bullet',
	(SELECT id
     FROM sound
     WHERE path LIKE '/Sound/Projectile/GunshotHit.wav')
);

UPDATE projectiles p
SET p.trigger = 'ignition'
WHERE p.trigger = 'ignite';

SELECT path
FROM proj_sound ps
INNER JOIN projectiles p ON p.id = ps.proj_ver_id
INNER JOIN sound s ON s.id = ps.sound_id
WHERE p.name = 'Arrow' && s.description = 'P-launch';

SELECT *
FROM proj_versions;

-- show projectiles and their sounds
SELECT ps.proj_name AS name, s.description, s.path AS sound
FROM proj_sound ps
INNER JOIN sound s ON s.id = ps.sound_id
ORDER BY name, type, sound;

UPDATE proj_versions
SET sprite_code = (SELECT code
				   FROM sprites
                   WHERE code LIKE 'ss$p$%ock_1')
WHERE proj_name LIKE '%ock' AND version = 1;

SET foreign_key_checks = 0;
UPDATE proj_sound
SET proj_name = LOWER(proj_name);
SET foreign_key_checks = 1;

UPDATE projectiles
SET aftershock = 'none'
WHERE name = 'arrow';