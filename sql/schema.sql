-- For a clean future installation. User already created this table manually.
CREATE TABLE IF NOT EXISTS public.orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    size VARCHAR(10) NOT NULL,
    base VARCHAR(50) NOT NULL,
    sauce VARCHAR(20) NOT NULL,
    cheese VARCHAR(30) NOT NULL,
    toppings JSONB NOT NULL,
    extra_topping_charge_gbp INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
