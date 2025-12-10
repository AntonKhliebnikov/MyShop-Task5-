-- 3.1 Вывести все записи из таблицы Users.
SELECT * 
FROM users;

-- 3.2	Вывести список пользователей, у которых username содержит букву "A".
SELECT *
FROM users
WHERE username LIKE '%a%';

-- 3.3	Вывести информацию о продуктах с ценой выше 100.
SELECT *
FROM products
WHERE price > 100;

-- 3.4	Вывести пользователей, у которых отсутствуют дополнительные данные (User_Details).
SELECT u.*
FROM users u
LEFT JOIN user_details ud 
	ON u.id = ud.user_id
WHERE ud.user_id IS NULL;

-- 3.5	Вывести список продуктов и количество пользователей, добавивших их в корзину.
SELECT 
	p.id, 
	p.product_name,
	COUNT(DISTINCT sc.user_id) AS users_count
FROM products p
LEFT JOIN shopping_cart sc 
	ON p.id = sc.product_id
GROUP BY p.id, p.product_name
ORDER BY users_count DESC;

-- 3.6	Найдите общую сумму заказов для каждого пользователя.
SELECT 
	u.id, 
	u.username,
	SUM(o.total_amount) AS total_orders_amount
FROM users u
LEFT JOIN orders o 
	ON u.id = o.user_id
GROUP BY u.id, u.username;

-- 3.7	Вывести список продуктов, которые имеются в корзине пользователя (по id).
SELECT 
	p.id, 
	p.product_name, 
	p.price, 
	sc.quantity
FROM products p
LEFT JOIN shopping_cart sc 
	ON p.id = sc.product_id
WHERE sc.user_id = 3;

-- 3.8	Вывести список пользователей, у которых есть заказы на сумму более 500.
SELECT 
	u.id, 
	u.username, 
	SUM(o.total_amount) AS total_orders_amount
FROM users u
JOIN orders o 
	ON u.id = o.user_id
GROUP BY u.id, u.username
HAVING SUM(o.total_amount) > 500;

-- 3.9	Вывести пользователя, который купил больше всего товаров.
SELECT 
	u.id, 
	u.username,
	SUM(oi.quantity) AS ordered_products
FROM users u
JOIN orders o 
	ON u.id = o.user_id
JOIN order_items oi 
	ON o.id = oi.order_id
GROUP BY u.id, u.username
ORDER BY ordered_products DESC
LIMIT 1;

-- 3.10	Вывести список 10-ти самых дорогих товаров.
SELECT *
FROM products p
ORDER BY price DESC
LIMIT 10;

-- 3.11 Вывести список товаров с ценой выше средней.
SELECT *
FROM products
WHERE price > (
	SELECT 
		AVG(price) 
	FROM products
	);

-- 3.12	Вывести список пользователей,
-- у которых суммарная стоимость продуктов в корзине
-- превышает среднюю стоимость продуктов в корзине всех пользователей.

WITH user_cart_totals AS (
    SELECT
        sc.user_id,
        SUM(p.price * sc.quantity) AS cart_total
    FROM shopping_cart sc
    JOIN products p 
    	ON p.id = sc.product_id
    GROUP BY sc.user_id
)
SELECT
    u.id,
    u.username,
    uct.cart_total
FROM users u
JOIN user_cart_totals uct 
	ON u.id = uct.user_id
WHERE uct.cart_total > (
	SELECT 
		AVG(cart_total) 
	FROM user_cart_totals
	);

-- 3.13	Вывести пользователей, у которых все продукты в корзине имеют цену выше 100.
SELECT
	u.id,
	u.username
FROM users u
WHERE EXISTS (
	SELECT 1
	FROM shopping_cart sc
	WHERE u.id = sc.user_id
)
AND NOT EXISTS (
	SELECT 1
	FROM shopping_cart sc
	JOIN products p 
		ON p.id = sc.product_id
	WHERE u.id = sc.user_id 
		AND p.price <= 100
);

-- 3.14	Вывести список продуктов, которые есть в корзине у всех пользователей.
SELECT 
	p.id, 
	p.product_name
FROM products p
WHERE EXISTS (
	SELECT 1
	FROM shopping_cart sc
	WHERE p.id = sc.product_id
)
AND NOT EXISTS (
	SELECT 1
	FROM users u
	WHERE NOT EXISTS (
		SELECT 1
		FROM shopping_cart sc2
		WHERE u.id = sc2.user_id
		AND p.id = sc2.product_id
	)
);

-- 3.15	Вывести информацию о пользователях, 
-- у которых в корзине присутствуют продукты с общим количеством более 10 единиц.
SELECT
	u.id,
	u.username,
	SUM(sc.quantity) AS total_quantity
FROM users u
JOIN shopping_cart sc 
	ON u.id = sc.user_id
GROUP BY u.id, u.username
HAVING SUM(sc.quantity) > 3;

-- 3.16	Вывести пользователя, 
-- у которого сумма всех заказов превышает сумму заказов любого другого пользователя.
SELECT
	u.id,
	u.username,
	SUM(o.total_amount) AS total_orders_amount
FROM users u
JOIN orders o 
	ON u.id = o.user_id
GROUP BY u.id, u.username
HAVING SUM(o.total_amount) >= ALL (
	SELECT SUM(o2.total_amount) 
	FROM orders o2
	GROUP BY o2.user_id
);

-- 3.17	Вывести список пользователей, 
-- у которых количество продуктов в корзине
-- превышает среднее количество продуктов в корзине всех пользователей.

WITH user_cart_products_counts AS (
    SELECT
        sc.user_id,
        SUM(sc.quantity) AS products_count
    FROM shopping_cart sc
    GROUP BY sc.user_id
)
SELECT
    u.id,
    u.username,
    ucpc.products_count
FROM users u
JOIN user_cart_products_counts ucpc 
	ON u.id = ucpc.user_id
WHERE ucpc.products_count > (
	SELECT AVG(products_count) 
	FROM user_cart_products_counts
	);

-- 3.18	Вывести продукты, которые есть в корзине только одного пользователя.
SELECT
	p.id,
	p.product_name
FROM products p
JOIN shopping_cart sc 
	ON p.id = sc. product_id
GROUP BY p.id, p.product_name
HAVING COUNT(DISTINCT sc.user_id) = 1;

-- 3.19	Вывести пользователей, 
-- у которых суммарная стоимость заказов превышает 1000, и количество заказов более 3.
SELECT
	u.id,
	u.username,
	SUM(o.total_amount) AS total_orders_amount,
	COUNT(o.id) AS orders_count
FROM users u
JOIN orders o 
	ON u.id = o.user_id
GROUP BY u.id, u.username
HAVING SUM(o.total_amount) > 1000
	AND COUNT(o.id) > 3;
	
-- 3.20	Вывести информацию о продукте 
-- с наибольшей суммарной стоимостью в корзинах пользователей.
SELECT 
	p.id,
	p.product_name,
	SUM(p.price * sc.quantity) AS total_value_in_carts
FROM products p
JOIN shopping_cart sc 
	ON p.id = sc.product_id
GROUP BY p.id, p.product_name
ORDER BY total_value_in_carts DESC
LIMIT 1;