use hbg13;

set foreign_key_checks = 0;
DROP table if exists K_customers;
DROP table if exists K_cookies;
DROP table if exists K_pallets;
DROP table if exists K_orders;
DROP table if exists K_order_spec;
DROP table if exists K_rawmaterials;
DROP table if exists K_recipes;
DROP table if exists K_updates;
set foreign_key_checks = 1;


Create table K_customers (
    name varchar(255),
    address varchar(255),
    primary key (name)
);

Create table K_cookies (
    cookieName varchar (255),
    primary key (cookieName)
);

Create table K_pallets (
    palletID int AUTO_INCREMENT,
    cookies_cookieName varchar(255),
    production_date date,
    delivery_date date,
    address varchar(255),
    packed date,
    blocked bit,
    primary key (palletId),
    foreign key (cookies_cookieName) references K_cookies(cookieName)
);

Create table K_orders (
    orderID int AUTO_INCREMENT,
    pallets_palletID int,
    customers_name varchar(255),
    primary key (orderID),
    foreign key (pallets_palletID) references K_pallets(palletID),
    foreign key (customers_name) references K_customers(name)
);

Create table K_order_spec (
    amount int,
    cookies_cookieName varchar(255),
    orders_orderID int,
    foreign key (cookies_cookieName) references K_cookies(cookieName),
    foreign key (orders_orderID) references K_orders(orderID)
);

Create table K_rawmaterials (
    name varchar(255),
    amount int,
    unit varchar(255),
    last_time_Used datetime,
    primary key (name)
);

Create table K_recipes (
    cookies_cookieName varchar(255),
    rawmaterials_name varchar(255),
    amount int,
    unit varchar(255),
    foreign key (cookies_cookieName) references K_cookies(cookieName),
    foreign key  (rawmaterials_name) references K_rawmaterials(name)
);

Create table K_updates (
    id int AUTO_INCREMENT,
    rawmaterials_name varchar(255),
    amount int,
    primary key (id),
    foreign key (rawmaterials_name) references K_rawmaterials(name)
);

INSERT INTO K_customers(name, address)
 VALUES 
 ('Småbröd AB', 'Malmö'),
 ('Kaffebröd AB', 'Landskrona'),
 ('Bjudkakor AB', 'Ystad'),
 ('Kalaskakor AB', 'Trelleborg'),
 ('Partykakor AB', 'Kristianstad'),
 ('Gästkakor AB', 'Hässleholm'),
 ('Skånekakor AB', 'Perstorp'),
 ('Finkakor AB','Helsingborg');

INSERT INTO K_cookies(cookieName)
VALUES
('Almond delight'),
('Amneris'),
('Berliner'),
('Nut cookie'),
('Nut ring'),
('Tango');

INSERT INTO K_rawmaterials(name, amount, unit)
VALUES
('Bread crumbs','500000','g'),
('Butter', '500000', 'g'),
('Chocolate', '500000', 'g'),
('Chopped almonds', '500000', 'g'),
('Cinnamon', '500000', 'g'),
('Egg whites', '500000', 'ml'),
('Eggs', '500000', 'g'),
('Fine-ground nuts', '500000', 'g'),
('Flour', '500000', 'g'),
('Ground, roasted nuts', '500000', 'g'),
('Icing sugar', '500000', 'g'),
('Marzipan', 500000, 'g'),
('Potato starch', 500000, 'g'),
('Roasted, chopped nuts', 500000, 'g'),
('Sodium bicarbonate', 500000, 'g'),
('Sugar', 500000, 'g'),
('Vanilla sugar', 500000, 'g'),
('Vanilla', 500000, 'g'),
('Wheat flour', 500000, 'g');


INSERT INTO K_recipes(cookies_cookieName, rawmaterials_name, amount, unit)
VALUES
('Almond delight', 'Butter', '400', 'g'),
('Almond delight', 'Chopped almonds', '279', 'g'),
('Almond delight', 'Cinnamon', '10', 'g'),
('Almond delight', 'Flour', '400', 'g'),
('Almond delight', 'Sugar', '270', 'g'),
('Amneris', 'Butter', '250', 'g'),
('Amneris', 'Eggs', '250', 'g'),
('Amneris', 'Marzipan', '750', 'g'),
('Amneris', 'Potato starch', '25', 'g'),
('Amneris', 'Wheat flour', '25', 'g'),
('Berliner', 'Butter', '250', 'g'),
('Berliner', 'Chocolate', '50', 'g'),
('Berliner', 'Eggs', '50', 'g'),
('Berliner', 'Flour', '350', 'g'),
('Berliner', 'Icing sugar', '100', 'g'),
('Berliner', 'Vanilla sugar', '5', 'g'),
('Nut cookie', 'Bread crumbs', '125', 'g'),
('Nut cookie', 'Chocolate', '50', 'g'),
('Nut cookie', 'Egg whites', '350', 'ml'),
('Nut cookie', 'Fine-ground nuts', '750', 'g'),
('Nut cookie', 'Ground, roasted nuts', '625', 'g'),
('Nut cookie', 'Sugar', '375', 'g'),
('Nut ring', 'Butter', '450', 'g'),
('Nut ring', 'Flour', '450', 'g'),
('Nut ring', 'Icing sugar', '190', 'g'),
('Nut ring', 'Roasted, chopped nuts', '225', 'g'),
('Tango', 'Butter', '200', 'g'),
('Tango', 'Flour', '300', 'g'),
('Tango', 'Sodium bicarbonate', '4', 'g'),
('Tango', 'Sugar', '250', 'g'),
('Tango', 'Vanilla', '2', 'g');

