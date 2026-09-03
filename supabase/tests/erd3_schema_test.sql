\set ON_ERROR_STOP on

begin;

do $$
declare
  actual_count integer;
  test_user_id bigint;
  test_business_id bigint;
  test_member_id bigint;
  test_product_id bigint;
begin
  select count(*) into actual_count
  from information_schema.tables
  where table_schema = 'public'
    and table_type = 'BASE TABLE';
  if actual_count <> 24 then
    raise exception 'expected 24 public tables, got %', actual_count;
  end if;

  select count(*) into actual_count
  from pg_class c
  join pg_namespace n on n.oid = c.relnamespace
  where n.nspname = 'public'
    and c.relkind = 'r'
    and c.relrowsecurity;
  if actual_count <> 24 then
    raise exception 'expected RLS on 24 public tables, got %', actual_count;
  end if;

  select count(*) into actual_count from public.role;
  if actual_count <> 3 then
    raise exception 'expected 3 roles, got %', actual_count;
  end if;

  select count(*) into actual_count from public.style;
  if actual_count <> 4 then
    raise exception 'expected 4 styles, got %', actual_count;
  end if;

  if has_table_privilege('anon', 'public.product', 'select')
     or has_table_privilege('authenticated', 'public.product', 'select')
     or has_table_privilege('service_role', 'public.product', 'select') then
    raise exception 'Data API roles must not have product access';
  end if;

  begin
    insert into public.role (role_name) values ('SUPERUSER');
    raise exception 'invalid role was accepted';
  exception
    when check_violation then null;
  end;

  insert into public.app_user (email, password_hash, name)
  values ('schema-test@example.com', 'not-a-real-password-hash', 'Schema Test')
  returning user_id into test_user_id;

  insert into public.business (
    business_name, business_number, business_type, status
  ) values (
    'Schema Test Business', 'SCHEMA-TEST-001', 'SHOP', 'ACTIVE'
  ) returning business_id into test_business_id;

  insert into public.business_member (
    business_id, user_id, member_role, status
  ) values (
    test_business_id, test_user_id, 'OWNER', 'ACTIVE'
  ) returning business_member_id into test_member_id;

  insert into public.product (
    business_id,
    created_by_business_member_id,
    product_name,
    brand_name,
    category,
    original_price,
    rental_price,
    description,
    image_url,
    status
  ) values (
    test_business_id,
    test_member_id,
    'Schema Test Product',
    'Fitly',
    'TOP',
    100000,
    20000,
    'Constraint test fixture',
    'https://example.invalid/product.jpg',
    'ACTIVE'
  ) returning product_id into test_product_id;

  begin
    insert into public.product_variant (
      product_id, size, total_stock, available_stock, status
    ) values (
      test_product_id, 'M', 1, 2, 'ACTIVE'
    );
    raise exception 'invalid inventory was accepted';
  exception
    when check_violation then null;
  end;

  insert into public.business_contract (
    business_id, commission_rate, start_date, status
  ) values (
    test_business_id, 0.1500, current_date, 'ACTIVE'
  );

  begin
    insert into public.business_contract (
      business_id, commission_rate, start_date, status
    ) values (
      test_business_id, 0.2000, current_date, 'ACTIVE'
    );
    raise exception 'second active business contract was accepted';
  exception
    when unique_violation then null;
  end;
end;
$$;

rollback;

select 'ERD3 schema checks passed' as result;
