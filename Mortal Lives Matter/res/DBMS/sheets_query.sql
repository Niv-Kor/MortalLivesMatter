
UPDATE sheets
SET mask_path = REPLACE(mask_path, 'sprites', 'sheets');

UPDATE sheets
SET sprite_path = '/sheets/sight/scope/Sprite_sheet_110x110.png'
WHERE sprite_path = '/sheets/sight/scope/Sprite_sheet_225x225.png';

-- insert new sheet
INSERT INTO sheets(code, width, height)
VALUES (
	'ss$a$forest_2_1', 172, 377
);

INSERT INTO masks(code, path)
VALUES (
	'ss$a$forest_2_1',
    '/maps/field/2/objects/Mask_sheet_172x377.png'
);

DELETE FROM sheets
WHERE code = 'ss$g$barrier_wall';

-- select mortal sprites
SELECT *
FROM sheets
WHERE code LIKE '%m$%';

-- select projectile sprites
SELECT *
FROM sheets
WHERE code LIKE '%p$%';

-- select loot sprites
SELECT *
FROM sheets
WHERE code LIKE '%l$%';

-- select arena sprites
SELECT *
FROM sheets
WHERE code LIKE '%a$%';

-- select general sprites
SELECT *
FROM sheets
WHERE code LIKE '%g$%';

SELECT EXISTS(SELECT code FROM sprites WHERE code = 'ss$m$tyler') AS 'exists';

SELECT *
FROM sheets s
WHERE s.mask_path IS NOT NULL
AND s.code NOT IN (SELECT m.code
				   FROM masks m);
                   
SELECT *
FROM masks;

UPDATE masks
SET path = REPLACE(path, 'Sprite_sheet_53x241', 'Mask_sheet_53x241.png')
WHERE code LIKE '%barrier%';