CREATE TABLE users (
	id BIGSERIAL PRIMARY KEY,
	username VARCHAR(50) NOT NULL UNIQUE,
	email VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE user_details (
	user_id BIGINT PRIMARY KEY REFERENCES users(id) ON DELETE CASCADE,
	first_name VARCHAR(50),
	last_name VARCHAR(50),
	address TEXT,
	phone VARCHAR(20)
);

CREATE TABLE products (
	id BIGSERIAL PRIMARY KEY,
	product_name VARCHAR(100),
	price NUMERIC(10,2) NOT NULL
);

CREATE TABLE shopping_cart (
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
	quantity INTEGER NOT NULL DEFAULT 1,
	PRIMARY KEY (user_id, product_id)
);

CREATE TABLE orders (
	id BIGSERIAL PRIMARY KEY,
	user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
	ordered_products TEXT,
	total_amount NUMERIC(10,2) NOT NULL
);

CREATE TABLE order_items (
	order_id BIGINT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
	product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
	quantity INTEGER NOT NULL DEFAULT 1,
	price NUMERIC(10,2),
	PRIMARY KEY (order_id, product_id)
);