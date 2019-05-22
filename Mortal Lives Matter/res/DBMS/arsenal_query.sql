-- new projectile in arsenal
INSERT INTO arsenal(mortal_id, proj_name, proj_version, proj_state, )
VALUES
(
		(SELECT id
		 FROM mortals
		 WHERE name = 'Lava Boss'),
		'Earth Energy Wave', 1, 1
);

-- show mortals and their projectiles
SELECT m.name, a.proj_name AS projectile, a.proj_version AS version, a.proj_state AS state, a.default
FROM arsenal a
INNER JOIN mortals m ON m.id = a.mortal_id
ORDER BY m.name, a.proj_name, a.proj_version;

-- show mortals and their attacks
SELECT m.name, a.index, a.speed, a.strength, a.climax, a.launch_height, a.melee
FROM attacks a
INNER JOIN mortals m ON m.id = a.mortal_id
ORDER BY m.playable, m.name;

SET foreign_key_checks = 0;

UPDATE arsenal
SET proj_name = LOWER(proj_name);

SET foreign_key_checks = 1;

SELECT *
FROM arsenal;

SELECT *
FROM projectiles;

UPDATE projectiles
SET ammo = 'KIT_1'
WHERE ammo = 'arrows';

SELECT *
FROM attacks;

UPDATE attacks
SET attacks.default = true
WHERE attacks.index = 4
AND attacks.melee = true
AND attacks.mortal_id IN (SELECT id
						  FROM mortals
                          WHERE playable = true);