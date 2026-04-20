CREATE TABLE IF NOT EXISTS recipes (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR(200) NOT NULL,
    cook_time   INTEGER      NOT NULL CHECK (cook_time > 0),
    ingredients TEXT         NOT NULL,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO recipes (name, cook_time, ingredients) VALUES
('Борщ классический', 120,
 'свёкла, капуста, картофель, морковь, лук, томатная паста, говядина, соль, перец'),
('Яичница с помидорами', 15,
 'яйца, помидоры, лук, масло растительное, соль, перец'),
('Ризотто с грибами', 45,
 'рис арборио, грибы шампиньоны, лук, чеснок, белое вино, бульон, пармезан, масло сливочное'),
('Паста карбонара', 25,
 'спагетти, бекон, яйца, пармезан, чеснок, соль, перец'),
('Греческий салат', 10,
 'огурцы, помидоры, перец болгарский, маслины, сыр фета, лук, оливковое масло');
