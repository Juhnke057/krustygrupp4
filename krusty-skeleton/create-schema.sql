SET SQL_SAFE_UPDATES = 0;
SET FOREIGN_KEY_CHECKS = 0;

DROP table if exists K_customers;
DROP table if exists K_cookies;
DROP table if exists K_pallets;
DROP table if exists K_orders;
DROP table if exists K_order_spec;
DROP table if exists K_rawmaterials;
DROP table if exists K_recipes;
DROP table if exists K_updates;

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

SET SQL_SAFE_UPDATES = 1;
SET FOREIGN_KEY_CHECKS = 1;
