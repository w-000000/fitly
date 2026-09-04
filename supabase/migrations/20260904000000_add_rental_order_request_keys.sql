begin;

alter table public.rental_order
  add column if not exists idempotency_key varchar(100),
  add column if not exists order_group_key varchar(100),
  add column if not exists multi_item_order boolean not null default false;

create index if not exists rental_order_idempotency_idx
  on public.rental_order (user_id, idempotency_key)
  where idempotency_key is not null;

create index if not exists rental_order_group_key_idx
  on public.rental_order (order_group_key)
  where order_group_key is not null;

commit;
