begin;

-- The shared Supabase project was used by an earlier prototype before ERD3.
-- Preserve that schema (including its sample rows) instead of dropping it.
-- A fresh installation has no public.user_account marker, so this is a no-op.
do $$
declare
  legacy_table text;
begin
  if to_regclass('public.user_account') is null
     or to_regclass('public.app_user') is not null then
    return;
  end if;

  create schema if not exists legacy_pre_erd3;
  revoke all on schema legacy_pre_erd3 from public, anon, authenticated, service_role;

  foreach legacy_table in array array[
    'group_rental_request',
    'laundry_inspection',
    'note',
    'product',
    'product_variant',
    'recommendation_job',
    'rental_order',
    'saved_outfit',
    'user_account',
    'wardrobe_item'
  ]
  loop
    if to_regclass(format('public.%I', legacy_table)) is not null then
      execute format(
        'alter table public.%I set schema legacy_pre_erd3',
        legacy_table
      );
    end if;
  end loop;
end;
$$;

commit;
