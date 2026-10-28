-- Run in pgAdmin, connected to database pizzaops.
SELECT id, size, base, sauce, cheese, toppings, extra_topping_charge_gbp, created_at
FROM public.orders
ORDER BY id;

-- Count persisted orders:
SELECT COUNT(*) AS persisted_orders FROM public.orders;

-- Count orders by size:
SELECT size, COUNT(*) AS total FROM public.orders GROUP BY size ORDER BY total DESC, size;
